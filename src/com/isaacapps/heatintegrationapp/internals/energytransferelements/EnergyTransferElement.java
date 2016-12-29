package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import java.math.BigDecimal;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;

public class EnergyTransferElement {
	private double heatLoad; // kW
	private double sourceTemp, sourceShiftTemp, targetTemp, targetShiftTemp, deltaTMin; // K
	protected double heatTransferCoeff; // kW * K^-1
	protected String name, heatLoadUnit, tempUnit,  type;
	private static int numCount = 1;
	protected final int num;  
	
	//
	public EnergyTransferElement(String name, double sourceTemp, double targetTemp, double heatTransferCoeff, double heatLoad, double deltaTMin) throws DefinedPropertiesException{
		this.name = name;
		this.sourceTemp = sourceTemp;
		this.targetTemp = targetTemp;
		this.heatLoad = heatLoad;
		this.heatTransferCoeff = heatTransferCoeff;
		heatLoadUnit = "kW";
		tempUnit = "kW/K";
		
		calculateUnknownProperties();
		determineType();
		
		setShiftTemps(deltaTMin);
		
		num = numCount++;
	}
	public EnergyTransferElement(String name, double sourceTemp, double targetTemp, double cp, double heatLoad) throws DefinedPropertiesException{
		this(name, sourceTemp, targetTemp, cp, heatLoad, 0.0f);
		name = name + num ;
	}	
	public EnergyTransferElement(double sourceTemp, double targetTemp, double cp, double heatLoad) throws DefinedPropertiesException{
		this("Energy Transfer Element ", sourceTemp, targetTemp, cp, heatLoad, 0.0f);
		name = name + num ;
	}
	
	//
	protected void calculateUnknownProperties() throws DefinedPropertiesException{
		calculateUnknownProperties(name);
	}
	private void calculateUnknownProperties(String entityName) throws DefinedPropertiesException{
		int[] propStates = new int[]{(sourceTemp==0.0f)?0:1,
									 (targetTemp==0.0f)?0:1,
									 (getHeatTransferCoeff()==0.0f)?0:1,
									 (heatLoad==0.0f)?0:1}; 
		
		int propStatesSum = propStates[0] + propStates[1] + propStates[2] + propStates[3];
		
		if(propStatesSum < 3){
			throw new DefinedPropertiesException("Has insufficient # defined properties.", entityName);
		}
		else if(propStatesSum == 4){
			double minDiff = 0.000001 ;
			
			boolean state = new BigDecimal(sourceTemp).subtract(new BigDecimal(targetTemp).subtract(new BigDecimal(heatLoad).divide(new BigDecimal(getHeatTransferCoeff())))).abs().doubleValue() < minDiff  
							&& new BigDecimal(targetTemp).subtract(new BigDecimal(targetTemp).add(new BigDecimal(heatLoad).divide(new BigDecimal(getHeatTransferCoeff())))).abs().doubleValue() < minDiff
						    && new BigDecimal(getHeatTransferCoeff()).subtract( new BigDecimal(heatLoad).divide(new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp))).abs()).abs().doubleValue() < minDiff
							&& new BigDecimal(heatLoad).subtract(new BigDecimal(getHeatTransferCoeff()).multiply(new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp)))).abs().doubleValue() < minDiff;
				
			if(!state){
				throw new DefinedPropertiesException("Properties incorrectly defined. ", entityName);
			}
		}
		else{
			if(propStates[0]==0){
				sourceTemp = new BigDecimal(targetTemp).subtract(new BigDecimal(heatLoad).divide(new BigDecimal(getHeatTransferCoeff()))).doubleValue();
			}
			else if(propStates[1]==0){
				targetTemp = new BigDecimal(targetTemp).add(new BigDecimal(heatLoad).divide(new BigDecimal(getHeatTransferCoeff()))).doubleValue();
			}
			else if(propStates[2]==0){
				setHeatTransferCoeff( new BigDecimal(heatLoad).divide(new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp))).abs().doubleValue());
			}
			else{
				heatLoad = new BigDecimal(getHeatTransferCoeff()).multiply(new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp))).doubleValue();
			}
		}
	}
	protected void determineType(){
		if(sourceTemp<targetTemp){
			type = "Cold";
			this.heatLoad = Math.abs(heatLoad);
		}
		else if(sourceTemp>targetTemp){
			type = "Hot";
			this.heatLoad = -Math.abs(heatLoad);
		}
		else{
			type = "Utility";
		}
	}
	
	//	
	public String getType(){
		return type;
	}
	
	public double getSourceTemp(){
		return sourceTemp;
	}
	public void setSourceTemp(double sourceTemp) throws DefinedPropertiesException{
		this.sourceTemp = sourceTemp;
		calculateUnknownProperties();
		determineType();
	}
	
	public double getTargetTemp(){
		return targetTemp;
	}
	public void setTargetTemp(double targetTemp) throws DefinedPropertiesException{
		this.targetTemp = targetTemp;
		calculateUnknownProperties();
		determineType();
	}
	
	public double getHeatLoad(){
		return heatLoad;
	}
	public void setHeatLoad(double heatLoad) throws DefinedPropertiesException{
		this.heatLoad = heatLoad;
		calculateUnknownProperties();
		determineType();
	}
	
	public void setShiftTemps(double deltaTMin){
		if(type.equals("Cold")){
			sourceShiftTemp = sourceTemp + deltaTMin/2;
			targetShiftTemp = targetTemp + deltaTMin/2;
		}else{
			sourceShiftTemp = sourceTemp - deltaTMin/2;
			targetShiftTemp = targetTemp - deltaTMin/2;
		}
	}
	
	public double getSourceShiftTemp(){
		return sourceShiftTemp;
	}
	public double getTargetShiftTemp(){
		return targetShiftTemp;
	}
	
	public double getDeltaTMin(){
		return deltaTMin;
	}

	public double getHeatTransferCoeff(){
		return heatTransferCoeff;
	}
	public void setHeatTransferCoeff(double HeatTransferCoeff) throws DefinedPropertiesException{
		this.heatTransferCoeff = HeatTransferCoeff;
		calculateUnknownProperties();
	}
	
	public String getName(){
		return name;
	}
	
	//
	@Override
	public String toString(){
		return String.format("\"energyTransferElement\":{\"name\": \"%s\", \"num\": %d, \"type\": \"%s\", \"sourceTemp\": %f, \"targetTemp\": %f, \"enthalpyChange\": %f, \"heatTransferCoeff\": %f, \"tempUnit\": \"%s\", \"heatLoadUnit\": \"%s\"}"
				             , name, num, type, sourceTemp, targetTemp, heatLoad, heatTransferCoeff, tempUnit, heatLoadUnit);
	}
}
