package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;

public class Condenser extends EnergyTransferElement {
	private Column column;
	
	public Condenser(double temp, double heatLoad, double deltaTMin) throws DefinedPropertiesException {
		super("Condenser ", temp + Column.UTILITY_TEMP_DIFF, temp, 0.0f, heatLoad, deltaTMin); //Treat condensers as hot streams
		name += num;
	}
	public Condenser(double temp, double heatLoad) throws DefinedPropertiesException {
		this(temp, heatLoad, 0.0f);
	}
	
	//
	protected void calculateUnknownProperties() throws DefinedPropertiesException{
		
	}
	
	//
	void setColumn(Column column){
		this.column = column;
		name = "Condenser of "+column.getName();
	}
	
	public Column getColumn(){
		return column;
	}
	
	@Override
	public String getName(){
		return "Condenser of "+column.getName();
	}
	
	@Override
	public String toString(){
		return String.format("\"condenser\":%s",  super.toString().substring(super.toString().indexOf(":")+1));
	}
}
