package com.isaacapps.heatintegrationapp.graphics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class CompositeCurve extends LineGraph {
	private List<Stream> streams;
	private double minHotUtility;
	private Map<String, double[][]> dataPoints; //Key value of map used as legend on graph.
	
	/**
	 * Creates data points of enthalpy vs. temperature
	 * @param streams
	 * @param minHotUtility
	 */
	public CompositeCurve(List<Stream> streams, double minHotUtility){
		this.streams = streams;
		this.minHotUtility = minHotUtility;
		dataPoints = new HashMap<String, double[][]>();
		setupDataPoints();
	}
	
	//
	private void setupDataPoints(){
		dataPoints.put("Cold Stream Curve",createStreamCurveDataPoints("Cold"));
		dataPoints.put("Hot Stream Curve", createStreamCurveDataPoints("Hot"));
		separateStreamCurvesByPinch();
	}
	
 	private double[][] createStreamCurveDataPoints(String streamType){ 
		double totalCP;	
 		List<Double> intervalTemps = createStreamIntervalTemps(streamType);
		double[][] streamCurveDataPoint = new double[][]{new double[intervalTemps.size()], new double[intervalTemps.size()]};
		
		//Set enthalpy reference point
		streamCurveDataPoint[0][0] = 0.0; 
		streamCurveDataPoint[1][0] = intervalTemps.get(0); 
		
		for(int i=1; i<intervalTemps.size(); i++){	
			//Add up all the specific heats for streams that cross interval
			final int j = i; //Lambda functions can only use final local variables.
			totalCP = streams.stream().filter(s->s.getType().equalsIgnoreCase(streamType)&&streamCrossesInterval(s, intervalTemps.get(j), intervalTemps.get(j-1)))
					                  .mapToDouble(s -> s.getHeatTransferCoeff())
					                  .sum();
			
			//Calculate net enthalpy for each temperature interval	
			streamCurveDataPoint[0][i] = totalCP * (streamCurveDataPoint[1][i] - streamCurveDataPoint[1][i-1]) + streamCurveDataPoint[0][i-1];	
			
			//Set Temperature data points
			streamCurveDataPoint[1][i] = intervalTemps.get(i);
		}
		
		return streamCurveDataPoint;
	}
 	private boolean streamCrossesInterval(Stream stream, double temp1, double temp2){
 		return (stream.getSourceTemp() <= temp1)&&(stream.getTargetTemp() >= temp2) 
					       ||(stream.getSourceTemp() >= temp1)&&(stream.getTargetTemp() <= temp2);
 	}
 	
	private List<Double> createStreamIntervalTemps(String streamType){
		return java.util.stream.Stream.concat(
				streams.stream().filter(s -> s.getType().equalsIgnoreCase(streamType)).map(s -> s.getSourceTemp()),
				streams.stream().filter(s -> s.getType().equalsIgnoreCase(streamType)).map(s -> s.getTargetTemp()))
				.distinct().sorted().collect(Collectors.toList());
	}
	private void separateStreamCurvesByPinch(){
		for(int i=0; i<dataPoints.get("Cold Stream Curve").length; i++){
			dataPoints.get("Cold Stream Curve")[0][i] -= minHotUtility ;
		}
	}
	
	public void updateGraph(){
		setupDataPoints();
	}

	//
	public double[][] getColdStreamCurveDataPoints() {
		return dataPoints.get("Cold Stream Curve");
	}
	public double[][] getHotStreamCurveDataPoints() {
		return dataPoints.get("Hot Stream Curve");
	}
}
