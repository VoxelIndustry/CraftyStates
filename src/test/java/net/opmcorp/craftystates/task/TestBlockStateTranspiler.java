package net.opmcorp.craftystates.task;

import org.junit.Test;

import java.io.File;

public class TestBlockStateTranspiler
{
	@Test
	public void testTranspileBlockState()
	{
		BlockStateTranspiler transpiler = new BlockStateTranspiler();

		transpiler.transpileBlockState(new File("tests/ironcopperore.json"));
	}
}
