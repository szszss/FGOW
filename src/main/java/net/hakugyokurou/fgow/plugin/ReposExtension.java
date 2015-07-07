package net.hakugyokurou.fgow.plugin;

public class ReposExtension {
	
	//private boolean useMavenCentral = true;
	//private String customMavenUrl = null;
	private String mcJsonUrl   = "http://s3.amazonaws.com/Minecraft.Download/versions/{MC_VERSION}/{MC_VERSION}.json";
	private String mcClientUrl = "http://s3.amazonaws.com/Minecraft.Download/versions/{MC_VERSION}/{MC_VERSION}.jar";
	private String mcServerUrl = "http://s3.amazonaws.com/Minecraft.Download/versions/{MC_VERSION}/minecraft_server.{MC_VERSION}.jar";
	private String assestUrl = "http://resources.download.minecraft.net";
	private String assestIndexUrl = "http://s3.amazonaws.com/Minecraft.Download/indexes/{ASSET_INDEX}.json";
	//private String libraryUrl = "https://libraries.minecraft.net/";
	//private String forgeUrl = "http://files.minecraftforge.net/maven";
	private String mcpUrl = "http://files.minecraftforge.net/fernflower-fix-1.0.zip";
	//private String mcpJsonUrl = "http://export.mcpbot.bspk.rs/versions.json";
	//private String forgeJsonUrl = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
	private static boolean gradleIsMumFucker = false;

	/*public boolean isUseMavenCentral() {
		return useMavenCentral;
	}
	public void setUseMavenCentral(boolean useMavenCentral) {
		this.useMavenCentral = useMavenCentral;
	}
	public String getCustomMavenUrl() {
		return customMavenUrl;
	}
	public void setCustomMavenUrl(String customMavenUrl) {
		this.customMavenUrl = customMavenUrl;
	}*/
	public String getMcClientUrl() {
		return mcClientUrl;
	}
	public void setMcClientUrl(String mcClientUrl) {
		this.mcClientUrl = mcClientUrl;
	}
	public String getMcServerUrl() {
		return mcServerUrl;
	}
	public void setMcServerUrl(String mcServerUrl) {
		this.mcServerUrl = mcServerUrl;
	}
	public String getAssestIndexUrl() {
		return assestIndexUrl;
	}
	public void setAssestIndexUrl(String assestIndexUrl) {
		this.assestIndexUrl = assestIndexUrl;
	}
	public String getMcJsonUrl() {
		return mcJsonUrl;
	}
	public void setMcJsonUrl(String mcJsonUrl) {
		this.mcJsonUrl = mcJsonUrl;
	}
	public String getAssestUrl() {
		return assestUrl;
	}
	public void setAssestUrl(String assestUrl) {
		this.assestUrl = assestUrl;
	}
	/*public String getMcpJsonUrl() {
		return mcpJsonUrl;
	}
	public void setMcpJsonUrl(String mcpJsonUrl) {
		this.mcpJsonUrl = mcpJsonUrl;
	}
	public String getForgeJsonUrl() {
		return forgeJsonUrl;
	}
	public void setForgeJsonUrl(String forgeJsonUrl) {
		this.forgeJsonUrl = forgeJsonUrl;
	}
	public String getLibraryUrl() {
		return libraryUrl;
	}
	public void setLibraryUrl(String libraryUrl) {
		this.libraryUrl = libraryUrl;
	}
	public String getForgeUrl() {
		return forgeUrl;
	}
	public void setForgeUrl(String forgeUrl) {
		this.forgeUrl = forgeUrl;
	}*/
	public String getMcpUrl() {
		return mcpUrl;
	}
	public void setMcpUrl(String mcpUrl) {
		this.mcpUrl = mcpUrl;
	}
	
	public static ReposExtension getReposExtension(FgowUserPlugin plugin) {
		Object ext;
		if(!gradleIsMumFucker)
		{
			gradleIsMumFucker = true;
			ext = plugin.project.getExtensions().create("repos", ReposExtension.class);
		}
		else
		{
			ext = plugin.project.getExtensions().getByName("repos");
		}
		return (ReposExtension)ext;
	}
	
	public static void applyRepoHack(FgowUserPlugin plugin) {
		getReposExtension(plugin);
		plugin.project.allprojects(new ActionRepoHacker(plugin));
		plugin.project.getGradle().getTaskGraph().whenReady(new ClosureRepoHacker(plugin, null));
	}
}
