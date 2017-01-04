package com.isaacapps.heatintegrationapp.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.isaacapps.heatintegrationapp.internals.ProblemTable;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Column;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class ProblemTableTest {
	ProblemTable problemTable;

	@Before
	public void setUp() throws Exception {
		double deltaTMin = 10.0;
		
		List<Stream> streams = new ArrayList<Stream>();
		streams.add(new Stream("H1", 270.0, 35.0, 0.1, 0.0));
		streams.add(new Stream("H2", 220.0, 60.0, 0.22, 0.0));
		streams.add(new Stream("H3", 252.0, 251.0, 4.75, 0.0));
		streams.add(new Stream("H4", 192.85, 191.85f, 5.25, 0.0));
		streams.add(new Stream("C1", 40.0, 190.0, 0.18, 0.0));
		streams.add(new Stream("C2", 150.0, 240.0, 0.3, 0.0));
		streams.add(new Stream("C3", 25.0, 35.0, 1.47, 0.0));

		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column( "Column 1", new double[]{40.0}, new double[]{450.0}, new double[]{20.0}, new double[]{250.0}, deltaTMin ));

		streams.stream().forEach(s -> s.setShiftTemps(deltaTMin));
		columns.stream().forEach(c -> c.setShiftTemps(deltaTMin));

		problemTable = new ProblemTable(streams, columns, deltaTMin);
	}

	@Test
	public void test_CascadeInterval_Temperatures() {
		String[] expectedIntervalTemps = new String[]{"265.0 K", "247.0 K", "246.0 K", "245.0 K", "215.0 K", "195.0 K", "187.85 K"
				,"186.85 K", "155.0 K", "55.0 K", "45.0 K", "45.0 K", "40.0 K", "30.0 K", "15.0 K", "15.0 K" };
		java.util.stream.Stream<String> actualIntervalTemps = java.util.stream.Stream.concat(java.util.stream.Stream.of(problemTable.getCascadeIntervals().get(0).getSourceShiftTempWithUnit())
				                                              , problemTable.getCascadeIntervals().stream().map(cI -> cI.getTargetShiftTempWithUnit()));
		
		assertArrayEquals("Cascade interval temperature elements do not match.", expectedIntervalTemps, actualIntervalTemps.toArray());
	}

	@Test
	public void test_CascadeInterval_HeatLoads() {
		String[] expectedHeatLoads = new String[]{ "1.8 kW", "4.85 kW", "0.1 kW", "-6.0 kW", "0.4 kW", "-1.144 kW", "5.09 kW", "-5.096 kW", "14.0 kW"
				                                        , "-0.8 kW" , "-450.0 kW", "0.5 kW", "-13.7 kW", "0.0 kW",  "250.0 kW"};
		java.util.stream.Stream<String> actualHeatLoads = problemTable.getCascadeIntervals().stream()
																.map(cI -> cI.getHeatLoadWithUnit());

		assertArrayEquals("Cascade interval heat load elements do not match.", expectedHeatLoads,actualHeatLoads.toArray());
	}

	@Test
	public void test_CascadeInterval_CascadingEnergies() {
		String[] expectedCascadingEnergies = new String[] { "451.8 kW", "456.65 kW", "456.75 kW", "450.75 kW", "451.15 kW",
				"450.006 kW", "455.096 kW", "450.0 kW", "464.0 kW", "463.2 kW", "13.2 kW", "13.7 kW", "0.0 kW", "0.0 kW" , "250.0 kW" };
		java.util.stream.Stream<String>  actualCascadingEnergies = problemTable.getCascadeIntervals().stream()
																		.map(cI -> cI.getCascadeEnergyWithUnit());

		assertArrayEquals("Cascade interval cascading energy elements do not match.", expectedCascadingEnergies, actualCascadingEnergies.toArray());
	}

	@Test
	public void test_CascadeInterval_Types() {
		String[] expectedIntervalTypes = new String[] { "Stream Interval Containing: H1", "Stream Interval Containing: H1, H3", "Stream Interval Containing: H1"
				    , "Stream Interval Containing: C2, H1", "Stream Interval Containing: C2, H1, H2", "Stream Interval Containing: C1, C2, H1, H2", "Stream Interval Containing: C1, C2, H1, H2, H4"
				    , "Stream Interval Containing: C1, C2, H1, H2", "Stream Interval Containing: C1, H1, H2", "Stream Interval Containing: C1, H1"
				    , "Column Interval Containing: Reboiler of Column 1", "Stream Interval Containing: H1", "Stream Interval Containing: C3, H1", "Stream Interval Containing: ", "Column Interval Containing: Condenser of Column 1"};
		java.util.stream.Stream<String> actualIntervalTypes = problemTable.getCascadeIntervals().stream().map(cI -> cI.getType());
		
		assertArrayEquals(expectedIntervalTypes, actualIntervalTypes.toArray());
	}
	
	@Test
	public void test_PinchTemps() {
		assertEquals("Does not contain first shift pinch temperature.", "15.0 K",
				problemTable.getShiftPinchTempsWithUnit().stream().sorted().toArray()[0]);
		assertEquals("Does not contain second shift pinch temperature.", "30.0 K",
				problemTable.getShiftPinchTempsWithUnit().stream().sorted().toArray()[1]);

		assertEquals("Does not contain first unshifted pinch temperature.", "20.0 K",
				problemTable.getUnshiftedPinchTempsWithUnit().stream().sorted().toArray()[0]);
		assertEquals("Does not contain second unshifted pinch temperature.", "25.0 K",
				problemTable.getUnshiftedPinchTempsWithUnit().stream().sorted().toArray()[1]);
		assertEquals("Does not contain third unshifted pinch temperature.", "35.0 K",
				problemTable.getUnshiftedPinchTempsWithUnit().stream().sorted().toArray()[2]);
	}

	@Test
	public void test_Utilities() {
		assertEquals("MER hot utility (QH) is incorrect.", "450.0 kW", problemTable.getMerQhWithUnit());
		
		assertEquals("MER cold utility (QC) is incorrect.", "250.0 kW", problemTable.getMerQcWithUnit());
	}

}
