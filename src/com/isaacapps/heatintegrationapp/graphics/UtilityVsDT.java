package com.isaacapps.heatintegrationapp.graphics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import com.isaacapps.heatintegrationapp.internals.ProblemTable;

public class UtilityVsDT extends LineGraph {
	private Map<String,double[][]> dataPoints; //Key value of map used as legend on graph.
	private ProblemTable problemTable;
	private double[] deltaTMinBounds;
	private int numOfPoints;
	
	public UtilityVsDT(ProblemTable problemTable, double deltaTMinLowerBound, double deltaTMinUpperBound, int numOfPoints){
		this.problemTable = problemTable;
		this.deltaTMinBounds = new double[] {deltaTMinLowerBound, deltaTMinUpperBound};
		this.numOfPoints = numOfPoints;	
		dataPoints = new HashMap<String, double[][]>();
		setupDataPoints();
	}
	public UtilityVsDT(ProblemTable problemTable){
		this(problemTable, problemTable.getDeltaTMin(), problemTable.getDeltaTMin()+50.0, 50);
	}
		
	//
	private void setupDataPoints(){
		double[] dtmXValues = calculateDtmXValues();	
		dataPoints.put("Hot Utility", createHotUtilityDataPoints(dtmXValues));
		dataPoints.put("Cold Utility", createColdUtilityDataPoints(dtmXValues));
	}
	
	private double[] calculateDtmXValues(){
		//Create delta T Min x values based on range and number of points specifications.
		return   DoubleStream.iterate(deltaTMinBounds[0], d -> d + (deltaTMinBounds[1] - deltaTMinBounds[0])/numOfPoints)
							 .limit(numOfPoints)
							 .toArray();
	}
	
	private double[][] createHotUtilityDataPoints(double[] dtmXValues){
		return createGeneralUtilityDataPoints(dtmXValues, "Hot");
	}
	private double[][] createColdUtilityDataPoints(double[] dtmXValues){
		return createGeneralUtilityDataPoints(dtmXValues, "Cold");
	}
	private double[][] createGeneralUtilityDataPoints(double[] dtmXValues, String utilType){
		//Lambda function that calculates Minimum Energy Requirement Utility heat for each delta T min input
		DoubleUnaryOperator calcUtil = dT -> {problemTable.setDeltaTMin(dT); 
		                                           return (utilType.equals("Hot")?problemTable.getMERQH():problemTable.getMERQC());};
		 
		//Data set of utility heat for every Delta T Min.
		return new double[][]{ dtmXValues, Arrays.stream(dtmXValues).map(calcUtil).toArray()};
	}
		
	public void updateGraph(){
		setupDataPoints();
	}
	
	//
	public double[][] getHotUtilityDataPoints(){
		return dataPoints.get("Hot Utility");
	}
	public double[][] getColdUtilityDataPoints(){
		return dataPoints.get("Cold Utility");
	}

	public void setDeltaTMinBounds(double leftBound, double rightBound){
		deltaTMinBounds[0] = leftBound;
		deltaTMinBounds[1] = rightBound;
		setupDataPoints();
	}
	public double[] getDeltaTMinBOunds(){
		return deltaTMinBounds;
	}
	
	public int getNumOfPoints(){
		return numOfPoints;
	}
	public void setNumOfPoints(int numOfPoints){
		this.numOfPoints = numOfPoints;
		setupDataPoints();
	}
}
