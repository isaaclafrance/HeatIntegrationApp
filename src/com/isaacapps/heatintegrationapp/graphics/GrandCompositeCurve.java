package com.isaacapps.heatintegrationapp.graphics;

import com.isaacapps.heatintegrationapp.internals.ProblemTable;;

public class GrandCompositeCurve extends LineGraph {
	private ProblemTable problemTable;
	private double[][] dataPoints;
	
	//
	public GrandCompositeCurve(ProblemTable problemTable){
		this.problemTable = problemTable;
		setupDataPoints();
	}
	
	//
	private void setupDataPoints(){
		int numOfDataPoints = problemTable.getCascadeIntervals().size();
		dataPoints = new double[numOfDataPoints][2];
		
		dataPoints[0][0] = problemTable.getCascadeIntervals().get(0).getTemp2();
		dataPoints[0][1] = problemTable.getMERQH();
		
		for(int i=1;i<numOfDataPoints; i++){
			dataPoints[i][0] = problemTable.getCascadeIntervals().get(i).getTemp2();
			dataPoints[i][1] = problemTable.getCascadeIntervals().get(i).getCascadeEnergy();
		}
	}
	
	public void updateGraph(){
		setupDataPoints();
	}
	
	//
	public double[][] getDataPoints(){
		return dataPoints;
	}
}
