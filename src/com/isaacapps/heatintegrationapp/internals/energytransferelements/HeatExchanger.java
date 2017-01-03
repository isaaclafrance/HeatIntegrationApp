package com.isaacapps.heatintegrationapp.internals.energytransferelements;

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
	public String getAreaWithUnit(){
		return getArea()+" "+getAreaUnit();
	}
	public void setArea(double area) throws DefinedPropertiesException{
		this.area = area;
		calculateUnknownProperties();
	}
	
	public String getAreaUnit(){
		return areaUnit;
	}
	
	/**
	 * Unlike base class this heat transfer coefficient is per area. 
	 * @return Heat transfer coefficient WITH a partial Log Mean Temperature Difference correction applied.
	 */
	@Override
	public double getHeatTransferCoeff(){
		return heatTransferCoeff / (double) Math.log(getSourceTemp()/getTargetShiftTemp());
	}
	
	/**
	 * Unlike base class this heat transfer coefficient is per area. 
	 * @return Heat transfer coefficient WITHOUT a partial Log Mean Temperature Difference correction applied.
	 */
	public double getHeatTransferCoeffWithoutLMTDCorrection(){
		return heatTransferCoeff;
	}
	@Override
	public void setHeatTransferCoeff(double heatTransferCoeffPerArea) throws DefinedPropertiesException{
		super.setHeatTransferCoeff(heatTransferCoeffPerArea);
	}
	
	@Override
	public String getHeatTransferCoeffUnit(){
		return getHeatUnit() +"/"+ getTempUnit() +"/"+ getAreaUnit();
	}
	
	/**
	 * Temperature difference between two counter-current or co-current streams at terminus.
	 */
	@Override
	public double getSourceTemp(){
		return super.getSourceTemp();
	}
	
	
	/**
	 * Temperature difference between two counter-current or co-current streams at terminus.
	 */
	@Override
	public double getTargetTemp(){
		return super.getTargetTemp();
	}
	
	
	//
	@Override
	public String toString(){
		String etElemObj = super.toString().substring(super.toString().indexOf(":")); 
		return String.format("\"heatExchange\": {%s, \"area\": %f, \"areaUnit\": %s}",etElemObj.substring(1, etElemObj.length()), getArea(), getAreaUnit());
	}
}
