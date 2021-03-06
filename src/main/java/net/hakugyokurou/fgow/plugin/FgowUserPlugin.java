package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.bundling.Jar;

import com.google.gson.JsonSyntaxException;

import net.minecraftforge.gradle.common.Constants;
import net.minecraftforge.gradle.user.UserConstants;
import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension;
import net.minecraftforge.gradle.user.patcherUser.forge.ForgePlugin;
import net.minecraftforge.gradle.util.delayed.DelayedFile;
import net.minecraftforge.gradle.util.delayed.DelayedString;
import net.minecraftforge.gradle.util.json.JsonFactory;
import net.minecraftforge.gradle.util.json.forgeversion.ForgeVersion;

public class FgowUserPlugin extends ForgePlugin {	
	/*protected String FML_AT            = getConstant(UserPatchConstants.class, "FML_AT");
	protected String FORGE_AT          = getConstant(UserPatchConstants.class, "FORGE_AT");
	protected String FML_PATCHES_ZIP   = getConstant(UserPatchConstants.class, "FML_PATCHES_ZIP");
	protected String FORGE_PATCHES_ZIP = getConstant(UserPatchConstants.class, "FORGE_PATCHES_ZIP");
	protected String SRC_DIR           = getConstant(UserPatchConstants.class, "SRC_DIR");
	protected String RES_DIR           = getConstant(UserPatchConstants.class, "RES_DIR");*/
	
	protected String sourceRecord   ;//project.getProjectDir().getAbsolutePath()+"/src/main/java/md5record.buildignore";
	protected String resourceRecord ;//project.getProjectDir().getAbsolutePath()+"/src/main/resources/md5record.buildignore";
	
	protected static String getConstant(Class<?> klass, String fieldName) {
		try {
			Field field = klass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (String)field.get(null);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get ForgeGradle constant: "+fieldName, e);
		}
	}	
	
	@Override
	protected void applyUserPlugin() {
		super.applyUserPlugin();
		
		sourceRecord   = project.getProjectDir().getAbsolutePath() + "/src/main/java/sourceRecord.buildignore";
		resourceRecord = project.getProjectDir().getAbsolutePath() + "/src/main/resources/resourcesRecord.buildignore";
		
		ReposExtension.applyRepoHack(this);
		//TasksClassicWorkspace.createTasks(this);
	}

	/*@Override
	protected void delayedTaskConfig() {
		super.delayedTaskConfig();
		//Fix runXXX
		Jar jar = (Jar)(project.getTasksByName("jar", false).toArray()[0]);
		
        JavaExec exec = (JavaExec) project.getTasks().getByName("runClient");
        {
        	exec.setClasspath( exec.getClasspath().minus(new SimpleFileCollection(jar.getArchivePath())) );
            exec.classpath(delayedFile("{BUILD_DIR}/classes/main/"));
            exec.classpath(delayedFile("{BUILD_DIR}/classes/api/"));
            exec.classpath(delayedFile("{BUILD_DIR}/resources/main/"));
            exec.classpath(delayedFile("{BUILD_DIR}/resources/api/"));
        }

        exec = (JavaExec) project.getTasks().getByName("runServer");
        {
            exec.setClasspath( exec.getClasspath().minus(new SimpleFileCollection(jar.getArchivePath())) );
            exec.classpath(delayedFile("{BUILD_DIR}/classes/main/"));
            exec.classpath(delayedFile("{BUILD_DIR}/classes/api/"));
            exec.classpath(delayedFile("{BUILD_DIR}/resources/main/"));
            exec.classpath(delayedFile("{BUILD_DIR}/resources/api/"));
        }

        exec = (JavaExec) project.getTasks().getByName("debugClient");
        {
            exec.setClasspath( exec.getClasspath().minus(new SimpleFileCollection(jar.getArchivePath())) );
            exec.classpath(delayedFile("{BUILD_DIR}/classes/main/"));
            exec.classpath(delayedFile("{BUILD_DIR}/classes/api/"));
            exec.classpath(delayedFile("{BUILD_DIR}/resources/main/"));
            exec.classpath(delayedFile("{BUILD_DIR}/resources/api/"));
        }

        exec = (JavaExec) project.getTasks().getByName("debugServer");
        {
            exec.setClasspath( exec.getClasspath().minus(new SimpleFileCollection(jar.getArchivePath())) );
            exec.classpath(delayedFile("{BUILD_DIR}/classes/main/"));
            exec.classpath(delayedFile("{BUILD_DIR}/classes/api/"));
            exec.classpath(delayedFile("{BUILD_DIR}/resources/main/"));
            exec.classpath(delayedFile("{BUILD_DIR}/resources/api/"));
        }
	}*/

	@Override //hack every json :)
	protected String getWithEtag(String strUrl, File cache, File etagFile) {
		final String forgeJsonUrl = Constants.URL_FORGE_MAVEN + "/net/minecraftforge/forge/json";
		if(strUrl.equals(Constants.URL_MCP_JSON) && project.hasProperty("mcpJsonUrl"))
		{
			strUrl = project.property("mcpJsonUrl").toString();
		}
		else if(strUrl.equals(forgeJsonUrl) && project.hasProperty("forgeJsonUrl"))
		{
			strUrl = project.property("forgeJsonUrl").toString();
		}
		else if(strUrl.equals("https://www.abrarsyed.com/ForgeGradleVersion.json") && project.hasProperty("fgVersionUrl"))
		{
			strUrl = project.property("fgVersionUrl").toString();
		}
		else if(strUrl.equals(Constants.URL_MC_MANIFEST) && project.hasProperty("mcManifestUrl"))
		{
			strUrl = project.property("mcManifestUrl").toString();
		}
		return super.getWithEtag(strUrl, cache, etagFile);
	}
	
	

	//hack forge json 
	//UNUSED.
	/*protected void setForgeVersionJsonAgain() {
		if(!project.hasProperty("forgeJsonUrl"))
			return;
		File jsonCache = cacheFile("ForgeVersion.json");
        File etagFile = new File(jsonCache.getAbsolutePath() + ".etag");
        String url =  project.property("forgeJsonUrl").toString();;
        ForgeVersion forgeVersion = JsonFactory.GSON.fromJson(getWithEtag(url, jsonCache, etagFile), ForgeVersion.class);
        try
        {
        	ForgeExtension extension = getExtension();
        	Field field = extension.getClass().getField("forgeJson");
        	field.setAccessible(true);
        	field.set(extension, forgeVersion);
            //getExtension().forgeJson = JsonFactory.GSON.fromJson(getWithEtag(url, jsonCache, etagFile), ForgeVersion.class);
        }
        catch(Exception e)
        {
        }
	}*/

	@Override
	protected void doFGVersionCheck(List<String> outLines) {
		if(project.hasProperty("skipFGVersionCheck"))
		{
			Object prop = project.property("skipFGVersionCheck");
			if(prop instanceof Boolean && ((Boolean)prop).booleanValue() == true)
				return;
			if(prop instanceof String && ((String)prop).equalsIgnoreCase("true"))
				return;
		}
		super.doFGVersionCheck(outLines);
	}

	public DelayedString delayedStringPublic(String path) {
		return delayedString(path);
	}

	public DelayedFile delayedFilePublic(String path) {
		return delayedFile(path);
	}

	/*public String getSrcDepNamePublic() {
		return getSrcDepName();
	}

	public String getBinDepNamePublic() {
		return getBinDepName();
	}
	
	public void setMinecraftDepsPublic(boolean decomp, boolean remove) {
		setMinecraftDeps(decomp, remove);
	}*/
}
