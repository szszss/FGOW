package net.hakugyokurou.fgow.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.tasks.Copy;

class ActionGenerateMD5Record extends ActionMD5 {
	private File outFile;
	private File destDir;
	
	public ActionGenerateMD5Record(File outFile) {
		super();
		this.outFile = outFile;
	}
	
	@Override
	public void execute(Object arg0) {
		if(arg0 instanceof FileCopyDetails)
		{
			FileCopyDetails detail = (FileCopyDetails)arg0;
			File file = detail.getFile();
			if(!file.isFile())
				return;
			String md5 = getMD5(file);
			md5s.put(detail.getRelativePath().getPathString(), md5);
			File checkExistingFile = new File(destDir, detail.getRelativePath().getPathString());
			if(checkExistingFile.exists())
				detail.exclude();
		}
		else if(arg0 instanceof Copy)
		{
			Copy task = (Copy)arg0;
			if(started==false)
			{
				started = true;
				md5s = new HashMap<String, String>();
				destDir = task.getDestinationDir();
				if(!destDir.exists())
					destDir.mkdirs();
			}
			else
			{
				started = false;
				BufferedWriter bw = null;
				try {
					if(outFile.exists())
						outFile.delete();
					bw = new BufferedWriter(new FileWriter(outFile));
					boolean first = true;
					for(Entry<String, String> entry : md5s.entrySet())
					{
						if(first)
							first = false;
						else
							bw.write("\r\n");
						bw.write(entry.getKey());
						bw.write(":");
						bw.write(entry.getValue());
						bw.write(";");
					}
					bw.flush();
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Failed to generate MD5 record file.", e);
				} finally {
					if(bw!=null)
						try {bw.close();} catch (IOException e) {}
				}
				md5s.clear();
				try { FileUtils.deleteDirectory(task.getSource().getSingleFile()); } catch (Exception e) {}
			}
		}
		else 
		{
			throw new RuntimeException("Unexpected arg when copying.");
		}
	}
}
