package net.hakugyokurou.fgow.plugin;

import java.util.HashMap;
import org.gradle.api.Action;
import org.gradle.api.Project;
import net.minecraftforge.gradle.common.Constants;

class ActionRepoHacker implements Action<Project> {
	
	protected final FgowUserPlugin plugin;
	
	public ActionRepoHacker(FgowUserPlugin plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Project proj) {
        TaskDownloadAssetsCopyCat downloadAssets;
        {
        	HashMap map = new HashMap();
        	map.put("name", "getAssets");
        	map.put("type", TaskDownloadAssetsCopyCat.class);
        	map.put("overwrite", true);
        	downloadAssets = (TaskDownloadAssetsCopyCat)proj.task(map, "getAssets");
        	downloadAssets.setAssetsDir(plugin.delayedFilePublic(Constants.DIR_ASSETS));
        	downloadAssets.setAssetsIndex(plugin.delayedFilePublic(Constants.JSON_ASSET_INDEX));
        	downloadAssets.dependsOn(Constants.TASK_DL_ASSET_INDEX);
        }
	}
}
