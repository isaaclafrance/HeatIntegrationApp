package com.heatIntegration.internals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProblemTable {
	///Fields
	private StreamProcess streamProcess;
	public class Interval{
		public float temp1, temp2, cP, heatLoad, cascadeEnergy;
		public String type;
		public Interval(float temp1, float temp2, float cP, float heatLoad, float cascadeEnergy, String type){
			this.temp1 = temp1;
			this.temp2 = temp2;
			this.cP = cP;
			this.heatLoad = heatLoad;
			this.cascadeEnergy = cascadeEnergy;
			this.type = type;
		}
	}
	public ArrayList<Interval> cascadeIntervals;	
	public float qH, qC, pinchTemp, defaultDeltaTMin;
	
	///Constructor
	public ProblemTable(StreamProcess streamProcess, float defaultDeltaTMin){
		this.streamProcess = streamProcess;	
		this.defaultDeltaTMin = defaultDeltaTMin;
		setupCascadeIntervals();
	}
	
	////Methods
	private void setupCascadeIntervals(){
		setupCascadeIntervals(defaultDeltaTMin);
	}
	private void setupCascadeIntervals(float deltaTMin){
		Map<Float, ArrayList<String[]>> colTemp_ColNum_Dict = assignIntervalShiftTemperatures(deltaTMin);
		calculateIntervalSpecificHeat();
		calculateIntervalHeatLoads( colTemp_ColNum_Dict );
	}
	private Map<Float, ArrayList<String[]>> assignIntervalShiftTemperatures(float deltaTMin){
		ArrayList<Float> shiftTempVec = new ArrayList<Float>();
		float[] shiftTempVec_array;
		Map<Float, ArrayList<String[]>> colTemp_ColNum_Dict = new HashMap<Float, ArrayList<String[]>>(); //Associates a temperature with a column's reboiler and/or condenser
		
		//Finds shift temperature of streams
		for(int i=0; i<streamProcess.streams.size(); i++){
			if(!shiftTempVec.contains(streamProcess.streams.get(i).inletTemp + deltaTMin/2) || 
				!shiftTempVec.contains(streamProcess.streams.get(i).inletTemp - deltaTMin/2)){
				if(streamProcess.streams.get(i).getType().equals("Cold")){
					shiftTempVec.add(streamProcess.streams.get(i).inletTemp + deltaTMin/2);
				}
				else{
					shiftTempVec.add(streamProcess.streams.get(i).inletTemp - deltaTMin/2);
				}
			}
			if(!shiftTempVec.contains(streamProcess.streams.get(i).outletTemp + deltaTMin/2) || 
			   !shiftTempVec.contains(streamProcess.streams.get(i).outletTemp - deltaTMin/2)){
				if(streamProcess.streams.get(i).getType().equals("Cold")){
					shiftTempVec.add(streamProcess.streams.get(i).outletTemp + deltaTMin/2);
				}
				else{
					shiftTempVec.add(streamProcess.streams.get(i).outletTemp - deltaTMin/2);
				}
			}
		}
		shiftTempVec_array = new float[shiftTempVec.size()];
		for(int i=0;i<shiftTempVec.size();i++){
			shiftTempVec_array[i] = shiftTempVec.get(i);
		}
		Arrays.sort(shiftTempVec_array);
		
		//Finds shift temperature of columns treated as streams
		for(int i=0; i<streamProcess.columns.size(); i++){
			//Adds condenser as hot stream
			if(colTemp_ColNum_Dict.containsKey(streamProcess.columns.get(i).condTemp - deltaTMin/2)){
				colTemp_ColNum_Dict.get(streamProcess.columns.get(i).condTemp - deltaTMin/2).add(new String[]{"Condenser of Column "+streamProcess.columns.get(i).getColName(),
																											  i+" Condenser"});
			}		
			else{
				colTemp_ColNum_Dict.put(streamProcess.columns.get(i).condTemp - deltaTMin/2, new ArrayList<String[]>());
				colTemp_ColNum_Dict.get(streamProcess.columns.get(i).condTemp - deltaTMin/2).add(new String[]{"Condenser of Column "+streamProcess.columns.get(i).getColName(),
																											  i+" Condenser"});
				
			}
			//Adds reboiler as cold stream
			if(colTemp_ColNum_Dict.containsKey(streamProcess.columns.get(i).rebTemp + deltaTMin/2)){
				colTemp_ColNum_Dict.get(streamProcess.columns.get(i).rebTemp + deltaTMin/2).add(new String[]{"Reboiler of Column "+streamProcess.columns.get(i).getColName(),
																											 i+" Reboiler"});
			}else{
				colTemp_ColNum_Dict.put(streamProcess.columns.get(i).rebTemp + deltaTMin/2, new ArrayList<String[]>());
				colTemp_ColNum_Dict.get(streamProcess.columns.get(i).rebTemp + deltaTMin/2).add(new String[]{"Reboiler of Column "+streamProcess.columns.get(i).getColName(),
																											 i+" Reboiler"});
			}
		}
		
		//Creates intervals and assigns temperature ranges and types
		for(int i=0; i<shiftTempVec_array.length-1; i++){
			cascadeIntervals.add(new Interval(shiftTempVec_array[i], shiftTempVec_array[i+1], 0.0f, 0.0f, 0.0f, ""));
		}
		
		for(Float temp:colTemp_ColNum_Dict.keySet()){
			for(String[] type:colTemp_ColNum_Dict.get(temp)){
				cascadeIntervals.add(new Interval(temp, temp, 0.0f, 0.0f, 0.0f, type[0]));
			}
		}
		
		//Return a dictionary of temperature and associated column type and column arraylist location
		return colTemp_ColNum_Dict;
	}
	private void calculateIntervalSpecificHeat(){
		for(Interval interval:cascadeIntervals){
			if(interval.type.equals("")){
				for(int i=0;i<streamProcess.streams.size();i++){
				  if ( ((interval.temp1 <= streamProcess.streams.get(i).inletTemp)&&(interval.temp2 >= streamProcess.streams.get(i).outletTemp)) 
				    || ((interval.temp1 >= streamProcess.streams.get(i).inletTemp)&&(interval.temp2 <= streamProcess.streams.get(i).outletTemp)) ){
					
					if(streamProcess.streams.get(i).getType().equals("Hot")){
						interval.cP += streamProcess.streams.get(i).cp;
					}
					else{
						interval.cP -= streamProcess.streams.get(i).cp;
					}
				  }
				}
			}
		}
	}
	private void calculateIntervalHeatLoads(Map<Float, ArrayList<String[]>> colTemp_ColNum_Dict){
		int colIntervalRepeatNum = 0;
		CharSequence rebSeq = "Reboiler";
		CharSequence condSeq = "Condenser";
		
		for(Interval interval:cascadeIntervals){
			if(interval.type.equals("")){
				interval.heatLoad = interval.cP * (interval.temp2 - interval.temp1);
			}
			else{
				if(colTemp_ColNum_Dict.get(interval.temp1).get(colIntervalRepeatNum)[1].contains(condSeq)){
					interval.heatLoad = streamProcess.columns.get(Integer.parseInt(colTemp_ColNum_Dict.get(interval.temp1).get(colIntervalRepeatNum)[1].substring(0, 1))).condHeatLoad;
				}
				else if(colTemp_ColNum_Dict.get(interval.temp1).get(colIntervalRepeatNum)[1].contains(rebSeq)){
					interval.heatLoad = streamProcess.columns.get(Integer.parseInt(colTemp_ColNum_Dict.get(interval.temp1).get(colIntervalRepeatNum)[1].substring(0, 1))).rebHeatLoad;
				}
				
				if(colTemp_ColNum_Dict.get(interval.temp1).size() > 1 && colIntervalRepeatNum < colTemp_ColNum_Dict.get(interval.temp1).size()-1){
					colIntervalRepeatNum++;
				}
				else{
					colIntervalRepeatNum = 0;
				}
			}
		}
	}
	private int findPinchIntervalIndex(){
		for(int i=0; i<cascadeIntervals.size(); i++){
			if(cascadeIntervals.get(i).cascadeEnergy == 0){
				return i;
			}
		}
		return -1;
	}
	private int findInfeasibleIntervalIndex(){
		int minIndex = 0;
		
		for(int i=0; i<cascadeIntervals.size(); i++){
			if(cascadeIntervals.get(i).cascadeEnergy < cascadeIntervals.get(minIndex).cascadeEnergy){
				minIndex = i;
			}
		}
		return minIndex;
	}
	
 	public int performEnergyCascade(float qH_initial, float deltaTMin){
		setupCascadeIntervals(deltaTMin);
		
		cascadeIntervals.get(0).cascadeEnergy = qH_initial + cascadeIntervals.get(0).heatLoad;
		for(int i=1; i<cascadeIntervals.size(); i++){
			cascadeIntervals.get(i).cascadeEnergy = cascadeIntervals.get(i-1).cascadeEnergy + cascadeIntervals.get(i).heatLoad;
		}
		
		qH = 0.0f;
		qC = cascadeIntervals.get(cascadeIntervals.size()-1).cascadeEnergy;
		int pinchIntervalIndex = findPinchIntervalIndex();
		pinchTemp = (pinchIntervalIndex >0 )?cascadeIntervals.get(pinchIntervalIndex).temp2:0;
		
		return pinchIntervalIndex;
	}
	public int performEnergyCascade(float qH_initial){
		return performEnergyCascade(qH_initial, defaultDeltaTMin);
	}
	public void performFeasibleEnergyCascade(float deltaTMin){
		performEnergyCascade(0, deltaTMin);		
		qH = cascadeIntervals.get(findInfeasibleIntervalIndex()).cascadeEnergy;
		pinchTemp = performEnergyCascade(qH, deltaTMin);
		qC = cascadeIntervals.get(cascadeIntervals.size()-1).cascadeEnergy;
	}
	public void performFeasibleEnergyCascade(){
		performFeasibleEnergyCascade(defaultDeltaTMin);
	}	
}
