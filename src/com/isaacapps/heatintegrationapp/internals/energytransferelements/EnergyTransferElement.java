package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class EnergyTransferElement {
	private static int numCount = 1;
	
	protected double heatLoad; // kW
	protected double sourceTemp, sourceShiftTemp, targetTemp, targetShiftTemp, deltaTMin; // K
	protected double heatTransferCoeff; // kW * K^-1
	protected String heatUnit, tempUnit;
	protected String name, type;
	protected final int ID;  
	
	//
	public EnergyTransferElement(String name, double sourceTemp, double targetTemp, double heatTransferCoeff, double heatLoad, double deltaTMin) throws DefinedPropertiesException{
		this.name = name;
		this.sourceTemp = sourceTemp;
		this.targetTemp = targetTemp;
		this.heatLoad = heatLoad;
		this.heatTransferCoeff = heatTransferCoeff;
		this.deltaTMin = deltaTMin;
		this.heatUnit = "kW";
		this.tempUnit = "K";
		
		calculateUnknownProperties();
			
		ID = numCount++;
	}
	public EnergyTransferElement(String name, double sourceTemp, double targetTemp, double heatTransferCoeff, double heatLoad) throws DefinedPropertiesException{
		this(name, sourceTemp, targetTemp, heatTransferCoeff, heatLoad, 0.0);
		name = name + ID ;
	}	
	public EnergyTransferElement(double sourceTemp, double targetTemp, double heatTransferCoeff, double heatLoad) throws DefinedPropertiesException{
		this("Energy Transfer Element ", sourceTemp, targetTemp, heatTransferCoeff, heatLoad, 0.0);
		name = name + ID ;
	}
	
	//
	protected void calculateUnknownProperties() throws DefinedPropertiesException{
		calculateUnknownProperties(name);
	}
	private void calculateUnknownProperties(String entityName) throws DefinedPropertiesException{
		//Zero is suitable to denote unspecified temperature because the unit is K and generally it is thermodynamically impossible to reach absolute zero.
		//Zero is suitable to denote unspecified heat because a zero heat stream would not be thermodynamically important and therefore would not considered in heat integration considerations.
		//Zero is suitable to denote unspecified heat transfer coeff because such a stream would not be thermodynamically important.
		MathContext mc = new MathContext(2, RoundingMode.UP);
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
			
			//For accuracy sake, BigDecimal needs to be used for calculations
			boolean state = new BigDecimal(sourceTemp).subtract(new BigDecimal(targetTemp).subtract(new BigDecimal(heatLoad).divide(new BigDecimal(getHeatTransferCoeff()),mc))).abs().compareTo(new BigDecimal(minDiff)) == -1 
							&& new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp).add(new BigDecimal(heatLoad).divide(new BigDecimal(getHeatTransferCoeff()),mc))).abs().compareTo(new BigDecimal(minDiff)) == -1 
						    && new BigDecimal(getHeatTransferCoeff()).subtract( new BigDecimal(heatLoad).divide(new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp)),mc).abs()).compareTo(new BigDecimal(minDiff)) == -1 
							&& new BigDecimal(heatLoad).subtract(new BigDecimal(getHeatTransferCoeff()).multiply(new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp)))).abs().compareTo(new BigDecimal(minDiff)) == -1 ;
				
			if(!state){
				throw new DefinedPropertiesException("Properties incorrectly defined. ", entityName);
			}
		}
		else{
			if(propStates[0]==0){
				setSourceTemp(new BigDecimal(targetTemp).subtract(new BigDecimal(heatLoad).divide(new BigDecimal(getHeatTransferCoeff()),mc)).doubleValue());
			}
			else if(propStates[1]==0){
				setTargetTemp(new BigDecimal(sourceTemp).add(new BigDecimal(heatLoad).divide(new BigDecimal(getHeatTransferCoeff()),mc)).doubleValue());
			}
			else if(propStates[2]==0){
				setHeatTransferCoeff( new BigDecimal(heatLoad).divide(new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp)),mc).abs().doubleValue());
			}
			else{
				heatLoad = new BigDecimal(getHeatTransferCoeff()).multiply(new BigDecimal(targetTemp).subtract(new BigDecimal(sourceTemp))).doubleValue();
			}
		}
		determineType();
		setShiftTemps(deltaTMin);
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
	}
	
	//	
	public String getType(){
		return type;
	}
	
	public double getSourceTemp(){
		return sourceTemp;
	}
	public String getSourceTempWithUnit(){
		return getSourceTemp() +" "+ getTempUnit();
	}
	public void setSourceTemp(double sourceTemp) throws DefinedPropertiesException{
		this.sourceTemp = sourceTemp;
		calculateUnknownProperties();
	}
	
	public double getTargetTemp(){
		return targetTemp;
	}
	public String getTargetTempWithUnit(){
		return getTargetTemp() +" "+ getTempUnit();
	}
	public void setTargetTemp(double targetTemp) throws DefinedPropertiesException{
		this.targetTemp = targetTemp;
		calculateUnknownProperties(); //Make sure newly set temp is correct based on existing properties
	}
	
	public double getHeatLoad(){
		return heatLoad;
	}
	public String getHeatLoadWithUnit(){
		return getHeatLoad() +" "+ heatUnit;
	}
	public void setHeatLoad(double heatLoad) throws DefinedPropertiesException{
		this.heatLoad = heatLoad;
		calculateUnknownProperties(); //Make sure newly set temp is correct based on existing properties
	}
	
	public void setShiftTemps(double deltaTMin){
		if(type.equals("Cold")){
			sourceShiftTemp = sourceTemp + deltaTMin/2;
			targetShiftTemp = targetTemp + deltaTMin/2;
		}else if(type.equals("Hot")){
			sourceShiftTemp = sourceTemp - deltaTMin/2;
			targetShiftTemp = targetTemp - deltaTMin/2;
		}
	}
	
	public double getSourceShiftTemp(){
		return sourceShiftTemp;
	}
	public String getSourceShiftTempWithUnit(){
		return getSourceTemp() +" "+ getTempUnit();
	}
	public double getTargetShiftTemp(){
		return targetShiftTemp;
	}
	public String getTargetShiftTempWithUnit(){
		return getTargetTemp() +" "+ getTempUnit();
	}
	
	public double getDeltaTMin(){
		return deltaTMin;
	}

	public double getHeatTransferCoeff(){
		return heatTransferCoeff;
	}
	public String getHeatTransferCoeffWithUnit(){
		return getHeatTransferCoeff() +" "+ getHeatTransferCoeffUnit();
	}
	public void setHeatTransferCoeff(double HeatTransferCoeff){
		this.heatTransferCoeff = HeatTransferCoeff;
		try {
			calculateUnknownProperties();
		} catch (DefinedPropertiesException e) {
			e.printStackTrace();
		}
	}
	
	public String getHeatUnit(){
		return heatUnit;
	}
	public String getTempUnit(){
		return tempUnit;
	}
	public String getHeatTransferCoeffUnit(){
		return getHeatUnit() + "/" + getTempUnit();
	}
	
	public String getName(){
		return name;
	}
	
	public int getID(){
		return ID;
	}
	
	//
	@Override
	public String toString(){
		return String.format("\"energyTransferElement\": {\"name\": \"%s\", \"ID\":%d \"type\": \"%s\", \"sourceTemp\": \"%s\", \"targetTemp\": \"%s\", \"enthalpyChange\": \"%s\", \"heatTransferCoeff\": \"%s\"}"
				             , name, ID, type, getSourceTempWithUnit(), getTargetTempWithUnit(), getHeatLoadWithUnit(), getHeatTransferCoeffWithUnit());
	}
}
