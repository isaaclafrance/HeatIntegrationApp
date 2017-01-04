package com.isaacapps.heatintegrationapp.graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static java.util.stream.Collectors.*;

import com.isaacapps.heatintegrationapp.internals.ProblemTable;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.DefinedPropertiesException;

public class PinchTempVsDT extends LineGraph {
	private Map<String, double[][]> dataPoints; //List is necessary in cases where the process has multiple varying pinches
	private ProblemTable problemTable;
	private double[] deltaTMinBounds;
	private int numOfPoints;
	
	public PinchTempVsDT(ProblemTable problemTable, double deltaTMinLowerBound, double deltaTMinUpperBound, int numOfPoints) throws DefinedPropertiesException{
		//Create clone of problemTable to prevent it from being inadvertently modified when deltaTMin is adjustment. 
		this.problemTable = new ProblemTable(problemTable.getStreams(), problemTable.getColumns(), problemTable.getDeltaTMin());
		this.deltaTMinBounds = new double[] {deltaTMinLowerBound, deltaTMinUpperBound};
		this.numOfPoints = numOfPoints;		
		dataPoints = new HashMap<String, double[][]>();
		setupDataPoints();
	}
	public PinchTempVsDT(ProblemTable problemTable) throws DefinedPropertiesException {
		this(problemTable, problemTable.getDeltaTMin(), problemTable.getDeltaTMin()+50.0, 50);
	} 	
	
	//
	private void setupDataPoints(){
		double[] dtmXValues = calculateDtmXValues();
		List<ArrayList<Double>> listOfPinchesForEveryDTM = createListOfPinchesForEveryDTM(dtmXValues);
		List<double[][]> listOfPinchVsDtmDatasets = createListOfPinchVsDtmDatasets(listOfPinchesForEveryDTM
				                                                                	,dtmXValues
				                                                                	,listOfPinchesForEveryDTM.stream()
				                                                                						 .mapToInt(pinchList->pinchList.size())
				                                                                                         .max().orElse(0));
		//Create a map of "Pinch #" for every pinch vs delta T Min dataset
		dataPoints.putAll(IntStream.iterate(1, i -> i++).limit(listOfPinchVsDtmDatasets.size()).boxed()
				 				   .map(num -> new Object[]{"Pinch "+num, listOfPinchVsDtmDatasets.get(num-1)})
				 				   .collect(toMap(objArr->(String)objArr[0], objArr-> (double[][])objArr[1])));
	}
	
	private double[] calculateDtmXValues(){
		//Create delta T Min x values based on range and number of points specifications.
		return   DoubleStream.iterate(deltaTMinBounds[0], d -> d + (deltaTMinBounds[1] - deltaTMinBounds[0])/numOfPoints).limit(numOfPoints)
							 .toArray();
	}
	private List<ArrayList<Double>> createListOfPinchesForEveryDTM(double[] dtmXValues){
		//Lambda function that calculates shifted pinch temp for each delta T min input
		Function<Double, Set<Double>> calcPinch = dT -> {problemTable.setDeltaTMin(dT); return problemTable.getShiftPinchTemps();};
		 
		//Through functional transformations, creates a two element array. The first element is max number of pinches present for any of the delta T Min data points.
		//The second element is a stream of unique sets of pinches, which will subsequently be transformed to a list for easier manipulation
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Object[] arrayOf_SetSize_N_PinchSetStream_PerDTM = Arrays.stream(dtmXValues).boxed()
															   .map(calcPinch)
					                                           .collect(groupingBy(set->set.size()))
					                                           .entrySet().stream()
					                                           .map(entry -> new Object[]{entry.getKey(), entry.getValue().stream()})
					                                           .reduce((prev,curr)->new Object[]{(Integer)prev[0]>(Integer)curr[0]?prev[0]:curr[0]
					                                        		   							 , Stream.concat((Stream<Set>)curr[1], (Stream<Set>)prev[1])})
					                                           .get();
		

		return ((Stream<Set<Double>>)arrayOf_SetSize_N_PinchSetStream_PerDTM[1]).map(set -> new ArrayList<Double>(set))
																			   .collect(toList());
	}
	private List<double[][]> createListOfPinchVsDtmDatasets(List<ArrayList<Double>> listOfPinchesForEveryDTM, double[] dtmXValues, int maxNumOfPinches){
		//Fills in each pinch list will zeros until the size of the pinch reaches the max size previous calculated. 
		//This is for potential situations where the number of pinches may have varied as the delta T Min changed.
		//This will ensure that each graph will be zero until the point at which each pinch becomes prominent.
		for(ArrayList<Double> listOfPinchesPerDTM: listOfPinchesForEveryDTM){
			while(listOfPinchesPerDTM.size()<maxNumOfPinches){
				listOfPinchesPerDTM.add(0.0);
			}
			Collections.sort(listOfPinchesPerDTM);
		}
		
		//Create a separate [pinch temp vs. delta T Min] dataset for each pinch as it comes and goes and/or changes with changing delta T min values
		return listOfPinchesForEveryDTM.stream().map(list->list.stream().map(pinch->new ArrayList<>(Arrays.asList(pinch))).collect(toList()))
					                            .reduce((prev, curr)->{for(int i=0; i<maxNumOfPinches; i++)
					                            						{
					                            							prev.get(i).addAll(curr.get(i));
					                            						}
																	 return prev;})
					                            .orElse(Collections.emptyList())
					                            .stream().map(pinchList->new double[][]{dtmXValues
					                            	                                    , pinchList.stream().mapToDouble(pinch->pinch)
					                            	                                                        .toArray()})
								                .collect(toList());
	}
	
	public void updateGraph(){
		setupDataPoints();
	}
	
	//
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
	
	public List<double[][]> getListOfPinchCurveDataPoints(){
		return Collections.unmodifiableList(dataPoints.values().stream().collect(toList()));
	}
}
