package com.heatIntegration.internals;

public class Column {
	private String colName;
	private int colNum;
	public float rebTemp; // K
	public float rebHeatLoad; // kW 
	public float condTemp; // K
	public float condHeatLoad; // kW

	public Column(String colName, int colNum, float rebTemp, float rebHeatLoad, float condTemp, float condHeatLoad){
		this.colName = colName;
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
		this(String.valueOf(colNum), colNum, rebTemp, rebHeatLoad, condTemp, condHeatLoad);
	}
	
	private void notEnoughData(int[] propStates){
		System.out.println("Column has insufficient # defined properties.");
	}
	
	public String getColName(){
		return colName;
	}
	public int getColNum(){
		return colNum;
	}
}
