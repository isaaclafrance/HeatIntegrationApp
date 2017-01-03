package com.isaacapps.heatintegrationapp.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.isaacapps.heatintegrationapp.graphics.EnergyCascadeDiagram;
import com.isaacapps.heatintegrationapp.internals.ProblemTable;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Column;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class EnergyCascadeDiagramTest {
	EnergyCascadeDiagram ecDiagram;
	
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

		ecDiagram = new EnergyCascadeDiagram(new ProblemTable(streams, columns, deltaTMin));
		
	}

	@Test
	public void test_EnergyCascade_Temperatures() {
		String[] expectedIntervalTemps = new String[]{"265.0 K", "247.0 K", "246.0 K", "245.0 K", "215.0 K", "195.0 K", "187.85 K"
				,"186.85 K", "155.0 K", "55.0 K", "45.0 K", "45.0 K", "40.0 K", "30.0 K", "15.0 K", "15.0 K" };

		assertTrue("Cascade interval temperature elements are not found in the printed representation of energy cascade diagram.",
							Arrays.stream(expectedIntervalTemps).allMatch(it_str -> ecDiagram.toString().contains(it_str)));
	}

	@Test
	public void test_EnergyCascade_HeatLoads() {
		String[] expectedHeatLoads = new String[]{ "1.8 kW", "4.85 kW", "0.1 kW", "-6.0 kW", "0.4 kW", "-1.144 kW", "5.09 kW", "-5.096 kW", "14.0 kW"
				                                        , "-0.8 kW" , "-450.0 kW", "0.5 kW", "-13.7 kW", "0.0 kW",  "250.0 kW"};

		assertTrue("Cascade interval heat load elements are not found in the printed representation of energy cascade diagram.",
							Arrays.stream(expectedHeatLoads).allMatch(hl_str -> ecDiagram.toString().contains(hl_str)));
	}

	@Test
	public void test_EnergyCascade_CascadingEnergies() {
		String[] expectedCascadingEnergies = new String[] { "451.8 kW", "456.65 kW", "456.75 kW", "450.75 kW", "451.15 kW",
				"450.006 kW", "455.096 kW", "450.0 kW", "464.0 kW", "463.2 kW", "13.2 kW", "13.7 kW", "0.0 kW", "0.0 kW" , "250.0 kW" };

		assertTrue("Cascade interval cascading energy elements are not found in the printed representation of energy cascade diagram.",
							Arrays.stream(expectedCascadingEnergies).allMatch(ce_str -> ecDiagram.toString().contains(ce_str)));
	}

	@Test
	public void test_EnergyCascade_Types() {
		String[] expectedIntervalTypes = new String[] { "Stream Interval Containing: H1", "Stream Interval Containing: H1, H3", "Stream Interval Containing: H1"
				    , "Stream Interval Containing: C2, H1", "Stream Interval Containing: C2, H1, H2", "Stream Interval Containing: C1, C2, H1, H2", "Stream Interval Containing: C1, C2, H1, H2, H4"
				    , "Stream Interval Containing: C1, C2, H1, H2", "Stream Interval Containing: C1, H1, H2", "Stream Interval Containing: C1, H1"
				    , "Column Interval Containing: Reboiler of Column 1", "Stream Interval Containing: H1", "Stream Interval Containing: C3, H1", "Stream Interval Containing: ", "Column Interval Containing: Condenser of Column 1"};
		
		assertTrue("Cascade interval type elements are not found in the printed representation of energy cascade diagram.",
							Arrays.stream(expectedIntervalTypes).allMatch(ct_str -> ecDiagram.toString().contains(ct_str)));
	}
	
	@Test
	public void test_EnergyCascade_PinchTemps() {
		int pinchCount = 0;
		Matcher pinchMatcher = Pattern.compile("pinch", Pattern.CASE_INSENSITIVE).matcher(ecDiagram.toString());
		while(pinchMatcher.find())
			pinchCount++;
		
		assertEquals("Pinch count in string representation of energy cascade diagram is incorrect.",2, pinchCount);
	}

	@Test
	public void test_EnegryCascade_Utilities() {
		assertTrue("MER hot utility (QH) is not found in string representation of energy cascade diagram.", ecDiagram.toString().contains("450.0 kW"));
		assertTrue("MER cold utility (Qc) is not found in string representation of energy cascade diagram.", ecDiagram.toString().contains("250.0 kW"));
	}

}
