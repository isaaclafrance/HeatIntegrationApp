package com.isaacapps.heatintegrationapp.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.EnergyTransferElement;

public class EnergyTransferElementTest {

	@Test
	public void testCalculateUnknownPropertiesFails() {
		try {
			new EnergyTransferElement(1.0, 2.0, 3.0, 4.0);
			fail("No DefinedProperty exception was for heat transfer element defined with improper properties.");
		} catch (DefinedPropertiesException e) {
		}
	}

	@Test
	public void testCalculateUnknownPropertiesSucceeds() {
		try {
			assertEquals("Heat load improperly calculated for heat transfer element.", -2.0, new EnergyTransferElement(2.0, 1.0, 2.0, 0.0).getHeatLoad(), 0.000001);
			assertEquals("Heat transfer coeff improperly calculated for heat transfer element.", 2.0, new EnergyTransferElement(2.0, 1.0, 0.0, -2.0).getHeatTransferCoeff(), 0.000001);
			assertEquals("Target temperature improperly calculated for heat transfer element.", 1.0, new EnergyTransferElement(2.0, 0.0, 2.0, -2.0).getTargetTemp(), 0.000001);
			assertEquals("Source temperature improperly calulated for heat transfer element.", 2.0, new EnergyTransferElement(0.0, 1.0, 2.0, -2.0).getSourceTemp(), 0.000001);
		} catch (DefinedPropertiesException e) {
			fail("A DefinedProperty exception was thrown for heat transfer element with properly defined properties.");
		}
	}
	
	@Test
	public void testDetermineType() {
		try{
			assertEquals("Type 'Cold' is not determined", "Cold", new EnergyTransferElement(1.0, 2.0, 3.0, 0.0).getType());
			assertEquals("Type 'Hot' is not determined", "Hot", new EnergyTransferElement(2.0, 1.0, 3.0, 0.0).getType());
		} catch (DefinedPropertiesException e) {
			fail("A DefinedProperty exception was thrown for heat transfer element with properly defined properties.");
		}
	}

	@Test
	public void testSetShiftTemps() {
		try{
			EnergyTransferElement coldET =  new EnergyTransferElement(10.0, 20.0, 3.0, 0.0); coldET.setShiftTemps(10.0);
			EnergyTransferElement hotET =  new EnergyTransferElement(20.0, 10.0, 3.0, 0.0); hotET.setShiftTemps(10.0);
			
			assertEquals("Cold source shift temperature is incorrect", 15.0, coldET.getSourceShiftTemp(), 0.000001);
			assertEquals("Cold target shift temperature is incorrect", 25.0, coldET.getTargetShiftTemp(), 0.000001);

			assertEquals("Hot source shift temperature is incorrect", 15.0, hotET.getSourceShiftTemp(), 0.000001);
			assertEquals("Hot target shift temperature is incorrect", 5.0, hotET.getTargetShiftTemp(), 0.000001);
		} catch (DefinedPropertiesException e) {
			fail("A DefinedProperty exception was thrown for heat transfer element with properly defined properties.");
		}
	}

}
