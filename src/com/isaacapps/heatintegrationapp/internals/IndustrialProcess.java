package com.isaacapps.heatintegrationapp.internals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.isaacapps.heatintegrationapp.internals.energytransferelements.Column;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class IndustrialProcess {
	public Map<String,Stream> streamsMap;
	public Map<String,Column> columnsMap;
	private ProblemTable problemTable;
	private HXNetwork hxNetwork;
	private double preMERHotUtility;
	private double preMERColdUtility;
	
	//
	public IndustrialProcess(List<Stream> streams, List<Column> columns, double defaultDeltaTMin, double preMERHotUtility, double preMERColdUtility){
		this.streamsMap = new HashMap<String, Stream>();
		for(Stream stream:streams){
			stream.setShiftTemps(defaultDeltaTMin);
			this.streamsMap.put(stream.getName(), stream);
		}
		this.columnsMap = new HashMap<String, Column>();
		for(Column column:columns){
			column.setShiftTemps(defaultDeltaTMin);
			this.columnsMap.put(column.getName(), column);
		}
		
		this.preMERHotUtility = preMERHotUtility;
		this.preMERColdUtility = preMERColdUtility;
		
		problemTable = new ProblemTable(new ArrayList<Stream>(streamsMap.values()), new ArrayList<Column>(columnsMap.values()), defaultDeltaTMin);
		
		hxNetwork = new HXNetwork(problemTable);	
	}
	public IndustrialProcess(double defaultDeltaTMin, double preMERHotUtility, double preMERColdUtility){
		this(new ArrayList<>(), new ArrayList<>(), defaultDeltaTMin, preMERHotUtility, preMERColdUtility );
	}
	
	//
	public void addStream(Stream stream){
		streamsMap.put(stream.getName(), stream).setIndustrialProcessRef(this);
		updateAll();
	}
	public void removeStream(String streamName){
		streamsMap.remove(streamName).setIndustrialProcessRef(null);
		updateAll();
	}
	
	public void addColumn(Column column){
		columnsMap.put(column.getName(), column);
		updateAll();
	}
	public void removeColumn(String columnName){
		columnsMap.remove(columnName);
		updateAll();
	}
	
	//
	public void updateAll(){
		problemTable.performFeasibleEnergyCascade();
		hxNetwork.designDesignNetwork();	
	}
	
	//
	public double getPreMERHotUtility(){
		return preMERHotUtility;
	}
	public double getPreMERColdUtility(){
		return preMERColdUtility;
	}	
	public double getMinimumHotUtility(){
		return problemTable.getMERQH();
	}
	public double getMinimumColdUtility(){
		return problemTable.getMERQC();
	}
	public Set<Double> getPinchTemps(){
		return problemTable.getUnshiftedPinchTemps();
	}
		
	public void setDeltaTMin(double deltaTMin){
		streamsMap.values().stream().forEach(s -> s.setShiftTemps(deltaTMin));
		columnsMap.values().stream().forEach(c -> c.setShiftTemps(deltaTMin));
		
		problemTable.setDeltaTMin(deltaTMin);
		hxNetwork.designDesignNetwork();
	}
	public double getDeltaTMin(){
		return problemTable.getDeltaTMin();
	}
	
	public ProblemTable getProblemTable(){
		return problemTable;
	}
	
	public HXNetwork getHXNetwork(){
		return hxNetwork;
	}
	public void setHXNetwork(HXNetwork hxNetwork){
		this.hxNetwork = hxNetwork;
	}
	
	public Map<String, Stream> getStreamsMap(){
		return streamsMap;
	}
	public Map<String, Column> getColumnsMap(){
		return columnsMap;
	}
	
	//
	public String toString(){
		return String.format("{\"Industrial Process\":{\"streams\": [%s], \"columns\": [%s], %s, %s}}"
				             , streamsMap.values().stream().map(s->s.toString()).reduce((prev,curr)->prev+","+curr.substring(curr.indexOf(":")+1)).orElse("")
				             , columnsMap.values().stream().map(c->c.toString()).reduce((prev,curr)->prev+","+curr.substring(curr.indexOf(":")+1)).orElse("")
				             , hxNetwork.toString()
				             , problemTable.toString());
	}
	
	//////////
	public static void main(String[] args) throws DefinedPropertiesException{
		ArrayList<Stream> streams = new ArrayList<Stream>();
		streams.add(new Stream("H1", 230.0, 65.0, 1.1, 0.0));
		streams.add(new Stream("H2", 122.85, 391.85, 3.25, 0.0));
		streams.add(new Stream("C1", 45.0, 590.0, 0.18, 0.0));

		
		ArrayList<Column> columns = new ArrayList<Column>();
		columns.add(new Column( new double[]{20.0,45.6}, new double[]{450.0, 500.0}, new double[]{20.0, 34.0}, new double[]{250.0, 350.0} ));
		
		IndustrialProcess ip = new IndustrialProcess(streams, columns, 10.0, 0.0, 0.0);
		System.out.println("Hot Utility: " + ip.getMinimumHotUtility());
		System.out.println("Cold Utility: " + ip.getMinimumColdUtility());
		ip.getPinchTemps().stream().forEach(temp -> System.out.println("An Unshifted Pinch Temp: " + temp));
		System.out.println(ip);
	}
}
