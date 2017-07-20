package net.opmcorp.craftystates;

import net.opmcorp.craftystates.task.BlockStatesTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CraftyStates implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
		project.getExtensions().create("statesTranspiling", CraftyStatesExtension.class);
		project.getTasks().create("blockstatesTranspiling", BlockStatesTask.class);

		project.getTasks().findByName("deobf").dependsOn(project.getTasks().getByName("blockstatesTranspiling"));
	}
}