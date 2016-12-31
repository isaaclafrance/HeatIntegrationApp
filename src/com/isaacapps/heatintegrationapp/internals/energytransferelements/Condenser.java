package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;

public class Condenser extends ColumnEnergyTransferElement {	
	public Condenser(double temp, double heatLoad, double deltaTMin) throws DefinedPropertiesException {
		super("Condenser", temp + Column.UTILITY_TEMP_DIFF, temp, heatLoad, deltaTMin); //Treat condensers as hot streams
	}
	public Condenser(double temp, double heatLoad) throws DefinedPropertiesException {
		this(temp, heatLoad, 0.0f);
	}
}
