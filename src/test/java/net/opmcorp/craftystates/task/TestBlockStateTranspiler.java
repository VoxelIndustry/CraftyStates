package net.opmcorp.craftystates.task;

import net.opmcorp.craftystates.CraftyStatesExtension;
import org.junit.Test;

import java.io.File;

public class TestBlockStateTranspiler
{
	@Test
	public void testTranspileBlockState()
	{
		BlockStateTranspiler transpiler = new BlockStateTranspiler();

		CraftyStatesExtension extension = new CraftyStatesExtension();
		extension.setPrettyPrinting(true);

		transpiler.setExtension(extension);

		transpiler.transpileBlockState(new File("tests/ironcopperore.cs.json"));
	}
}
