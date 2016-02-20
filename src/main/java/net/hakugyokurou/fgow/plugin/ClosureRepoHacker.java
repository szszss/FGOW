package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.AbstractArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.DefaultFlatDirArtifactRepository;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;

import com.google.common.collect.Lists;

import groovy.lang.Closure;
import net.minecraftforge.gradle.common.Constants;
import net.minecraftforge.gradle.tasks.Download;
import net.minecraftforge.gradle.tasks.EtagDownloadTask;
import net.minecraftforge.gradle.tasks.ObtainFernFlowerTask;

@SuppressWarnings("serial")
public class ClosureRepoHacker extends Closure<Object> {

	public ClosureRepoHacker(FgowUserPlugin owner, Object thisObject) {
		super(owner, thisObject);
	}
	
	@Override
    public Object call(Object obj)
    {
        return call();
    }

    @Override
    public Object call(Object... obj)
    {
        return call();
    }
    
    @Override
    public Object call()
    {
    	final FgowUserPlugin plugin = (FgowUserPlugin)getOwner();
    	final Project project = plugin.project;
    	project.allprojects(new Action<Project>() {
			@Override
			public void execute(Project proj) {
				Project rootProject = proj.getRootProject();
		    	while(rootProject != rootProject.getRootProject())  //Get the "rootest" project.
		    		rootProject = rootProject.getRootProject();
		    	//ReposExtension exten = (ReposExtension)rootProject.getExtensions().findByName("repos"); //Reset repositories.
		    	int size = proj.getRepositories().size();
		    	if(size > 5)
		    	{
		    		boolean hasFlat = false;
		    		final Set<File> flat = new HashSet<File>();
		    		FlatDirectoryArtifactRepository local = null;
		    		for(Iterator<ArtifactRepository> iterator = proj.getRepositories().iterator(); iterator.hasNext(); )
		    		{
		    			ArtifactRepository artifactRepository = iterator.next();
		    			String name = artifactRepository.getName();
		    			if(name.equals("forge") || name.equals("MavenRepo") || name.equals("minecraft"))
		    				iterator.remove();
		    			else if(name.equals("TweakerMcRepo"))
		    			{
		    				flat.addAll(((FlatDirectoryArtifactRepository)artifactRepository).getDirs());
		    				iterator.remove();
		    			}
		    			else if(name.equals("deobfDeps"))
		    			{
		    				flat.add(new File(((MavenArtifactRepository)artifactRepository).getUrl()));
		    				iterator.remove();
		    			}
		    			else if(name.equals("local"))
		    			{
		    				local = (FlatDirectoryArtifactRepository)artifactRepository;
		    				//local.setName("TweakerMcRepo");
		    				hasFlat = true;
		    			}
		    		}
		    		if(!hasFlat)
		    		{
		    			proj.getRepositories().flatDir(new Action<FlatDirectoryArtifactRepository>() {
		    	            @Override
		    	            public void execute(FlatDirectoryArtifactRepository repo)
		    	            {
		    	                repo.setName("TweakerMcRepo");
		    	                repo.setDirs(flat);
		    	            }
		    	        });
		    		}
		    		else
		    		{
		    			local.setDirs(flat);
		    		}
		    	}
		    	//For debug
		    	/*for(Object o : proj.getRepositories())
		    	{
		    		AbstractArtifactRepository repository = (AbstractArtifactRepository)o;
		    		System.out.println("======================");
		    		System.out.println(repository.getName());
		    		if(repository instanceof DefaultMavenArtifactRepository)
		    		{
		    			System.out.println(((DefaultMavenArtifactRepository)repository).getUrl());
		    		}
		    		else if(repository instanceof DefaultFlatDirArtifactRepository)
		    		{
		    			for(File file : ((DefaultFlatDirArtifactRepository)repository).getDirs())
		    			{
		    				System.out.println(file);
		    			}
		    		}        			
		    	}*/
		    	
		    	/*proj.getRepositories().clear();
		    	final String repoDir = plugin.delayedDirtyFilePublic("this", "doesnt", "matter").call().getParentFile().getAbsolutePath();
		    	plugin.addFlatRepo(proj, plugin.getApiName()+"FlatRepo", repoDir);
		    	plugin.addMavenRepo(proj, "forge", exten.getForgeUrl());
		    	String customMaven = exten.getCustomMavenUrl();
		    	if(customMaven!=null && !customMaven.trim().equals(""))
		    		plugin.addMavenRepo(proj, "custommaven", customMaven);
		    	if(exten.isUseMavenCentral())
		    		proj.getRepositories().mavenCentral();
		    	plugin.addMavenRepo(proj, "minecraft", exten.getLibraryUrl());*/
		        
		        //---------------------------------------------------------------------------------------------------------
		        //Hack downloading address
		        
		        ReposExtension reposExtension = ReposExtension.getReposExtension(plugin);
		        
		        Download downloadClient = (Download)(proj.getTasksByName(Constants.TASK_DL_CLIENT, false).toArray()[0]);
		        {
		        	downloadClient.setUrl(plugin.delayedStringPublic(reposExtension.getMcClientUrl()));
		        }
		        
		        Download downloadServer = (Download)(proj.getTasksByName(Constants.TASK_DL_SERVER, false).toArray()[0]);
		        {
		        	downloadServer.setUrl(plugin.delayedStringPublic(reposExtension.getMcServerUrl()));
		        }
		        
		        EtagDownloadTask getAssetsIndex = (EtagDownloadTask)(proj.getTasksByName(Constants.TASK_DL_ASSET_INDEX, false).toArray()[0]);
		        {
		        	getAssetsIndex.setUrl(plugin.delayedStringPublic(reposExtension.getAssestIndexUrl()));
		        	//getAssetsIndex.setFile(plugin.delayedFilePublic(Constants.ASSETS + "/indexes/{ASSET_INDEX}.json"));
		        }
		        
		        EtagDownloadTask getVersionJson = (EtagDownloadTask)(proj.getTasksByName(Constants.TASK_DL_VERSION_JSON, false).toArray()[0]);
		        {
		        	getVersionJson.setUrl(plugin.delayedStringPublic(reposExtension.getMcJsonUrl()));
		        }
		        
		        TaskDownloadAssetsCopyCat.setAssetsSource(reposExtension.getAssestUrl());
			}
		});
    	return null;
    }

}
