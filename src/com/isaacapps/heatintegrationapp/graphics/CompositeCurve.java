package com.isaacapps.heatintegrationapp.graphics;

import java.util.List;
import java.util.stream.Collectors;

import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class CompositeCurve extends LineGraph {
	private List<Stream> streams;
	private double minHotUtility;
	private double[][] coldStreamCurveDataPoints;
	private double[][] hotStreamCurveDataPoints;
	
	//
	public CompositeCurve(List<Stream> streams, double minHotUtility){
		this.streams = streams;
		this.minHotUtility = minHotUtility;
		setupDataPoints();
	}
	
	//
	private void setupDataPoints(){
		coldStreamCurveDataPoints = createStreamCurveDataPoints("Cold");
		hotStreamCurveDataPoints = createStreamCurveDataPoints("Hot");
		separateStreamCurvesByPinch();
	}
	
 	private double[][] createStreamCurveDataPoints(String streamType){ //Creates data points of enthalpy vs. temperature
		double totalCP;	
 		List<Double> intervalTemps = createStreamIntervalTemps(streamType);
		double[][] streamCurveDataPoint = new double[][]{new double[intervalTemps.size()], new double[intervalTemps.size()]};
		
		for(int i=0; i<intervalTemps.size(); i++){
			streamCurveDataPoint[1][i] = intervalTemps.get(i);
		}
		
		//Set enthalpy reference point
		streamCurveDataPoint[0][0] = 0.0f; 
		
		for(int i=0; i<intervalTemps.size()-1; i++){	
			//Add up all the specific heats for streamsMap that cross interval
			totalCP = 0.0f;
			for(Stream stream:streams){ 
				if (stream.getType().equals(streamType) 
				    &&((stream.getSourceTemp() <= intervalTemps.get(i))&&(stream.getTargetTemp() >= intervalTemps.get(i+1)) 
				       ||(stream.getSourceTemp() >= intervalTemps.get(i))&&(stream.getTargetTemp() <= intervalTemps.get(i+1))) ){
					
					totalCP += stream.getHeatTransferCoeff(); 
				}
			}
			//Calculate net enthalpy for each temperature interval	
			streamCurveDataPoint[0][i+1] = totalCP * (streamCurveDataPoint[1][i+1] - streamCurveDataPoint[1][i]) + streamCurveDataPoint[0][i];		
		}
		
		return streamCurveDataPoint;
	}
	private List<Double> createStreamIntervalTemps(String streamType){
		return java.util.stream.Stream.concat(
				streams.stream().filter(s -> s.getType().equalsIgnoreCase(streamType)).map(s -> s.getSourceTemp()),
				streams.stream().filter(s -> s.getType().equalsIgnoreCase(streamType)).map(s -> s.getTargetTemp()))
				.distinct().sorted().collect(Collectors.toList());
	}
	private void separateStreamCurvesByPinch(){
		for(int i=0; i<coldStreamCurveDataPoints.length; i++){
			coldStreamCurveDataPoints[i][0] -= minHotUtility ;
		}
	}
	
	public void updateGraph(){
		setupDataPoints();
	}

	//
	public double[][] getColdStreamCurveDataPoints() {
		return coldStreamCurveDataPoints;
	}
	public double[][] getHotStreamCurveDataPoints() {
		return hotStreamCurveDataPoints;
	}
}
