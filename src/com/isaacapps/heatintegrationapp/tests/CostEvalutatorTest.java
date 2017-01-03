package com.isaacapps.heatintegrationapp.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.isaacapps.heatintegrationapp.internals.IndustrialProcess;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Column;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class CostEvalutatorTest {
	IndustrialProcess industrialProcess;
	
	@Before
	public void setUp() throws Exception {
		List<Stream> streams = new ArrayList<Stream>();
		streams.add(new Stream("H1", 270.0f, 58.0f, 0.1f, 0.0f));
		streams.add(new Stream("C1", 46.0f, 170.0f, 0.98f, 0.0f));
	

		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column( new double[]{50.0f}, new double[]{550.0f}, new double[]{60.0f}, new double[]{350.0f} ));
		
		industrialProcess = new IndustrialProcess(streams, columns, 10.0, 0.0, 0.0, "kW");
	}

	@Test
	public void testCalculateGlobalCost() {
		fail("Not yet implemented"); // TODO
	}

}
