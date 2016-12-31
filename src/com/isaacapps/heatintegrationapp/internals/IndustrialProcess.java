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
	public IndustrialProcess(List<Stream> streams, List<Column> columns, double defaultDeltaTMin, double preMERHotUtility, double preMERColdUtility) throws DefinedPropertiesException{
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
	public IndustrialProcess(double defaultDeltaTMin, double preMERHotUtility, double preMERColdUtility) throws DefinedPropertiesException{
		this(new ArrayList<>(), new ArrayList<>(), defaultDeltaTMin, preMERHotUtility, preMERColdUtility );
	}
	
	//
	public void addStream(Stream stream) throws DefinedPropertiesException{
		streamsMap.put(stream.getName(), stream).setIndustrialProcessRef(this);
		updateAll();
	}
	public void removeStream(String streamName) throws DefinedPropertiesException{
		streamsMap.remove(streamName).setIndustrialProcessRef(null);
		updateAll();
	}
	
	public void addColumn(Column column) throws DefinedPropertiesException{
		columnsMap.put(column.getName(), column);
		updateAll();
	}
	public void removeColumn(String columnName) throws DefinedPropertiesException{
		columnsMap.remove(columnName);
		updateAll();
	}
	
	//
	public void updateAll() throws DefinedPropertiesException{
		problemTable.performFeasibleEnergyCascade();
		hxNetwork.designHXNetwork();	
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
		
	public void setDeltaTMin(double deltaTMin) throws DefinedPropertiesException{
		streamsMap.values().stream().forEach(s -> s.setShiftTemps(deltaTMin));
		columnsMap.values().stream().forEach(c -> c.setShiftTemps(deltaTMin));
		
		problemTable.setDeltaTMin(deltaTMin);
		hxNetwork.designHXNetwork();
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
		hxNetwork.setProblemTable(problemTable);
		this.hxNetwork = hxNetwork;
	}
	
	public Stream getStream(String name){
		return streamsMap.get(name);
	}
	public Column getColumn(String name){
		return columnsMap.get(name);
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
		List<Stream> streams = new ArrayList<Stream>();
		streams.add(new Stream("H1", 270.0f, 58.0f, 0.1f, 0.0f));
		streams.add(new Stream("H2", 220.0f, 60.0f, 10.22f, 0.0f));
		streams.add(new Stream("H3", 252.0f, 241.0f, 4.75f, 0.0f));
		streams.add(new Stream("H4", 192.85f, 131.85f, 5.25f, 0.0f));
		streams.add(new Stream("C1", 46.0f, 170.0f, 0.98f, 0.0f));
		streams.add(new Stream("C2", 160.0f, 240.0f, 10.3f, 0.0f));
		streams.add(new Stream("C3", 25.0f, 35.0f, 11.47f, 0.0f));
		streams.add(new Stream("H5", 444.0f, 70.0f, 0.18f, 0.0f));
		streams.add(new Stream("H6", 165.0f, 20.0f, 10.3f, 0.0f));
		streams.add(new Stream("C4", 25.0f, 35.0f, 1.47f, 0.0f));

		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column( new double[]{50.0f}, new double[]{550.0f}, new double[]{60.0f}, new double[]{350.0f} ));
		columns.add(new Column( new double[]{13.0f}, new double[]{270.0f}, new double[]{40.0f}, new double[]{750.0f} ));
		
		IndustrialProcess ip = new IndustrialProcess(streams, columns, 10.0, 0.0, 0.0);
		System.out.println("Hot Utility: " + ip.getMinimumHotUtility());
		System.out.println("Cold Utility: " + ip.getMinimumColdUtility());
		ip.getPinchTemps().stream().forEach(temp -> System.out.println("An Unshifted Pinch Temp: " + temp));
		System.out.println(ip);
	}
}
