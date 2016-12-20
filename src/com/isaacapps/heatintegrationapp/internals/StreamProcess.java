package com.isaacapps.heatintegrationapp.internals;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.isaacapps.heatintegrationapp.graphics.CompositeCurve;
import com.isaacapps.heatintegrationapp.graphics.GrandCompositeCurve;

//TODO: Make the grand composite and composite curve object only update their data points only when the associated UI button is pressed
//TODO: Create an class that creates a plot of Pinch Temperature vs. Delta T Min
//TODO: Create an class that creates a plot of QH vs. Delta T Min and QH vs. Delta T Min

public class StreamProcess {
	public Map<String,Stream> streams;
	public Map<String,Column> columns;
	private ProblemTable problemTable;
	private GrandCompositeCurve gcCurve;
	private CompositeCurve cCurve;
	private float initialHotUtility;
	
	public StreamProcess(Stream[] streams, Column[] columns, float defaultDeltaTMin, float initialHotUtility){
		this.streams = new HashMap<String, Stream>();
		for(Stream stream:streams){
			stream.setShiftInletTemperature(defaultDeltaTMin);
			stream.setShiftOutletTemperature(defaultDeltaTMin);
			this.streams.put(stream.getStreamName(), stream);
		}
		this.columns = new HashMap<String, Column>();
		for(Column column:columns){
			column.setCondShiftTemperature(defaultDeltaTMin);
			column.setRebShiftTemperature(defaultDeltaTMin);
			this.columns.put(column.getColName(), column);
		}
		
		this.initialHotUtility = initialHotUtility;
		
		this.problemTable = new ProblemTable(this, defaultDeltaTMin);
		this.problemTable.performFeasibleEnergyCascade(initialHotUtility);
		
		this.gcCurve = new GrandCompositeCurve(this.problemTable);
		this.cCurve = new CompositeCurve(this);
		

	}
	public StreamProcess(float defaultDeltaTMin, float initialHotUtility){
		this(new Stream[0], new Column[0], defaultDeltaTMin, initialHotUtility );
	}
	
	public float getInitialHotUtility(){
		return initialHotUtility;
	}
	public float getInitialColdUtility(){
		return getColdUtility() - (getHotUtility() - getInitialHotUtility());
	}	
	public float getHotUtility(){
		return problemTable.getQH();
	}
	public float getColdUtility(){
		return problemTable.getQC();
	}
	public ArrayList<Float> getUnshiftedPinchTemps(){
		return problemTable.getUnshiftedPinchTemps();
	}
	public ArrayList<Float> getShiftedPinchTemps(){
		return problemTable.getShiftPinchTemps();
	}
	
	public GrandCompositeCurve getGrandCompositeCurve(){
		return gcCurve;
	}
	public CompositeCurve getCompositeCurve(){
		return cCurve;
	}
	
	public ProblemTable getProblemTable(){
		return problemTable;
	}
	
	public void addStream(Stream stream){
		streams.put(stream.getStreamName(), stream);
		update();
	}
	public void removeStream(String streamName){
		streams.remove(streamName);
		update();
	}
	
	public void addColumn(Column column){
		columns.put(column.getColName(), column);
		update();
	}
	public void removeColumn(String columnName){
		columns.remove(columnName);
		update();
	}
	
	public void update(){
		problemTable.performFeasibleEnergyCascade(initialHotUtility);
		gcCurve.updateGraph();;
		cCurve.updateGraph();;
	}
	
	///
	public static void main(String[] args){
		StreamProcess sp = new StreamProcess(new Stream[]{new Stream("H1", 450.0f, 40.0f, 0.0f, 1006.0f),new Stream("C1", 129.9f, 280.0f, 0.0f, 57.4f)}, new Column[]{new Column(1, 129.9f, 95.0f, 35.3f, 35.7f), new Column(2, 40, 450, 20, 250 )}, 10.0f, 0.0f);
		System.out.println("Hot Utility: " + sp.getHotUtility());
		System.out.println("Cold Utility: " + sp.getColdUtility());
		if(!sp.getShiftedPinchTemps().isEmpty())
			System.out.println("An Unshifted Pinch Temp: " + sp.getUnshiftedPinchTemps().get(0));
		System.out.println(sp.problemTable.toString());
	}
}
