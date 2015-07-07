package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;

import com.google.common.collect.Lists;

import groovy.lang.Closure;
import net.minecraftforge.gradle.tasks.ObtainFernFlowerTask;
import net.minecraftforge.gradle.tasks.abstractutil.DownloadTask;
import net.minecraftforge.gradle.tasks.abstractutil.EtagDownloadTask;

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
		    	if(size > 4)
		    	{
		    		boolean hasFlat = false;
		    		final List<File> flat = Lists.newArrayList(plugin.delayedDirtyFilePublic("what", "a", "shame").call().getParentFile());
		    		for(Iterator<ArtifactRepository> iterator = proj.getRepositories().iterator(); iterator.hasNext(); )
		    		{
		    			ArtifactRepository artifactRepository = iterator.next();
		    			String name = artifactRepository.getName();
		    			if(name.equals("forge") || name.equals("MavenRepo") || name.equals("minecraft") || name.equals("forgeFlatRepo"))
		    				iterator.remove();
		    			if(name.equals("local"))
		    			{
		    				((FlatDirectoryArtifactRepository)artifactRepository).setDirs(flat);
		    				hasFlat = true;
		    			}
		    		}
		    		if(!hasFlat)
		    		{
		    			proj.getRepositories().flatDir(new Action<FlatDirectoryArtifactRepository>() {
		    	            @Override
		    	            public void execute(FlatDirectoryArtifactRepository repo)
		    	            {
		    	                repo.setName(plugin.getApiName()+"FlatRepo");
		    	                repo.dirs(flat);
		    	            }
		    	        });
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
		    			System.out.println(((DefaultFlatDirArtifactRepository)repository).getDirs().iterator().next());
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
		        
		        DownloadTask downloadClient = (DownloadTask)(proj.getTasksByName("downloadClient", false).toArray()[0]);
		        {
		        	downloadClient.setUrl(plugin.delayedStringPublic(reposExtension.getMcClientUrl()));
		        }
		        
		        DownloadTask downloadServer = (DownloadTask)(proj.getTasksByName("downloadServer", false).toArray()[0]);
		        {
		        	downloadServer.setUrl(plugin.delayedStringPublic(reposExtension.getMcServerUrl()));
		        }
		        
		        ObtainFernFlowerTask mcpTask = (ObtainFernFlowerTask)(proj.getTasksByName("downloadMcpTools", false).toArray()[0]);
		        {
		        	mcpTask.setMcpUrl(plugin.delayedStringPublic(reposExtension.getMcpUrl()));
		        }
		        
		        EtagDownloadTask getAssetsIndex = (EtagDownloadTask)(proj.getTasksByName("getAssetsIndex", false).toArray()[0]);
		        {
		        	getAssetsIndex.setUrl(plugin.delayedStringPublic(reposExtension.getAssestIndexUrl()));
		        	//getAssetsIndex.setFile(plugin.delayedFilePublic(Constants.ASSETS + "/indexes/{ASSET_INDEX}.json"));
		        }
		        
		        EtagDownloadTask getVersionJson = (EtagDownloadTask)(proj.getTasksByName("getVersionJson", false).toArray()[0]);
		        {
		        	getVersionJson.setUrl(plugin.delayedStringPublic(reposExtension.getMcJsonUrl()));
		        }
		        
		        TaskDownloadAssetsCopyCat.setAssetsSource(reposExtension.getAssestUrl());
			}
		});
    	return null;
    }

}
