package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;

public class HeatExchanger extends EnergyTransferElement {
	private double area;
	private String areaUnit;
	
	//
	public HeatExchanger(String name, double sourceTemp, double targetTemp, double cp, double heatLoad)
			throws DefinedPropertiesException {
		super(name, sourceTemp, targetTemp, cp, heatLoad);
	}
	
	public HeatExchanger(double sourceTemp, double targetTemp, double cp, double heatLoad)
			throws DefinedPropertiesException {
		this("Heat Exchanger ", sourceTemp, targetTemp, cp, heatLoad);
		name += ID;
	}
	
	//
	@Override
	protected void determineType(){
		type = "Heat Exchanger";
	}
	

	//
	public double getArea(){
		return area;
	}
	public void setArea(double area) throws DefinedPropertiesException{
		this.area = area;
		calculateUnknownProperties();
	}
	
	@Override
	public double getHeatTransferCoeff(){
		//Unlike base class this heat transfer coefficient is per area. Unit is kW/K/M^2 
		//Add correction log mean temperature difference
		return heatTransferCoeff / (double) Math.log(Math.abs(getSourceTemp() - getTargetShiftTemp()));
	}
	public double getHeatTransferCoeffWithoutLMTDCorrection(){
		//Unlike base class this heat transfer coefficient is per area. 
		return heatTransferCoeff;
	}
	@Override
	public void setHeatTransferCoeff(double heatTransferCoeffPerArea) throws DefinedPropertiesException{
		super.setHeatTransferCoeff(heatTransferCoeffPerArea);
	}
	
	@Override
	public double getSourceTemp(){
		//Temperature difference between two counter-current or co-current streams at terminus.
		return super.getSourceTemp();
	}
	
	@Override
	public double getTargetTemp(){
		//Temperature difference between two counter-current or co-current streams at terminus.
		return super.getTargetTemp();
	}
	
	@Override
	public void setShiftTemps(double deltaTMin){
		
	}
	
	//
	@Override
	public String toString(){
		String etElemObj = super.toString().substring(super.toString().indexOf(":")); 
		return String.format("\"heatExchange\": {%s, \"area\": %f, \"areaUnit\": %s}",etElemObj.substring(1, etElemObj.length()), area, areaUnit);
	}
}
