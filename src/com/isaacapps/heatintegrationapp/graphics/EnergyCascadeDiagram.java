package com.isaacapps.heatintegrationapp.graphics;

import java.util.Arrays;
import java.util.function.Function;

import com.isaacapps.heatintegrationapp.internals.ProblemTable;

public class EnergyCascadeDiagram {
	private ProblemTable problemTable;
	private String energyCascadeString;
	
	public EnergyCascadeDiagram(ProblemTable problemTable){
		this.problemTable = problemTable;
		energyCascadeString = getEnergyCascadeString();
	}
	
	//
	private int getMaxTempNHeatWithUnitCharLenth( int merQHStrLen){
		return problemTable.getCascadeIntervals().stream()
				.flatMap(cI -> Arrays.asList(cI.getHeatLoadWithUnit(), cI.getTargetShiftTempWithUnit()).stream())
				.mapToInt(size -> size.length())
				.filter(i-> i > merQHStrLen)
				.max()
				.orElse(merQHStrLen);
	}
	private String getMERQHString(String merQH, String arrowPadding, Function<String, String> padTempOrLoad){
		
		return String.format("\n%2$s \n%1$s| \n%1$sV \n%3$s"
				, arrowPadding
				, padTempOrLoad.apply(merQH)
				, padTempOrLoad.apply(problemTable.getCascadeIntervals().get(0).getSourceShiftTempWithUnit()));
	}
	private String getCascadeIntervalsString(String arrowPadding, int maxTempNLoadCharLength, Function<String, String> padTempOrLoad){
		String boxTopNBottom = new String(new char[maxTempNLoadCharLength]).replace("\0", "*");
		
		return problemTable.getCascadeIntervals().stream().map(cI -> String.format("\n#%1$s# \n#%2$s%2$s # \n#%3$s %4$s \n#%2$s%2$s # \n#%1$s# \n%2$s| \n%5$s \n%2$s| \n%2$sV \n%6$s"
																,boxTopNBottom ,arrowPadding
																,padTempOrLoad.apply(cI.getHeatLoadWithUnit())
																,((!cI.getEnergyTransferersCrossingInterval().isEmpty()) ? " " + cI.getName(): "")
																," "+ padTempOrLoad.apply(cI.getCascadeEnergyWithUnit())
																,padTempOrLoad.apply(cI.getTargetTempWithUnit())
																    + (problemTable.getShiftPinchTemps().contains(cI.getTargetTemp()) 
																    		                      && cI.getSourceTemp() != cI.getTargetTemp() ? " <<<---PINCH" : "")))
										                  .reduce((prev,curr)->prev+curr).orElse("");
	}
	private String getEnergyCascadeString() {
		String merQH = "MER QH: " + problemTable.getMerQhWithUnit();
		
		int initialTempNLoadPadding = 2;  
		int maxTempNLoadCharLength = getMaxTempNHeatWithUnitCharLenth(merQH.length());
		
		String arrowPadding = new String(new char[(int) Math.floor(maxTempNLoadCharLength / 2.0)]).replace("\0", " ");
		
		Function<String, String> padTempOrLoad = tempOrLoadStr -> new String(new char[(initialTempNLoadPadding + maxTempNLoadCharLength - tempOrLoadStr.length()) / 2])
				                                                            .replace("\0"," ")+tempOrLoadStr;
		
		return  getMERQHString(merQH, arrowPadding, padTempOrLoad) + getCascadeIntervalsString(arrowPadding, maxTempNLoadCharLength, padTempOrLoad);
				                         
	}
	
	//
	@Override
	public String toString(){
		return energyCascadeString;
	}
}
