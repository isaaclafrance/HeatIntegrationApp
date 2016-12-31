package com.isaacapps.heatintegrationapp.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.isaacapps.heatintegrationapp.internals.ProblemTable;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Column;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class ProblemTableTest {
	ProblemTable problemTable;

	@Before
	public void setUp() throws Exception {
		double deltaTMin = 10.0f;
		
		List<Stream> streams = new ArrayList<Stream>();
		streams.add(new Stream("H1", 270.0f, 35.0f, 0.1f, 0.0f));
		streams.add(new Stream("H2", 220.0f, 60.0f, 0.22f, 0.0f));
		streams.add(new Stream("H3", 252.0f, 251.0f, 4.75f, 0.0f));
		streams.add(new Stream("H4", 192.85f, 191.85f, 5.25f, 0.0f));
		streams.add(new Stream("C1", 40.0f, 190.0f, 0.18f, 0.0f));
		streams.add(new Stream("C2", 150.0f, 240.0f, 0.3f, 0.0f));
		streams.add(new Stream("C3", 25.0f, 35.0f, 1.47f, 0.0f));

		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column( "Column 1", new double[]{40.0f}, new double[]{450.0f}, new double[]{20.0f}, new double[]{250.0f}, deltaTMin ));

		streams.stream().forEach(s -> s.setShiftTemps(deltaTMin));
		columns.stream().forEach(c -> c.setShiftTemps(deltaTMin));

		problemTable = new ProblemTable(streams, columns, deltaTMin);
	}

	@Test
	public void test_CascadeInterval_Temperatures() {
		Object[] expectedIntervalTemps = new Object[] { 265.0, 247.0, 246.0, 245.0, 215.0, 195.0, 187.85
				,186.85, 155.0, 55.0, 45.0, 45.0, 40.0, 30.0, 15.0, 15.0 };
		List<Double> actualIntervalTemps = problemTable.getCascadeIntervals().stream().map(cI -> cI.getTargetShiftTemp())
				.collect(Collectors.toList());
		actualIntervalTemps.add(0, problemTable.getCascadeIntervals().get(0).getSourceShiftTemp());

		assertArrayEquals("Cascade interval temperature elements do not match.", expectedIntervalTemps,
				actualIntervalTemps.toArray());
		assertTrue("Cascade interval temperature elements are not found in string representation of problem table",
				Arrays.asList(expectedIntervalTemps).stream().map(it_f -> it_f.toString())
						.allMatch(it_str -> problemTable.toString().contains(it_str)));
	}

	@Test
	public void test_CascadeInterval_HeatLoads() {
		Object[] expectedHeatLoads = new Object[] { 1.8, 4.85, 0.1, -6.0, 0.4, -1.144, 5.09, -5.096, 14.0
				, -0.8 , -450.0, 0.5, -13.7, 0.0,  250.0 };
		List<Double> actualHeatLoads = problemTable.getCascadeIntervals().stream()
				.map(cI -> cI.getHeatLoad()).collect(Collectors.toList());

		assertArrayEquals("Cascade interval heat load elements do not match.", expectedHeatLoads,
				actualHeatLoads.toArray());
		assertTrue("Cascade interval heat load elements are not found in string representation of problem table",
				Arrays.asList(expectedHeatLoads).stream().map(hl_f -> hl_f.toString())
						.allMatch(hl_str -> problemTable.toString().contains(hl_str)));
	}

	@Test
	public void test_CascadeInterval_CascadingEnergies() {
		Object[] expectedCascadingEnergies = new Object[] { 451.8, 456.65, 456.75, 450.75, 451.15,
				450.006, 455.096, 450.0, 464.0, 463.2, 13.2, 13.7, 0.0, 0.0 , 250.0 };
		List<Double> actualCascadingEnergies = problemTable.getCascadeIntervals().stream()
				.map(cI -> cI.getCascadeEnergy()).collect(Collectors.toList());

		assertArrayEquals("Cascade interval cascading energy elements do not match.", expectedCascadingEnergies,
				actualCascadingEnergies.toArray());
		assertTrue("Cascade interval cascading energy elements are not found in string representation of problem table",
				Arrays.asList(expectedCascadingEnergies).stream().map(ce_f -> ce_f.toString())
						.allMatch(ce_str -> problemTable.toString().contains(ce_str)));
	}

	@Test
	public void test_CascadeInterval_Types() {
		Object[] expectedIntervalTypes = new String[] { "Stream Interval Containing: H1", "Stream Interval Containing: H1, H3", "Stream Interval Containing: H1"
				    , "Stream Interval Containing: C2, H1", "Stream Interval Containing: C2, H1, H2", "Stream Interval Containing: C1, C2, H1, H2", "Stream Interval Containing: C1, C2, H1, H2, H4"
				    , "Stream Interval Containing: C1, C2, H1, H2", "Stream Interval Containing: C1, H1, H2", "Stream Interval Containing: C1, H1"
				    , "Column Interval Containing: Reboiler of Column 1", "Stream Interval Containing: H1", "Stream Interval Containing: C3, H1", "Stream Interval Containing: ", "Column Interval Containing: Condenser of Column 1"};
		List<String> actualIntervalTypes = problemTable.getCascadeIntervals().stream()
				.map(cI -> cI.getType()).collect(Collectors.toList());
		
		assertArrayEquals(expectedIntervalTypes, actualIntervalTypes.toArray());
	}
	
	@Test
	public void test_PinchTemps() {
		assertEquals("Does not contain first shift pinch temperature.",
				(double)problemTable.getShiftPinchTemps().stream().sorted().toArray()[0], 15.0, 0.0001);
		assertEquals("Does not contain second shift pinch temperature.",
				(double)problemTable.getShiftPinchTemps().stream().sorted().toArray()[1], 30.0, 0.0001);

		assertEquals("Does not contain first unshifted pinch temperature.",
				(double)problemTable.getUnshiftedPinchTemps().stream().sorted().toArray()[0], 20.0, 0.0001);
		assertEquals("Does not contain second unshifted pinch temperature.",
				(double)problemTable.getUnshiftedPinchTemps().stream().sorted().toArray()[1], 25.0, 0.0001);
		assertEquals("Does not contain third unshifted pinch temperature.",
				(double)problemTable.getUnshiftedPinchTemps().stream().sorted().toArray()[2], 35.0, 0.0001);
		
		int pinchCount = 0;
		Matcher pinchMatcher = Pattern.compile("pinch", Pattern.CASE_INSENSITIVE).matcher(problemTable.toString());
		while(pinchMatcher.find())
			pinchCount++;
		
		assertEquals("Pinch count in string representation of problem table is incorrect.",2, pinchCount);
	}

	@Test
	public void test_Utilities() {
		assertEquals("MER hot utitlty (QH) is incorrect.", 450.0f, problemTable.getMERQH(), 0.0001f);
		assertTrue("MER hot utility (QH) is not found in string representation of problem table.", problemTable.toString().contains(String.valueOf(problemTable.getMERQH())));
		
		assertEquals("MER cold utitlty (QC) is incorrect.", 249.99998f, problemTable.getMERQC(), 0.0001f);
		assertTrue("MER cold utility (Qc) is not found in string representation of problem table.", problemTable.toString().contains(String.valueOf(problemTable.getMERQC())));
	}

}
