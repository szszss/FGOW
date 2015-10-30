package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;

import com.google.common.collect.Sets;

import joptsimple.internal.Strings;
import net.minecraftforge.gradle.user.UserConstants;
import net.minecraftforge.gradle.util.GradleConfigurationException;
import net.minecraftforge.gradle.util.delayed.DelayedFileTree;

abstract class TasksClassicWorkspace {
	
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public static void createTasks(final FgowUserPlugin plugin) {
		/*
		final Project project = plugin.project;
		
		//setupClassicWorkspace
		Copy unpackMinecraftFiles = plugin.makeTask("unpackMinecraftFiles", Copy.class);
		{
			unpackMinecraftFiles.dependsOn("repackMinecraft");
			unpackMinecraftFiles.from(new DelayedFileTree(project, "")
			{
				
	            @Override
				public FileTree resolveDelayed(String replaced)
	            {
					ProcessJarTask decompDeobf = (ProcessJarTask) project.getTasks().getByName("deobfuscateJar");
	                pattern = (decompDeobf.isClean() ? "{API_CACHE_DIR}/"+(MAPPING_APPENDAGE) : "{BUILD_DIR}/dirtyArtifacts") + "/";
	
	                if (!Strings.isNullOrEmpty("forgeSrc"))
	                    pattern += "forgeSrc";
	                else
	                    pattern += "{API_NAME}";
	
	                pattern += "-" + "{API_VERSION}-" + CLASSIFIER_SOURCES + ".jar";
	                return super.resolveDelayed(replaced);
	            }
			});
			//unpackMinecraftFiles.from(plugin.delayedFilePublic("{CACHE_DIR}/minecraft/net/minecraftforge/forge/{API_VERSION}/forgeSrc-{API_VERSION}-sources.jar"));
			unpackMinecraftFiles.into(plugin.delayedFilePublic("{BUILD_DIR}/tmp/unpackMinecraftFiles/"));
			unpackMinecraftFiles.doFirst(new Action() {
				@Override
				public void execute(Object arg0) {
					Copy zip = (Copy)arg0;
					File file = zip.getDestinationDir();
					if(file.exists())
					{
						try {
							FileUtils.cleanDirectory(file);
						} catch (IOException e) {
							throw new GradleConfigurationException("Failed to clean old directory:" + file.getAbsolutePath(), e);
						}
					}
				}
			});
		}
		
		Copy copyMinecraftSource = plugin.makeTask("copyMinecraftSource", Copy.class);
        {
        	Action whileCopy = new ActionGenerateMD5Record(new File(plugin.sourceRecord));
        	copyMinecraftSource.dependsOn("unpackMinecraftFiles");
        	copyMinecraftSource.from(plugin.delayedFilePublic("{BUILD_DIR}/tmp/unpackMinecraftFiles"));
        	copyMinecraftSource.include("** /*.java"); FIXME:
        	copyMinecraftSource.setIncludeEmptyDirs(false);
        	copyMinecraftSource.into(project.getProjectDir().getAbsolutePath()+"/src/main/java/");
        	copyMinecraftSource.doFirst(whileCopy);
        	copyMinecraftSource.eachFile(whileCopy);
        	copyMinecraftSource.doLast(whileCopy);
        }
        
        Copy copyMinecraftResources = plugin.makeTask("copyMinecraftResources", Copy.class);
        {
        	Action whileCopy = new ActionGenerateMD5Record(new File(plugin.resourceRecord));
        	copyMinecraftResources.dependsOn("copyMinecraftSource");
        	copyMinecraftResources.from(plugin.delayedFilePublic("{BUILD_DIR}/tmp/unpackMinecraftFiles"));
        	copyMinecraftResources.exclude("** /*.java"); FIXME:
        	copyMinecraftResources.into(project.getProjectDir().getAbsolutePath()+"/src/main/resources/");
        	copyMinecraftResources.doFirst(whileCopy);
        	copyMinecraftResources.eachFile(whileCopy);
        	copyMinecraftResources.doLast(whileCopy);
        }
        
        Jar jar = (Jar)(project.getTasksByName("jar", false).toArray()[0]);
        {
        	Action whileProcess = new ActionCheckMD5Record(new File(project.getProjectDir().getAbsolutePath()+"/src/main/java/"),
        												   new File(project.getProjectDir().getAbsolutePath()+"/src/main/resources/resourcesRecord.buildignore"),
        												   new File(project.getProjectDir().getAbsolutePath()+"/src/main/java/sourceRecord.buildignore"));
        	jar.doFirst(whileProcess);
        	jar.eachFile(whileProcess);
        	jar.doLast(whileProcess);
        	jar.exclude("*.buildignore");
        }
        
        Task makeMark = plugin.makeTask("makeClassicMark", DefaultTask.class);
        {
        	makeMark.doFirst(new Action() {
				@Override
				public void execute(Object arg0) {
					File markFile = new File(project.getProjectDir().getAbsolutePath()+"/src/main/java/classic.projectmark");
					if(!markFile.exists())
						try {
							markFile.createNewFile();
							FileUtils.writeLines(markFile, Arrays.asList("This is a mark, which means that this project is classic."));
						} catch (IOException e) {
							throw new RuntimeException("Failed to create mark file.",e);
						}
				}
			});
        }
        
        Task removeMark = plugin.makeTask("removeClassicMark", DefaultTask.class);
        {
        	removeMark.doFirst(new Action() {
				@Override
				public void execute(Object arg0) {
					File markFile = new File(project.getProjectDir().getAbsolutePath()+"/src/main/java/classic.projectmark");
					if(markFile.exists())
						try {
							markFile.delete();
						} catch (Exception e) {
							throw new RuntimeException("Failed to remove mark file.",e);
						}
				}
			});
        }
        
        Task setupCIWorkspace = (Task)(project.getTasksByName("setupCIWorkspace", false).toArray()[0]);
        {
        	setupCIWorkspace.dependsOn("removeClassicMark");
        }
        
        Task setupDevWorkspace = (Task)(project.getTasksByName("setupDevWorkspace", false).toArray()[0]);
        {
        	setupDevWorkspace.dependsOn("removeClassicMark");
        }
        
        Task setupDecompWorkspace = (Task)(project.getTasksByName("setupDecompWorkspace", false).toArray()[0]);
        {
        	setupDecompWorkspace.dependsOn("removeClassicMark");
        }
        
        Task setupClassicWorkspace = plugin.makeTask("setupClassicWorkspace", DefaultTask.class);
        {
        	setupClassicWorkspace.dependsOn("genSrgs", "getAssets", "extractNatives", "copyMinecraftResources", "makeClassicMark");
	        setupClassicWorkspace.setDescription("DevWorkspace + the deobfuscated Minecraft source deployed as editable source codes. "
	        		+ "Existing files in src/main/java&resources won't be deleted or overwritten.");
	        setupClassicWorkspace.setGroup("ForgeGradle");
        }
        
        //Classic Workspace
        Task cleanClassic = plugin.makeTask("cleanClassic", DefaultTask.class);
        {
        	cleanClassic.dependsOn("removeClassicMark");
        	cleanClassic.setDescription("Reverse operation of setupClassicWorkspace in a manner. "
	        		+ "Delete the unmodified minecraft source codes and resources from src/main/java&resources/. "
	        		+ "The modified files won't be deleted.");
        	cleanClassic.setGroup("Classic Workspace");
        	cleanClassic.doFirst(new ActionCleanClassic(new File[]{
        													new File(project.getProjectDir().getAbsolutePath()+"/src/main/java/"),
        													new File(plugin.sourceRecord)
        													},
        												new File[]{
															new File(project.getProjectDir().getAbsolutePath()+"/src/main/resources/"),
															new File(plugin.resourceRecord)
														}));
        }
        
        Copy pickSource = plugin.makeTask("pickSource", Copy.class);
        {
        	Action action = new ActionPick(new File(plugin.sourceRecord));
        	pickSource.from(project.getProjectDir().getAbsolutePath()+"/src/main/java/");
        	pickSource.into(plugin.delayedFilePublic("{BUILD_DIR}/picked/java/"));
        	pickSource.setIncludeEmptyDirs(false);
        	pickSource.exclude("*.buildignore","*.projectmark");
        	pickSource.doFirst(action);
        	pickSource.eachFile(action);
        	pickSource.doLast(action);
        }
        
        Copy pickResources = plugin.makeTask("pickResources", Copy.class);
        {
        	Action action = new ActionPick(new File(plugin.resourceRecord));
        	pickResources.from(project.getProjectDir().getAbsolutePath()+"/src/main/resources/");
        	pickResources.into(plugin.delayedFilePublic("{BUILD_DIR}/picked/resources/"));
        	pickResources.setIncludeEmptyDirs(false);
        	pickResources.exclude("*.buildignore");
        	pickResources.doFirst(action);
        	pickResources.eachFile(action);
        	pickResources.doLast(action);
        }
        
        Task pickClassic = plugin.makeTask("pickClassic", DefaultTask.class);
        {
        	pickClassic.setGroup("Classic Workspace");
        	pickClassic.setDescription("Pick the new files and modified minecraft files from src/main/java&resources/ "
        			+ "and copy them to build/picked/java&resources/. "
        			+ "Existing \"java\" and \"resource\" folder will be cleaned.");
        	pickClassic.dependsOn(pickSource,pickResources);
        }
        
        project.getGradle().getTaskGraph().whenReady(new ClosureClassicWorkspace(plugin, null));*/
	}
}
