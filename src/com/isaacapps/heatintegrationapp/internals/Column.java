package com.isaacapps.heatintegrationapp.internals;

public class Column {
	private String colName;
	private int colNum;
	private float rebTemp; // K
	private float rebShiftTemp;
	private float rebHeatLoad; // kW 
	private float condTemp; // K
	private float condShiftTemp;
	private float condHeatLoad; // kW
	static int colCount = 0;

	public Column(String colName, float rebTemp, float rebHeatLoad, float condTemp, float condHeatLoad){
		this.colName = colName;
		this.colNum = colCount++;
		this.rebTemp = rebTemp;
		this.rebHeatLoad = rebHeatLoad;
		this.condTemp = condTemp;
		this.condHeatLoad = condHeatLoad;
		
		if(rebTemp==0.0f ||condTemp==0.0f){
			int[] propStates = new int[]{(rebTemp==0.0f)?1:0,
										(condTemp==0.0f)?1:0};
			notEnoughData(propStates);
		}
	}
	public Column(int colNum, float rebTemp, float rebHeatLoad, float condTemp, float condHeatLoad){
		this(String.valueOf(colNum), rebTemp, rebHeatLoad, condTemp, condHeatLoad);
	}
	
	private void notEnoughData(int[] propStates){
		System.out.println("Column has insufficient # of defined properties.");
	}
	
	public String getColName(){
		return colName;
	}
	public int getColNum(){
		return colNum;
	}
	
	public float getRebTemp() {
		return rebTemp;
	}
	public float getRebHeatLoad() {
		return rebHeatLoad;
	}
	public float getCondTemp() {
		return condTemp;
	}
	public float getCondHeatLoad() {
		return condHeatLoad;
	}
	
	public float getRebShiftTemperature(){
		return rebShiftTemp;
	}
	public float getCondShiftTemperature(){
		return condShiftTemp;
	}
	
	public void setRebShiftTemperature(float deltaT){
		rebShiftTemp = rebTemp + deltaT/2;
	}
	public void setCondShiftTemperature(float deltaT){
		condShiftTemp = condTemp - deltaT/2;
	}
	
	public String toString(){
		return String.format("{Column Name: %s, Column Num: %d, Reb Temp: %f K, Reb Load: %f kW, Cond Temp: %f K, Cond Load: %f kW }", colName, colNum, rebTemp, rebHeatLoad, condTemp, condHeatLoad);
	}
}
