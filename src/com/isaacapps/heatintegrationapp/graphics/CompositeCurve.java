package com.heatIntegration.graphics;

import java.util.ArrayList;
import java.util.Arrays;

import com.heatIntegration.internals.ProblemTable;
import com.heatIntegration.internals.Stream;
import com.heatIntegration.internals.StreamProcess;

public class CompositeCurve extends LineGraph {
	private StreamProcess streamProcess;
	private ProblemTable problemTable;
	private float[][] coldStreamCurveDataPoints;
	private float[][] hotStreamCurveDataPoints;
	
	public CompositeCurve(StreamProcess streamProcess, ProblemTable problemTable){
		this.problemTable = problemTable;
		this.streamProcess = streamProcess;
	}
	
	private void setupDataPoints(){
		coldStreamCurveDataPoints = createStreamCurveDataPoints("Cold");
		hotStreamCurveDataPoints = createStreamCurveDataPoints("Hot");
		separateStreamCurvesByPinch();
	}
	
 	private float[][] createStreamCurveDataPoints(String streamType){ //Creates data points of enthalpy vs. temperature
		float[] tempIntervalVec = createStreamTempIntervals(streamType);
		float[][] streamCurveDataPoint = new float[tempIntervalVec.length][2];
		
		//Set first data point
		streamCurveDataPoint[0][0] = tempIntervalVec[0];
		streamCurveDataPoint[0][1] = 0.0f; 
		
		//Set remaining data point
		for(int i=0; i<tempIntervalVec.length-1; i++){
			//Assign temperatures
			streamCurveDataPoint[i][0] = tempIntervalVec[i];
			streamCurveDataPoint[i+1][0] = tempIntervalVec[i+1];			
			
			//Calculate net specific heat for each temperature interval			
			for(Stream stream:streamProcess.streams){
				if ( ((stream.inletTemp <= tempIntervalVec[i])&&(stream.outletTemp >= tempIntervalVec[i+1])) 
				    || ((stream.inletTemp >= tempIntervalVec[i])&&(stream.outletTemp <= tempIntervalVec[i+1])) ){
					
					streamCurveDataPoint[i][1] += 1/stream.cp; 
				}
			}
			
			//Calculate net enthalpy for each temperature interval	
			streamCurveDataPoint[i+1][1] = (streamCurveDataPoint[i+1][1] - streamCurveDataPoint[i][1])*(streamCurveDataPoint[i][0]);		
		}
		
		return streamCurveDataPoint;
	}	
	private float[] createStreamTempIntervals(String streamType){
		ArrayList<Float> tempVec = new ArrayList<Float>();	
		float[] tempVec_array;
		
		for(Stream stream:streamProcess.streams){
			//Hot Stream Curve Data Points			
			if(stream.getType().equals(streamType)){
				if(!tempVec.contains(stream.inletTemp)){
					tempVec.add(stream.inletTemp);
				}
				if(!tempVec.contains(stream.outletTemp)){
					tempVec.add(stream.outletTemp);
				}
			}
			tempVec_array = new float[tempVec.size()];
			for(int i=0; i<tempVec_array.length; i++){
				tempVec_array[i] = tempVec.get(i);
			}
			Arrays.sort(tempVec_array);			
		}	
		
		return tempVec_array;
	}
	private void separateStreamCurvesByPinch(){
		for(int i=0; i<coldStreamCurveDataPoints.length; i++){
			coldStreamCurveDataPoints[i][0] += coldStreamCurveDataPoints[i][0] + problemTable.qC;
		}
	}
	
	public void updateDataPoints(){
		setupDataPoints();
	}
}
