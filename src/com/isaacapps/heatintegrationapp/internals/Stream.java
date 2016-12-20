package com.isaacapps.heatintegrationapp.internals;
import java.util.ArrayList;


public class Stream {
	//Fields
	public ArrayList<Stream> subStreams;
	private String streamName;
	private int streamNum;
	private String type; //Hot or Cold
	private float cp; // kW * K^-1
	private float enthalpyChange; // kW
	private float inletTemp; // K
	private float inletShiftTemp; //K
	private float outletTemp; // K
	private float outletShiftTemp; // K
	private static int streamCounter = 0;
	
	//Constructor
	public Stream(String streamName, float inputTemp, float outputTemp,  float cp, float enthalpyChange){
		this.streamName = streamName;
		this.streamNum = streamCounter++;		
		this.inletTemp = inputTemp;
		this.outletTemp = outputTemp;
		this.cp = cp;
		this.enthalpyChange = enthalpyChange;
		
		if (inputTemp == 0.0f || outputTemp == 0.0f || cp == 0.0f || enthalpyChange == 0.0f){
			calculateUnknownProperties();
		}
		
		if(inputTemp<outputTemp){
			type = "Cold";
		}
		else{
			type = "Hot";
		}
	}
	public Stream(int streamNum, float inputTemp, float outputTemp,  float cp, float enthalpyChange){
		this(String.valueOf(streamNum), inputTemp, outputTemp, cp, enthalpyChange);
	}
	
	//Methods
	public boolean calculateUnknownProperties(){
		int[] propStates = new int[]{(inletTemp==0.0f)?0:1,
									 (outletTemp==0.0f)?0:1,
									 (cp==0.0f)?0:1,
									 (enthalpyChange==0.0f)?0:1}; 
		
		int propStatesSum = propStates[0] + propStates[1] + propStates[2] + propStates[3];
		
		if(propStatesSum < 3){
			System.out.println("Stream has insufficient # defined properties.");
			return false;
		}
		else if(propStatesSum == 4){
			boolean state = inletTemp == outletTemp - (enthalpyChange/cp) && outletTemp == inletTemp + (enthalpyChange/cp)
						    && cp == Math.abs(enthalpyChange/(outletTemp - inletTemp))
							&& enthalpyChange == cp*(outletTemp-inletTemp);
				
			if(!state){
				System.out.println("Stream properties incorrectly defined.");
			}
			
			return state;
		}
		else{
			if(propStates[0]==0){
				inletTemp = outletTemp - (enthalpyChange/cp);
			}
			else if(propStates[1]==0){
				outletTemp = inletTemp + (enthalpyChange/cp);
			}
			else if(propStates[2]==0){
				cp = Math.abs(enthalpyChange/(outletTemp - inletTemp));
			}
			else{
				enthalpyChange = cp*(outletTemp-inletTemp);
			}
			return true;
		}
	}
	
	//
	public String getType(){
		return type;
	}
	public String getStreamName(){
		return streamName;
	}
	public int getStreamNum(){
		return streamNum;
	}
	public float getCP(){
		return cp;
	}
	public float getInletTemp(){
		return inletTemp;
	}
	public float getOutletTemp(){
		return outletTemp;
	}
	public float getEnthalpyChange(){
		return enthalpyChange;
	}
	
	public void setShiftInletTemperature(float deltaT){
		if(type.equals("Cold")){
			inletShiftTemp = inletTemp + deltaT/2;
		}else{
			inletShiftTemp = inletTemp - deltaT/2;
		}
	}
	public void setShiftOutletTemperature(float deltaT){
		if(type.equals("Cold")){
			outletShiftTemp = outletTemp + deltaT/2;
		}else{
			outletShiftTemp = outletTemp - deltaT/2;
		}
	}
	
	public float getShiftInletTemperature(){
		return inletShiftTemp;
	}
	public float getShiftOutletTemperature(){
		return outletShiftTemp;
	}
	//
	public String toString(){
		return String.format("{Stream Name: %s, Stream Num: %d, Type: %s, cp: %f kW * K^-1, Inlet Temp: %f K, Outlet Temp: %f K, Enthalpy Change: %f kW }", streamName, streamNum, type, cp, inletTemp, outletTemp, enthalpyChange);
	}
}


