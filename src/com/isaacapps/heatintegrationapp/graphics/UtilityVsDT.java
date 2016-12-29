package com.isaacapps.heatintegrationapp.graphics;

import com.isaacapps.heatintegrationapp.internals.ProblemTable;

public class UtilityVsDT extends LineGraph {
	private double[][] dataPoints;
	private ProblemTable problemTable;
	private double deltaTMinRange;
	
	public UtilityVsDT(ProblemTable problemTable, double deltaTMinRange){
		this.problemTable = problemTable;
		this.deltaTMinRange = deltaTMinRange;
	}
	
	//
	private void setupDataPoints(){
	}
	
	public void updateGraph(){
		setupDataPoints();
	}
	
	//
	public double[][] getDataPoints(){
		return dataPoints;
	}
	public double getDeltaTMinRange(){
		return deltaTMinRange;
	}
}
