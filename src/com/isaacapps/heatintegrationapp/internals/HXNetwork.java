package com.isaacapps.heatintegrationapp.internals;

import java.util.ArrayList;
import java.util.List;

import com.isaacapps.heatintegrationapp.graphics.HXNetworkDiagram;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.HeatExchanger;

public class HXNetwork {
	private List<HeatExchanger> heatExchangers;
	private ProblemTable problemTable;
	private HXNetworkDiagram hxNetworkDiagram;
	
	//
	public HXNetwork(){
		heatExchangers = new ArrayList<HeatExchanger>();
	}
	public HXNetwork(ProblemTable problemTable){
		this();
		setProblemTable(problemTable);
	}
	public HXNetwork(ProblemTable problemTable, List<HeatExchanger> hxExchangers){
		//If a non-empty list of heat exchangers is already given, then assume it correct for the given problem table.
		//This prevent a possibly resource and time intensive  calculation from have to be done.
		setHeatExchangers(hxExchangers);
		setProblemTable(problemTable);
	}
	
	//
	public boolean designHXNetwork(){
		boolean solutionPossible = false;
		
		//TODO: Potentially Use a Genetic Algorithm Method to Find Network Solutions When Area and Cost Optimizations are Necessary
		
		getHXNetworkDiagram().updateDiagram();
		
		return solutionPossible;
	}
	
	
	//
	public HXNetworkDiagram getHXNetworkDiagram(){
		if(hxNetworkDiagram == null){
			hxNetworkDiagram = new HXNetworkDiagram(this);
		}
		return hxNetworkDiagram;
	}
	
	public List<HeatExchanger> getHeatExchangers(){
		return heatExchangers;
	}
	public void setHeatExchangers(List<HeatExchanger> heatExchangers){
		this.heatExchangers = heatExchangers;
		getHXNetworkDiagram().updateDiagram();
	}
	
	public void setProblemTable(ProblemTable problemTable){
		if(heatExchangers.isEmpty() || this.problemTable != problemTable){
			this.problemTable = problemTable;
			designHXNetwork();
		}
	}
	
	//
	@Override
	public String toString(){
		return String.format("\"HXNetwork\":[%s]", heatExchangers.stream().map(hx->hx.toString()).reduce((prev,curr)->prev+","+curr.substring(curr.indexOf(":")+1)).orElse(""));
	}
}
