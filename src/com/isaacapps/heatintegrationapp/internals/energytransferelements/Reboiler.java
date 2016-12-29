package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;

public class Reboiler extends EnergyTransferElement {
	private Column column;
	
	//
	public Reboiler(double temp, double reboilerHeatLoad, double deltaTMin) throws DefinedPropertiesException {
		super("Reboiler ", temp, temp + Column.UTILITY_TEMP_DIFF, 0.0f, reboilerHeatLoad, deltaTMin); //Treat reboilers as cold streams
		name += num;
	}
	public Reboiler(double temp, double reboilerHeatLoad) throws DefinedPropertiesException {
		this(temp, reboilerHeatLoad, 0.0f);
	}
	
	//
	protected void calculateUnknownProperties() throws DefinedPropertiesException{
		
	}
	
	//
	void setColumn(Column column){
		this.column = column;
		name = "Reboiler of "+column.getName();
	}
	
	public Column getColumn(){
		return column;
	}
	
	@Override
	public String getName(){
		return "Reboiler of "+column.getName();
	}
	
	@Override
	public String toString(){
		return String.format("\"reboiler\":%s",  super.toString().substring(super.toString().indexOf(":")+1));
	}
}
