package com.isaacapps.heatintegrationapp.internals;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

import com.isaacapps.heatintegrationapp.graphics.PinchTempVsDT;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Column;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.DefinedPropertiesException;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class IndustrialProcess {
	private Map<String,Stream> streamsMap;
	private Map<String,Column> columnsMap;
	private ProblemTable problemTable;
	private HXNetwork hxNetwork;
	private CostEvaluator costEvaluator;
	private double preMERHotUtility;
	private double preMERColdUtility;
	private String preMERUtilityUnit;
	
	//
	public IndustrialProcess(List<Stream> streams, List<Column> columns, double deltaTMin, double preMERHotUtility, double preMERColdUtility, String preMERUtilityUnit) throws DefinedPropertiesException{
		this(preMERHotUtility, preMERColdUtility, preMERUtilityUnit);
		streamsMap = streams.stream().collect(toMap(Stream::getName, s->s));	
		streamsMap.forEach((n,s)->s.setShiftTemps(deltaTMin));
		columnsMap = columns.stream().collect(toMap(Column::getName, c->c));
		columnsMap.forEach((n,c)->c.setShiftTemps(deltaTMin));		
		problemTable = new ProblemTable(new ArrayList<>(streamsMap.values()), new ArrayList<>(columnsMap.values()), deltaTMin);
		
		hxNetwork = new HXNetwork(problemTable);
		costEvaluator = new CostEvaluator(this);
	}
	public IndustrialProcess(double defaultDeltaTMin, double preMERHotUtility, double preMERColdUtility, String preMERUtilityUnit) throws DefinedPropertiesException{
		this(new ArrayList<>(), new ArrayList<>(), defaultDeltaTMin, preMERHotUtility, preMERColdUtility, preMERUtilityUnit);
	}
	public IndustrialProcess(double preMERHotUtility, double preMERColdUtility, String preMERUtilityUnit) {
		this.preMERHotUtility = preMERHotUtility;
		this.preMERColdUtility = preMERColdUtility;
		this.preMERUtilityUnit = preMERUtilityUnit;
	}
	
	//
	public boolean addStream(Stream stream){
		streamsMap.put(stream.getName(), stream).setIndustrialProcessRef(this);
		return updateAll();
	}
	public boolean removeStream(String streamName){
		streamsMap.remove(streamName).setIndustrialProcessRef(null);
		return updateAll();
	}
	
	public boolean addColumn(Column column){
		columnsMap.put(column.getName(), column);
		return updateAll();
	}
	public boolean removeColumn(String columnName){
		columnsMap.remove(columnName);
		return updateAll();
	}
	
	//
	public boolean updateAll(){
		try{
			boolean state;
			
			problemTable.performFeasibleEnergyCascade();	
			state = hxNetwork.designHXNetwork();	
			costEvaluator.calculateGlobalCost();
			
			return state;
		}
		catch(Exception e){
			return false;
		}
	}
	
	//
	public double getPreMERHotUtility(){
		return preMERHotUtility;
	}
	public String getPreMERHotUtilityWithUnit(){
		return preMERHotUtility+" "+preMERUtilityUnit;
	}
	public double getPreMERColdUtility(){
		return preMERColdUtility;
	}
	public String getPreMERColdUtilityWithUnit(){
		return preMERColdUtility+" "+preMERUtilityUnit;
	}
		
	public void setDeltaTMin(double deltaTMin) throws DefinedPropertiesException{		
		problemTable.setDeltaTMin(deltaTMin);
		hxNetwork.designHXNetwork();
	}
	public double getDeltaTMin(){
		return problemTable.getDeltaTMin();
	}
	
	public ProblemTable getProblemTable(){
		return problemTable;
	}
	public void setProblemTable( ProblemTable problemTable){
		this.problemTable = problemTable;
		
	}
	
	public HXNetwork getHXNetwork(){
		return hxNetwork;
	}
	public void setHXNetwork(HXNetwork hxNetwork){
		hxNetwork.setProblemTable(problemTable);
		this.hxNetwork = hxNetwork;
	}
	
	public CostEvaluator getCostEvaluator(){
		return costEvaluator;
	}
	public void setGlobalCostEvaluator( CostEvaluator costEvaluator){
		costEvaluator.setIndustrialProcess(this);
		this.costEvaluator = costEvaluator;
	}
	
	public Stream getStream(String name){
		return streamsMap.get(name);
	}
	public Column getColumn(String name){
		return columnsMap.get(name);
	}
	
	//
	public String toString(){
		return String.format("{\"Industrial Process\": {\"preMERHotUtility\": \"%s\", \"preMERColdUtility\": \"%s\" \n, \"streams\": [%s] \n, \"columns\": [%s] \n, %s \n, %s \n, %s}}#"
				             , getPreMERHotUtilityWithUnit()
				             , getPreMERColdUtilityWithUnit()
							 , streamsMap.values().stream().map(s->s.toString()).reduce((prev,curr)->prev+","+curr.substring(curr.indexOf(":")+1)).orElse("")
				             , columnsMap.values().stream().map(c->c.toString()).reduce((prev,curr)->prev+","+curr.substring(curr.indexOf(":")+1)).orElse("")
				             , hxNetwork.toString()
				             , costEvaluator.toString()
				             , problemTable.toString());
	}
	
	//////////TODO: Should be moved to the IndustrialProcess unit test class
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
		
		IndustrialProcess ip = new IndustrialProcess(streams, columns, 10.0, 0.0, 0.0, "kW");
		System.out.println(ip);
		
		PinchTempVsDT ptVSdt = new PinchTempVsDT(ip.problemTable, 10, 100, 50);
		ptVSdt.updateGraph();
		List<double[][]> dataPointsList = ptVSdt.getListOfPinchCurveDataPoints();
	}
}
