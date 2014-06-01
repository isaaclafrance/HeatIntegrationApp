package com.heatIntegration.graphics;

import com.heatIntegration.internals.ProblemTable;;

public class GrandCompositeCurve extends LineGraph {
	private ProblemTable problemTable;
	private float[][] dataPoints;
	
	public GrandCompositeCurve(ProblemTable problemTable){
		this.problemTable = problemTable;
		createDataPoints();
	}
	
	private void createDataPoints(){
		int numOfDataPoints = problemTable.cascadeIntervals.size()+1;
		dataPoints = new float[numOfDataPoints][3];
		
		for(int i=0;i<numOfDataPoints-1; i++){
			dataPoints[i][0] = problemTable.cascadeIntervals.get(i).temp1;
			dataPoints[i][1] = problemTable.cascadeIntervals.get(i).heatLoad;
		}
		dataPoints[numOfDataPoints-1][0] = problemTable.cascadeIntervals.get(numOfDataPoints-1).temp2;
		dataPoints[numOfDataPoints-1][1] = problemTable.cascadeIntervals.get(numOfDataPoints-1).cascadeEnergy;
	}
	
	public void updateDataPoints(){
		createDataPoints();
	}
}
