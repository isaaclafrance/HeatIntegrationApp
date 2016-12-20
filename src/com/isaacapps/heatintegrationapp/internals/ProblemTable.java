package com.isaacapps.heatintegrationapp.internals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProblemTable {
	///Fields
	private StreamProcess streamProcess;
	private ArrayList<CascadeInterval> cascadeIntervals;	
	private float qH, qC, deltaTMin;
	private ArrayList<ArrayList<Float>> pinchTemps; // [0] -> shift pinch temps, [1] --> unshifted pinch temps
	Map<Float, ArrayList<CascadeInterval>> shiftTemp_Interval_Dict;
	
	///Constructor
	public ProblemTable(StreamProcess streamProcess, float deltaTMin){
		this.streamProcess = streamProcess;	
		this.qH = this.qC = 0.0f;
		this.deltaTMin = deltaTMin;
		this.cascadeIntervals = new ArrayList<CascadeInterval>();
		this.pinchTemps = new ArrayList<ArrayList<Float>>(); 
		this.shiftTemp_Interval_Dict = new HashMap<Float, ArrayList<CascadeInterval>>();
		for(int i=0;i<2;i++){
			this.pinchTemps.add(new ArrayList<Float>());
		}
	}
	
	////Methods
	private void setupCascadeIntervals(){
		createCasacadeInterval(createShiftTempVec());
		calculateIntervalSpecificHeat();
		calculateIntervalHeatLoads();
	}
	private ArrayList<Float> createShiftTempVec(){
		ArrayList<Float> shiftTempVec = new ArrayList<Float>();
		
		//
		for(Stream stream:streamProcess.streams.values()){
			if(!shiftTempVec.contains(stream.getShiftInletTemperature())){
				shiftTempVec.add(stream.getShiftInletTemperature());
			}
			
			if(!shiftTempVec.contains(stream.getShiftOutletTemperature())){
				shiftTempVec.add(stream.getShiftOutletTemperature());
			}
		}
		
		//Finds shift temperature of columns treated as streams
		for(Column column:streamProcess.columns.values()){
			//Adds condenser as hot stream
			if(!shiftTempVec.contains(column.getCondShiftTemperature())){
				shiftTempVec.add(column.getCondShiftTemperature());
			}	
			shiftTempVec.add(column.getCondShiftTemperature());
			
			//Adds reboiler as cold stream
			if(!shiftTempVec.contains(column.getRebShiftTemperature())){
				shiftTempVec.add(column.getRebShiftTemperature());
			}
			shiftTempVec.add(column.getRebShiftTemperature());
		}

		Collections.sort(shiftTempVec);
		
		return shiftTempVec;
	}
	private void createCasacadeInterval(ArrayList<Float> shiftTempVec){				
		for(int i=shiftTempVec.size()-1; i>0; i--){
			float temp1 = shiftTempVec.get(i);
			float temp2 = shiftTempVec.get(i-1);
			CascadeInterval cascadeInterval = new CascadeInterval(temp1, temp2, 0.0f, 0.0f, 0.0f, (temp1 != temp2)?"Stream Only Interval":"");
			
			cascadeIntervals.add(cascadeInterval);
			
			if(!shiftTemp_Interval_Dict.containsKey(temp1)){
				shiftTemp_Interval_Dict.put(temp1, new ArrayList<CascadeInterval>());
			}
			if(!shiftTemp_Interval_Dict.containsKey(temp2)){
				shiftTemp_Interval_Dict.put(temp2, new ArrayList<CascadeInterval>());
			}	
			
			shiftTemp_Interval_Dict.get(temp1).add(cascadeInterval);
			shiftTemp_Interval_Dict.get(temp2).add(cascadeInterval);
		}	
	}
	private void calculateIntervalSpecificHeat(){
		for(CascadeInterval interval:cascadeIntervals){
			if(interval.getType().equals("Stream Only Interval")){
				for(Stream stream:streamProcess.streams.values()){
				  if ( !(interval.getTemp1() <= (stream.getType().equalsIgnoreCase("cold")?stream.getShiftInletTemperature():stream.getShiftOutletTemperature())&&interval.getTemp2() <= (stream.getType().equalsIgnoreCase("cold")?stream.getShiftInletTemperature():stream.getShiftOutletTemperature())) 
				    && !(interval.getTemp1() >= (stream.getType().equalsIgnoreCase("hot")?stream.getShiftInletTemperature():stream.getShiftOutletTemperature())&&interval.getTemp2() >= (stream.getType().equalsIgnoreCase("hot")?stream.getShiftInletTemperature():stream.getShiftOutletTemperature())) ){
					
					if(stream.getType().equals("Hot")){
						interval.setcP(interval.getcP() + stream.getCP());
					}
					else{
						interval.setcP(interval.getcP() - stream.getCP());
					}
					
					interval.getStreamsCrossingInterval().add(stream);
				  }
				}
			}
		}
	}
	private void calculateIntervalHeatLoads(){
		for(CascadeInterval interval:cascadeIntervals){
			if(interval.getType().equals("Stream Only Interval")){
				interval.setHeatLoad(interval.getcP() * (interval.getTemp1() - interval.getTemp2()));
			}
			else{
				for(Column column:streamProcess.columns.values()){
					if(column.getRebShiftTemperature() == interval.getTemp1() && column.getRebShiftTemperature() == interval.getTemp2()){
						interval.setHeatLoad(-column.getRebHeatLoad());
						interval.setType("Column "+column.getColName()+" Reboiler");
					}
					else if(column.getCondShiftTemperature() == interval.getTemp1() && column.getCondShiftTemperature() == interval.getTemp2()){
						interval.setHeatLoad(column.getCondHeatLoad());
						interval.setType("Column "+column.getColName()+" Condenser");
					}
				}
			}
		}
	}
	
	private ArrayList<Integer> findPinchIntervalIndices(){
		ArrayList<Integer> pinchIndices = new ArrayList<Integer>();
		for(int i=0; i<cascadeIntervals.size(); i++){
			if(cascadeIntervals.get(i).getCascadeEnergy() < 0.0001f){
				 pinchIndices.add(i);
			}
		}
		return pinchIndices;
	}
	private int findInfeasibleIntervalIndex(){
		int minIndex = 0;
		
		for(int i=0; i<cascadeIntervals.size(); i++){
			if(cascadeIntervals.get(i).getCascadeEnergy() < cascadeIntervals.get(minIndex).getCascadeEnergy()){
				minIndex = i;
			}
		}
		return minIndex;
	}
	private void calculateUnshiftedPinchTemps(){
		//TODO: Calculate hot and cold stream temperatures;
	}
	
 	private void performEnergyCascade(float qHAdjustment){		
		cascadeIntervals.get(0).setCascadeEnergy(qHAdjustment + cascadeIntervals.get(0).getHeatLoad());
		for(int i=1; i<cascadeIntervals.size(); i++){
			cascadeIntervals.get(i).setCascadeEnergy(cascadeIntervals.get(i-1).getCascadeEnergy() + cascadeIntervals.get(i).getHeatLoad());
		}
		
		qH = qHAdjustment;
		qC = cascadeIntervals.get(cascadeIntervals.size()-1).getCascadeEnergy();	
		
		for(Integer pinchIndex:findPinchIntervalIndices()){
			pinchTemps.get(0).add(cascadeIntervals.get(pinchIndex).getTemp2()); //Shift temp
			calculateUnshiftedPinchTemps();
		}
	}
	public void performFeasibleEnergyCascade(float qHInitial){
		setupCascadeIntervals();
		
		performEnergyCascade(0.0f);		
		float heatLoadInfeasible = cascadeIntervals.get(findInfeasibleIntervalIndex()).getCascadeEnergy();
		if(heatLoadInfeasible < 0){
			performEnergyCascade(-heatLoadInfeasible);
		}
		
		qH = qHInitial + qH;
	}

	//
	public ArrayList<Float> getShiftPinchTemps(){
		return pinchTemps.get(0);
	}
	public ArrayList<Float> getUnshiftedPinchTemps(){
		return pinchTemps.get(1);
	}
	
	public ArrayList<CascadeInterval> getCascadeIntervals(){
		return cascadeIntervals;
	}
	
	public float getQH() {
		return qH;
	}
	public float getQC() {
		return qC;
	}	
	
	public float getDeltaTMin() {
		return deltaTMin;
	}

	//
	private String printQH(){
		return String.format("\nQH: %f kW \n  | \n  V \n %f K", qH, cascadeIntervals.get(0).getTemp1());
	}
	private String printCascadeIntervals(){
		String cascadeString = "";
		
		for(CascadeInterval cascadeInterval:cascadeIntervals){
			cascadeString += String.format("\n************ \n*%f kW* \n************ \n  | \n%f kW \n  | \n  V \n%f K" 
		                                   + ((pinchTemps.get(0).contains(cascadeInterval.getTemp1()) || pinchTemps.get(0).contains(cascadeInterval.getTemp2()))?" <<---- PINCH":"")
		                                   , cascadeInterval.getHeatLoad()
					                       , cascadeInterval.getCascadeEnergy()
					                       , cascadeInterval.getTemp2());
		}
		
		return cascadeString;
	}
	public String toString(){
		return printQH() +"/n"+ printCascadeIntervals() +"/n";
	}
	
}
