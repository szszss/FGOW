package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.lang.reflect.Field;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.bundling.Jar;

import net.minecraftforge.gradle.common.Constants;
import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.delayed.DelayedFileTree;
import net.minecraftforge.gradle.delayed.DelayedString;
import net.minecraftforge.gradle.tasks.ProcessJarTask;
import net.minecraftforge.gradle.tasks.ProcessSrcJarTask;
import net.minecraftforge.gradle.user.UserConstants;
import net.minecraftforge.gradle.user.patch.UserPatchBasePlugin;
import net.minecraftforge.gradle.user.patch.UserPatchConstants;

public class FgowUserPlugin extends UserPatchBasePlugin {	
	protected String FML_AT            = getConstant(UserPatchConstants.class, "FML_AT");
	protected String FORGE_AT          = getConstant(UserPatchConstants.class, "FORGE_AT");
	protected String FML_PATCHES_ZIP   = getConstant(UserPatchConstants.class, "FML_PATCHES_ZIP");
	protected String FORGE_PATCHES_ZIP = getConstant(UserPatchConstants.class, "FORGE_PATCHES_ZIP");
	protected String SRC_DIR           = getConstant(UserPatchConstants.class, "SRC_DIR");
	protected String RES_DIR           = getConstant(UserPatchConstants.class, "RES_DIR");
	
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
	public void applyPlugin() {
		super.applyPlugin();
		
		sourceRecord   = project.getProjectDir().getAbsolutePath() + "/src/main/java/sourceRecord.buildignore";
		resourceRecord = project.getProjectDir().getAbsolutePath() + "/src/main/resources/resourcesRecord.buildignore";
		
		ReposExtension.applyRepoHack(this);
		TasksClassicWorkspace.createTasks(this);		
	}
	
	@Override
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
	}
	
	@Override //almost same to forge plugin
	protected void doVersionChecks(int buildNumber) {
		if (buildNumber != 0 && buildNumber < 1048)
            throw new IllegalArgumentException("Your ForgeGradle is too old ( <10.12.0.1048 ), pal!");
	}

	@Override //hack mcp json
	protected String getWithEtag(String strUrl, File cache, File etagFile) { 
		if(strUrl.equals(Constants.MCP_JSON_URL) && project.hasProperty("mcpJsonUrl"))
		{
			strUrl = project.property("mcpJsonUrl").toString();
		}
		return super.getWithEtag(strUrl, cache, etagFile);
	}

	@Override //hack forge json
	protected String getVersionsJsonUrl() {
		if(project.hasProperty("forgeJsonUrl"))
			return project.property("forgeJsonUrl").toString();
		return Constants.FORGE_MAVEN + "/net/minecraftforge/forge/json";
	}

	@Override //same to forge plugin
	protected void configurePatching(ProcessSrcJarTask patch) {
		patch.addStage("fml", delayedFile(FML_PATCHES_ZIP), delayedFile(SRC_DIR), delayedFile(RES_DIR));
        patch.addStage("forge", delayedFile(FORGE_PATCHES_ZIP));
	}

	@Override //same to forge plugin
	protected String getApiGroup() {
		return "net.minecraftforge";
	}

	@Override //same to forge plugin
	public String getApiName() {
		return "forge";
	}

	@Override //same to forge plugin
	protected void configureDeobfuscation(ProcessJarTask task) {
		task.addTransformerClean(delayedFile(FML_AT));
        task.addTransformerClean(delayedFile(FORGE_AT));
	}

	public DelayedFile delayedDirtyFilePublic(String name, String classifier, String ext) {
		return delayedDirtyFile(name, classifier, ext);
	}

	public DelayedString delayedStringPublic(String path) {
		return delayedString(path);
	}

	public DelayedFile delayedFilePublic(String path) {
		return delayedFile(path);
	}

	public String getSrcDepNamePublic() {
		return getSrcDepName();
	}

	public String getBinDepNamePublic() {
		return getBinDepName();
	}
	
	public void setMinecraftDepsPublic(boolean decomp, boolean remove) {
		setMinecraftDeps(decomp, remove);
	}
}
