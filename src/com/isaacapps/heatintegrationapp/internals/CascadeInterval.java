package com.isaacapps.heatintegrationapp.internals;

import java.util.ArrayList;

public class CascadeInterval {
	private float temp1;
	private float temp2;
	private float cP;
	private float heatLoad;
	private float cascadeEnergy;
	private String type;
	private ArrayList<Stream> streamsCrossingInterval;

	public CascadeInterval(float temp1, float temp2, float cP, float heatLoad, float cascadeEnergy, String type) {
		this.temp1 = temp1;
		this.temp2 = temp2;
		this.cP = cP;
		this.heatLoad = heatLoad;
		this.cascadeEnergy = cascadeEnergy;
		this.type = type;
		this.streamsCrossingInterval = new ArrayList<Stream>();
	}

	public float getTemp1() {
		return temp1;
	}
	public void setTemp1(float temp1) {
		this.temp1 = temp1;
	}
	
	public float getTemp2() {
		return temp2;
	}
	public void setTemp2(float temp2) {
		this.temp2 = temp2;
	}

	public float getcP() {
		return cP;
	}
	public void setcP(float cP) {
		this.cP = cP;
	}

	public float getHeatLoad() {
		return heatLoad;
	}
	public void setHeatLoad(float heatLoad) {
		this.heatLoad = heatLoad;
	}

	public float getCascadeEnergy() {
		return cascadeEnergy;
	}
	public void setCascadeEnergy(float cascadeEnergy) {
		this.cascadeEnergy = cascadeEnergy;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public ArrayList<Stream> getStreamsCrossingInterval(){
		return streamsCrossingInterval;
	}
}