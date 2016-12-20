package com.isaacapps.heatintegrationapp.graphics;

import java.util.ArrayList;
import java.util.Collections;

import com.isaacapps.heatintegrationapp.internals.Stream;
import com.isaacapps.heatintegrationapp.internals.StreamProcess;

public class CompositeCurve extends LineGraph {
	private StreamProcess streamProcess;
	private float[][] coldStreamCurveDataPoints;
	private float[][] hotStreamCurveDataPoints;
	
	public CompositeCurve(StreamProcess streamProcess){
		this.streamProcess = streamProcess;
		setupDataPoints();
	}
	
	private void setupDataPoints(){
		coldStreamCurveDataPoints = createStreamCurveDataPoints("Cold");
		hotStreamCurveDataPoints = createStreamCurveDataPoints("Hot");
		separateStreamCurvesByPinch();
	}
	
 	private float[][] createStreamCurveDataPoints(String streamType){ //Creates data points of enthalpy vs. temperature
		float[] tempIntervalVec = createStreamTempIntervals(streamType);
		float[][] streamCurveDataPoint = new float[][]{new float[tempIntervalVec.length], createStreamTempIntervals(streamType)};
		float totalCP;
		
		//Set enthalpy reference point
		streamCurveDataPoint[0][0] = 0.0f; 
		
		for(int i=0; i<tempIntervalVec.length-1; i++){	
			//Add up all the specific heats for streams that cross interval
			totalCP = 0.0f;
			for(Stream stream:streamProcess.streams.values()){ 
				if (stream.getType().equals(streamType) 
				    &&((stream.getInletTemp() <= tempIntervalVec[i])&&(stream.getOutletTemp() >= tempIntervalVec[i+1]) 
				       ||(stream.getInletTemp() >= tempIntervalVec[i])&&(stream.getOutletTemp() <= tempIntervalVec[i+1])) ){
					
					totalCP += stream.getCP(); 
				}
			}
			//Calculate net enthalpy for each temperature interval	
			streamCurveDataPoint[0][i+1] = totalCP * (streamCurveDataPoint[1][i+1] - streamCurveDataPoint[1][i]) + streamCurveDataPoint[0][i];		
		}
		
		return streamCurveDataPoint;
	}	
	private float[] createStreamTempIntervals(String streamType){
		ArrayList<Float> tempVec = new ArrayList<Float>();
		float[] tempVec_array;
		
		for(Stream stream:streamProcess.streams.values()){		
			if(stream.getType().equals(streamType)){
				if(!tempVec.contains(stream.getInletTemp())){
					tempVec.add(stream.getInletTemp());
				}
				if(!tempVec.contains(stream.getOutletTemp())){
					tempVec.add(stream.getOutletTemp());
				}
			}
		}	
		
		Collections.sort(tempVec);	
		tempVec_array = new float[tempVec.size()];
		for(int i=0;i<tempVec.size();i++){
			tempVec_array[i] = tempVec.get(i);
		}	
		
		return tempVec_array;
	}
	private void separateStreamCurvesByPinch(){
		for(int i=0; i<coldStreamCurveDataPoints.length; i++){
			coldStreamCurveDataPoints[i][0] += coldStreamCurveDataPoints[i][0] + (streamProcess.getProblemTable().getQH() - streamProcess.getInitialHotUtility()) ;
		}
	}
	
	public void updateGraph(){
		setupDataPoints();
	}

	//
	public float[][] getColdStreamCurveDataPoints() {
		return coldStreamCurveDataPoints;
	}
	public float[][] getHotStreamCurveDataPoints() {
		return hotStreamCurveDataPoints;
	}
}
