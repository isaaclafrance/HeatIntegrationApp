package com.heatIntegration.internals;
import java.util.ArrayList;


public class Stream {
	//Fields
	private class SubStream{
		public float cp, enthalpyChange, inletTemp, outletTemp;
		public SubStream(float cp, float enthalpyChange, float inletTemp, float outletTemp){
			this.cp = cp;
			this.enthalpyChange = enthalpyChange;
			this.inletTemp = inletTemp;
			this.outletTemp = outletTemp;
		}
	}
	public ArrayList<SubStream> subStreams;
	private String streamName;
	private int streamNum;
	private String type; //Hot or Cold
	public float cp; // kW * K^-1
	public float enthalpyChange; // kW
	public float inletTemp; // K
	public float outletTemp; // K
	
	//Constructor
	public Stream(String streamName, int streamNum, float inputTemp, float outputTemp,  float cp, float enthalpyChange){
		this.streamName = streamName;
		this.streamNum = streamNum;		
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
		this(String.valueOf(streamNum), streamNum, inputTemp, outputTemp, cp, enthalpyChange);
	}
	
	//Methods
	public int[] calculateUnknownProperties(){
		int[] propStates = new int[]{(inletTemp==0.0f)?1:0,
									 (outletTemp==0.0f)?1:0,
									 (cp==0.0f)?1:0,
									 (enthalpyChange==0.0f)?1:0}; 
		
		int propStatesSum = propStates[0] + propStates[1] + propStates[2];
		
		if(propStatesSum == 3){
			notEnoughData(propStates);
			return propStates;
		}
		else{
			if(propStates[0]==1){
				inletTemp = outletTemp - (enthalpyChange/cp);
			}
			else if(propStates[1]==1){
				outletTemp = inletTemp + (enthalpyChange/cp);
			}
			else if(propStates[2]==2){
				cp = enthalpyChange/(outletTemp - inletTemp);
			}
			else{
				enthalpyChange = cp*(outletTemp-inletTemp);
			}
			
			return new int[]{-1};
		}
	}
	
	private void notEnoughData(int[] propStates){
		System.out.println("Stream has insufficient # defined properties.");	
	}
	
	public String getType(){
		return type;
	}
	public String getStreamName(){
		return streamName;
	}
	public int getStreamNum(){
		return streamNum;
	}
}


