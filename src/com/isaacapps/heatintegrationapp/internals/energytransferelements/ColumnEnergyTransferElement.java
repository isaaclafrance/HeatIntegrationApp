package com.isaacapps.heatintegrationapp.internals.energytransferelements;

public abstract class ColumnEnergyTransferElement extends EnergyTransferElement {
	private Column column;
	private String columnElementType;
	
	//
	public ColumnEnergyTransferElement(String columnElementType, double sourceTemp, double targetTemp, double heatLoad, double deltaTMin) throws DefinedPropertiesException {
		super(columnElementType+" ", sourceTemp, targetTemp, 0.0f, heatLoad, deltaTMin);
		this.columnElementType = columnElementType;
		this.name += ID;
	}
	public ColumnEnergyTransferElement(String columnElementType, double sourceTemp, double targetTemp, double heatLoad) throws DefinedPropertiesException {
		this(columnElementType, sourceTemp, targetTemp, heatLoad, 0.0f);
	}
	
	//
	@Override
	protected void calculateUnknownProperties() throws DefinedPropertiesException{
		//Override parent class implementation in order to prevents things from blowing up due to very small temperature difference.
		determineType();
		setShiftTemps(deltaTMin);
	}
	
	@Override
	protected void determineType(){
		super.determineType();
		if(sourceTemp<targetTemp){
			this.heatLoad = Math.abs(heatLoad);
		}
		else if(sourceTemp>targetTemp){
			this.heatLoad = -Math.abs(heatLoad);
		}
	}
	
	//
	void setColumn(Column column){
		this.column = column;
		name = columnElementType+" of "+column.getName();
	}
	public Column getColumn(){
		return column;
	}
	
	@Override
	public String getName(){
		//Use this expression instead of the 'name' property just in case column name changed some time after column reference was set 
		return columnElementType+" of "+column.getName(); 
	}
	
	@Override
	public String toString(){
		return String.format("\"%s\":%s", getName().split(" ")[0], super.toString().substring(super.toString().indexOf(":")+1));
	}
}
