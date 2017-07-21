package net.opmcorp.craftystates;

import net.opmcorp.craftystates.task.BlockStatesTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.slf4j.LoggerFactory;

public class CraftyStates implements Plugin<Project>
{
	public static Logger LOGGER;

	@Override
	public void apply(Project project)
	{
		project.getExtensions().create("statesTranspiling", CraftyStatesExtension.class);
		project.getTasks().create("blockstatesTranspiling", BlockStatesTask.class);

		project.getTasks().findByName("processResources").dependsOn(project.getTasks().getByName("blockstatesTranspiling"));

		LOGGER = project.getLogger();
	}

	public static org.slf4j.Logger getLogger()
	{
		if (LOGGER != null)
			return LOGGER;
		else
		{
			// Test env
			return LoggerFactory.getLogger("CraftyStates");
		}
	}
}