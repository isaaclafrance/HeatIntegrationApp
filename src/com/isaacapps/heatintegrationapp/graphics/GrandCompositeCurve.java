package com.isaacapps.heatintegrationapp.graphics;

import java.util.HashMap;
import java.util.Map;

import com.isaacapps.heatintegrationapp.internals.ProblemTable;;

public class GrandCompositeCurve extends LineGraph {
	private ProblemTable problemTable;
	private Map<String, double[][]> dataPoints; //Key value of map used as legend on graph.
	
	//
	public GrandCompositeCurve(ProblemTable problemTable){
		this.problemTable = problemTable;
		dataPoints = new HashMap<String, double[][]>();
		setupDataPoints();
	}
	
	//
	private void setupDataPoints(){
		dataPoints.put("GCC", createGrandCompositeCurveDataPoints());
	}
	
	private double[][] createGrandCompositeCurveDataPoints(){
		int numOfDataPoints = problemTable.getCascadeIntervals().size();
		double[][] dataPoints = new double[2][numOfDataPoints];
		
		dataPoints[0][0] = problemTable.getCascadeIntervals().get(0).getSourceShiftTemp();
		dataPoints[1][0] = problemTable.getMERQH();
		
		for(int i=1;i<numOfDataPoints; i++){
			dataPoints[0][i] = problemTable.getCascadeIntervals().get(i).getTargetShiftTemp();
			dataPoints[1][i] = problemTable.getCascadeIntervals().get(i).getCascadeEnergy();
		}
		
		return dataPoints;
	}
	
	public void updateGraph(){
		setupDataPoints();
	}
	
	//
	public double[][] getDataPoints(){
		return dataPoints.get("GCC");
	}
}
