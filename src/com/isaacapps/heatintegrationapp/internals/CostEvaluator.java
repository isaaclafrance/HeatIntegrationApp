package com.isaacapps.heatintegrationapp.internals;

//TODO: Implement a class that calculates the estimated total cost of operating an industrial process with considerations 
//based on energy requirements, heat exchangers, etc..
public class CostEvaluator {
	private IndustrialProcess industrialProcess;
	private double energyCostPerKilogram;
	private double globalCost;
	
	public CostEvaluator(IndustrialProcess industrialProcess){
		this.industrialProcess = industrialProcess;
		calculateGlobalCost();
	}
	
	//
	private double calculateHeatExchangerCosts(){
		//TODO: Incorporate area size and material costs into calculations		
		return 0.0;
	}
	private double calculateUtilityCosts(){
		//TODO: Incorporate energy source type, amount, etc.
		return 0.0;
	}
	
	public void calculateGlobalCost(){
		globalCost = calculateHeatExchangerCosts()+calculateUtilityCosts();
	}
		
	//
	public double getGlobalCost(){
		return globalCost;
	}
	
	public void setIndustrialProcess(IndustrialProcess industrialProcess){
		this.industrialProcess = industrialProcess;
		calculateGlobalCost();
	}
	
	//
	@Override
	public String toString(){
		//TODO: Implement a string representation of cost evaluator
		
		return String.format("\"Cost Evaluator\": {}");
	}
}
