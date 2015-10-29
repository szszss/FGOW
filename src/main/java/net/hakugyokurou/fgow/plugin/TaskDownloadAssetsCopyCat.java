package net.hakugyokurou.fgow.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import groovy.lang.Closure;
import net.minecraftforge.gradle.common.Constants;
import net.minecraftforge.gradle.tasks.DownloadAssetsTask;
import net.minecraftforge.gradle.util.delayed.DelayedFile;
import net.minecraftforge.gradle.util.json.JsonFactory;
import net.minecraftforge.gradle.util.json.version.AssetIndex;
import net.minecraftforge.gradle.util.json.version.AssetIndex.AssetEntry;

public class TaskDownloadAssetsCopyCat extends DefaultTask {
	
	protected static String source = Constants.URL_ASSETS;
	
	public static void setAssetsSource(String source) {
		TaskDownloadAssetsCopyCat.source = source;
	}
	
	//---------------same to TaskDownloadAssets-----------------
	DelayedFile           assetsDir;

    Object                assetIndex;

    private File          virtualRoot  = null;
    private final File    minecraftDir = new File(Constants.getMinecraftDirectory(), "assets/objects");

    private static final int MAX_TRIES = 5;

    @TaskAction
    public void doTask() throws IOException, InterruptedException
    {
        File outDir = new File(getAssetsDir(), "objects");
        if (!outDir.exists() || !outDir.isDirectory())
        {
            outDir.mkdirs();
        }

        File indexFile = getAssetsIndex();
        AssetIndex index = JsonFactory.loadAssetsIndex(indexFile);

        // check virtual
        if (index.virtual)
        {
            virtualRoot = new File(getAssetsDir(), "virtual/" + Files.getNameWithoutExtension(indexFile.getName()));
            virtualRoot.mkdirs();
        }
        
        // make thread pool
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
        
        for (Entry<String, AssetEntry> e : index.objects.entrySet())
        {
            Asset asset = new Asset(e.getKey(), e.getValue().hash, e.getValue().size);
            executor.submit(new GetAssetTask(asset, outDir, minecraftDir, virtualRoot));
        }
        
        executor.shutdown(); // complete all the tasks then shutdown.

        int max = (int) executor.getTaskCount(); // its gonna be somewhere around 600-700 I think

        // as long as the excutor isnt dead yet.
        while (!executor.awaitTermination(1, TimeUnit.SECONDS))
        {
            int done = (int) executor.getCompletedTaskCount();
            getLogger().lifecycle("Current status: {}/{}   {}%", done, max, (int) ((double) done / max * 100));
        }
    }

    public File getAssetsDir()
    {
        return assetsDir.call();
    }

    public void setAssetsDir(DelayedFile assetsDir)
    {
        this.assetsDir = assetsDir;
    }

    public File getAssetsIndex()
    {
        return getProject().file(assetIndex);
    }

    public void setAssetsIndex(Closure<File> index)
    {
        this.assetIndex = index;
    }

    private static class Asset
    {
        public final String name;
        public final String path;
        public final String hash;
        public final long   size;

        Asset(String name, String hash, long size)
        {
            this.name = name;
            this.path = hash.substring(0, 2) + "/" + hash;
            this.hash = hash.toLowerCase();
            this.size = size;
        }
    }    
    private static boolean checkFileCorrupt(File file, long size, String expectedHash)
    {
        if (!file.exists())
            return true;
        
        if (file.length() != size)
            return true;
        
        if (!expectedHash.equalsIgnoreCase(Constants.hash(file, "SHA1")))
            return true;
        
        return false;
    }

    private static class GetAssetTask implements Callable<Boolean>
    {
        private static final Logger LOGGER = LoggerFactory.getLogger(GetAssetTask.class);
        private final Asset asset;
        private final File assetDir, minecraftDir, virtualRoot;
        
        private GetAssetTask(Asset asset, File assetDir, File minecraftDir, File virtualRoot)
        {
            this.asset = asset;
            this.assetDir = assetDir;
            this.minecraftDir = minecraftDir;
            this.virtualRoot = virtualRoot;
        }
        
        @Override
        public Boolean call()
        {
            boolean worked = true;
            
            for (int tryNum = 1; tryNum < MAX_TRIES + 1; tryNum++)
            {
                try
                {
                    File file = new File(assetDir, asset.path);

                    if (checkFileCorrupt(file, asset.size, asset.hash))
                    {
                        file.delete();
                    }

                    // if it exists, its good, so we dont do this stuff...
                    if (!file.exists())
                    {
                        file.getParentFile().mkdirs();
                        File localMc = new File(minecraftDir, asset.path);
                        
                        if (checkFileCorrupt(localMc, asset.size, asset.hash))
                        {
                            // download
                            ReadableByteChannel channel = Channels.newChannel(new URL(source + "/" + asset.path).openStream());
                            FileOutputStream fout = new FileOutputStream(file);
                            FileChannel fileChannel = fout.getChannel();
                            
                            fileChannel.transferFrom(channel, 0, asset.size);
                            
                            channel.close();
                            fout.close();
                        }
                        else
                        {
                            // copy from MC
                            Constants.copyFile(localMc, file, asset.size);
                        }
                    }
                    
                    
                    if (virtualRoot != null)
                    {
                        File virtual = new File(virtualRoot, asset.name);
                        
                        if (checkFileCorrupt(virtual, asset.size, asset.hash))
                        {
                            virtual.delete();
                            Constants.copyFile(file, virtual);
                        }
                    }
                    
                }
                catch (Exception e)
                {
                    LOGGER.error("Error downloading asset (try {}) : {}", tryNum, asset.name);
                    e.printStackTrace();
                    worked = false;
                }
            }
            
            return worked;
        }
    }
}
