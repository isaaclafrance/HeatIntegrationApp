package com.isaacapps.heatintegrationapp.internals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.isaacapps.heatintegrationapp.internals.energytransferelements.EnergyTransferElement;

public class CascadeInterval {
	private int cascadeIndex;
	private double temp1;
	private double temp2;
	private double totalCP;
	private double heatLoad;
	private double cascadeEnergy;
	private String type;
	private List<EnergyTransferElement> energyTransfererElementsCrossingInterval;

	//
	public CascadeInterval(double temp1, double temp2, double cP, double heatLoad, double cascadeEnergy, String type, int cascadeIndex) {
		this.cascadeIndex = cascadeIndex;
		this.temp1 = temp1;
		this.temp2 = temp2;
		this.totalCP = cP;
		this.heatLoad = heatLoad;
		this.cascadeEnergy = cascadeEnergy;
		this.type = type;
		this.energyTransfererElementsCrossingInterval = new ArrayList<>();
	}

	//
	public double getTemp1() {
		return temp1;
	}
	public void setTemp1(double temp1) {
		this.temp1 = temp1;
	}
	
	public double getTemp2() {
		return temp2;
	}
	public void setTemp2(double temp2) {
		this.temp2 = temp2;
	}

	public double getTotalCP() {
		return totalCP;
	}
	public void setTotalCP(double totalCP) {
		this.totalCP = totalCP;
	}

	public double getHeatLoad() {
		return heatLoad;
	}
	public void setHeatLoad(double heatLoad) {
		this.heatLoad = heatLoad;
	}

	public double getCascadeEnergy() {
		return cascadeEnergy;
	}
	public void setCascadeEnergy(double cascadeEnergy) {
		this.cascadeEnergy = cascadeEnergy;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public int getCascadeIndex(){
		return cascadeIndex;
	}
	
	public List<EnergyTransferElement> getEnergyTransferersCrossingInterval(){
		return energyTransfererElementsCrossingInterval;
	}
	
	//
	public String toString(){
		return String.format("\"cascadeInterval\":{\"index\": %d, \"temp1\": %f, \"temp2\": %f, \"totalCP\": %f, \"heatLoad\": %f, \"cascadingEnergy\": %f, \"type\": \"%s\", \"crossingElements\": [%s]}"
				             ,cascadeIndex, temp1, temp2, totalCP, heatLoad, cascadeEnergy
				             , energyTransfererElementsCrossingInterval.stream().map(et->et.toString()).reduce((prev, curr)->prev+","+curr.substring(curr.indexOf(":")+1)).orElse(""));
	}
}