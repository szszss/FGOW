package net.hakugyokurou.fgow.plugin;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.execution.TaskExecutionGraph;

import com.google.common.collect.ImmutableMap;

import groovy.lang.Closure;
import net.minecraftforge.gradle.user.UserConstants;

@SuppressWarnings("serial")
class ClosureClassicWorkspace extends Closure<Object> {

	public ClosureClassicWorkspace(FgowUserPlugin owner, Object thisObject) {
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
        TaskExecutionGraph graph = project.getGradle().getTaskGraph();
        String path = project.getPath();
        File markFile = new File(project.getProjectDir().getAbsolutePath()+"/src/main/java/classic.projectmark");
        if (graph.hasTask(path + "setupClassicWorkspace") || markFile.exists())
        {
            project.getConfigurations().getByName(UserConstants.CONFIG_MC).exclude(ImmutableMap.of("module", plugin.getBinDepNamePublic()));
            project.getConfigurations().getByName(UserConstants.CONFIG_MC).exclude(ImmutableMap.of("module", plugin.getSrcDepNamePublic()));
            //project.getLogger().info("Classic mode: Remove ");
        }
        if (graph.hasTask(path + "setupClassicWorkspace"))
        {
        	plugin.getExtension().setDecomp();
        }
        if (markFile.exists() && graph.hasTask(path + "setupDecompWorkspace"))
        {
        	plugin.getExtension().setDecomp();
            plugin.setMinecraftDepsPublic(true, true);
        }
        return null;
    }
}
