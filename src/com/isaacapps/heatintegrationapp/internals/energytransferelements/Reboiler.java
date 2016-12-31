package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;

public class Reboiler extends ColumnEnergyTransferElement {
	public Reboiler(double temp, double reboilerHeatLoad, double deltaTMin) throws DefinedPropertiesException {
		super("Reboiler", temp, temp + Column.UTILITY_TEMP_DIFF, reboilerHeatLoad, deltaTMin); //Treat reboilers as cold streams
	}
	public Reboiler(double temp, double reboilerHeatLoad) throws DefinedPropertiesException {
		this(temp, reboilerHeatLoad, 0.0f);
	}
}
