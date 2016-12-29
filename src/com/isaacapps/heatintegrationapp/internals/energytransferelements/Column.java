package com.isaacapps.heatintegrationapp.internals.energytransferelements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;

public class Column {
	public static double UTILITY_TEMP_DIFF = 0.00001;
	private String name;
	private int num;
	private List<Reboiler> reboilers;
	private List<Condenser> condensers;
	private static int numCount = 1;

	//
	public Column(String name, double[] rebTemps, double[] rebHeatLoads, double condTemps[], double[] condHeatLoads, double deltaTMin) throws DefinedPropertiesException{
		this.name = name;
		this.num = numCount++;
		
		if(rebTemps.length != rebHeatLoads.length){
			throw new DefinedPropertiesException("Number of reboiler temperatures does not match number of reboiler heat loads provided.", name);
		}
		if(condTemps.length != condHeatLoads.length){
			throw new DefinedPropertiesException("Number of condenser temperatures does not match number of condenser heat loads provided.", name);
		}
		
		reboilers = new ArrayList<Reboiler>();
		for(int i=0; i<rebTemps.length; i++){
			addReboiler(new Reboiler(rebTemps[i], rebHeatLoads[i], deltaTMin));
		}
		
		condensers = new ArrayList<Condenser>();
		for(int i=0; i<condTemps.length; i++){
			addCondenser(new Condenser(condTemps[i], condHeatLoads[i], deltaTMin));
		}
	}
	public Column(double[] rebTemps, double[] rebHeatLoads, double[] condTemps, double[] condHeatLoads, double deltaTMin) throws DefinedPropertiesException{
		this("Column ", rebTemps, rebHeatLoads, condTemps, condHeatLoads, deltaTMin);
		name += num;
	}
	public Column(double[] rebTemps, double[] rebHeatLoads, double[] condTemps, double[] condHeatLoads) throws DefinedPropertiesException{
		this(rebTemps, rebHeatLoads, condTemps, condHeatLoads, 0.0f);
	}
	
	//
	public void addReboiler(Reboiler reboiler){
		reboiler.setColumn(this);
		reboilers.add(reboiler);
	}
	public void addCondenser(Condenser condenser){
		condenser.setColumn(this);
		condensers.add(condenser);
	}
	
	//
	public String getName(){
		return name;
	}
	
	public List<Reboiler> getReboilers(){
		return reboilers;
	}
	public List<Condenser> getCondensers(){
		return condensers;
	}
	
	public void setShiftTemps(double deltaTMin){
		reboilers.stream().forEach(reb -> reb.setShiftTemps(deltaTMin));
		condensers.stream().forEach(cond -> cond.setShiftTemps(deltaTMin));
	}
	
	public String toString(){
		return String.format("\"column\":{\"name\": \"%s\", \"reboilers\": [%s], \"condensers\": [%s]}", name,
				reboilers.stream().map(reb -> reb.toString()).reduce((prev, curr) -> prev.substring(curr.indexOf(":")+1)+ ","+curr).orElse(""),
				condensers.stream().map(cond -> cond.toString()).reduce((prev, curr) -> prev+","+curr.substring(curr.indexOf(":")+1)).orElse(""));
	}
}
