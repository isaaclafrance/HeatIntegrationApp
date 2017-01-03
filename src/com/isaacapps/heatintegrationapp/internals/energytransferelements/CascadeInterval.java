package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import java.util.ArrayList;
import java.util.List;

import com.isaacapps.heatintegrationapp.internals.energytransferelements.EnergyTransferElement;

public class CascadeInterval extends EnergyTransferElement {
	private int cascadeIndex;
	private double cascadeEnergy;
	private List<EnergyTransferElement> energyTransfererElementsCrossingInterval;

	//
	public CascadeInterval(double temp1, double temp2, double totalCP, double heatLoad, double cascadeEnergy, String type, int cascadeIndex) throws DefinedPropertiesException {
		super(type, temp1, temp2, totalCP, heatLoad, 0.0);
		this.cascadeIndex = cascadeIndex;
		this.cascadeEnergy = cascadeEnergy;
		setName(type);
		this.energyTransfererElementsCrossingInterval = new ArrayList<>();
	}
	
	//
	@Override
	protected void calculateUnknownProperties(){
		//Most necessary calculations would have already been done by the problem table and they are assumed to be correct.
		setShiftTemps(deltaTMin);
	}
	 
	@Override
	protected void determineType(){
		//Type needs to be the same as name, so type should actually be set when name is set.
	}
	

	//
	public double getCascadeEnergy() {
		return cascadeEnergy;
	}
	public String getCascadeEnergyWithUnit(){
		return getCascadeEnergy()+" "+getHeatUnit();
	}
	public void setCascadeEnergy(double cascadeEnergy) {
		this.cascadeEnergy = cascadeEnergy;
	}
	
	public int getCascadeIndex(){
		return cascadeIndex;
	}
	
	public List<EnergyTransferElement> getEnergyTransferersCrossingInterval(){
		return energyTransfererElementsCrossingInterval;
	}
	
	public void setName(String name){
		this.name = name;
		this.type = name;
	}
	
	@Override
	public void setShiftTemps(double deltaTMin){
		sourceShiftTemp = sourceTemp;
		targetShiftTemp = targetTemp;
	}
	
	//
	public String toString(){
		String etElemObj = super.toString().substring(super.toString().indexOf(":")); 
		return String.format("\"cascadeInterval\": {%s, \"index\": %d, \"cascadingEnergy\": %f, \"crossingElements\": [%s]}"
						     ,etElemObj.substring(1, etElemObj.length())
				             ,cascadeIndex, cascadeEnergy
				             , energyTransfererElementsCrossingInterval.stream().map(et->et.toString())
				                                                                .reduce((prev, curr)->prev+","+curr.substring(curr.indexOf(":")+1))
				                                                                .orElse(""));
	}
}