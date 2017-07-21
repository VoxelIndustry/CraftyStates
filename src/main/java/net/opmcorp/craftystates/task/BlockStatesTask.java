package net.opmcorp.craftystates.task;

import net.opmcorp.craftystates.CraftyStatesExtension;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Collection;

public class BlockStatesTask extends DefaultTask
{

	public BlockStatesTask()
	{
	}

	@TaskAction
	public void transpile()
	{
		CraftyStatesExtension extension = getProject().getExtensions().findByType(CraftyStatesExtension.class);
		if (extension == null)
			extension = new CraftyStatesExtension();

		Collection<File> blockstates = FileUtils.listFiles(
				new File(getProject().getRootDir(), extension.getBlockstatesPath()), new String[]{"cs.json"}, true);

		BlockStateTranspiler task = new BlockStateTranspiler();
		blockstates.forEach(task::transpileBlockState);
	}
}