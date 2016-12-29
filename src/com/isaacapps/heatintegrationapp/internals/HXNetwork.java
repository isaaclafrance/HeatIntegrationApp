package com.isaacapps.heatintegrationapp.internals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.isaacapps.heatintegrationapp.graphics.HXNetworkDiagram;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.HeatExchanger;

public class HXNetwork {
	private List<HeatExchanger> heatExchangers;
	private ProblemTable problemTable;
	private HXNetworkDiagram hxNetworkDiagram;
	
	//
	public HXNetwork(ProblemTable problemTable){
		this.problemTable = problemTable;
		heatExchangers = new ArrayList<>();
		designDesignNetwork();
	}
	public HXNetwork(ProblemTable problemTable, List<HeatExchanger> hxExchangers){
		this.problemTable = problemTable;
		heatExchangers = hxExchangers;
		getHXNetworkDiagram().updateDiagram();
	}
	
	//
	public boolean designDesignNetwork(){
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
	
	//
	@Override
	public String toString(){
		return String.format("\"HXNetwork\":[%s]", heatExchangers.stream().map(hx->hx.toString()).reduce((prev,curr)->prev+","+curr.substring(curr.indexOf(":")+1)).orElse(""));
	}
}
