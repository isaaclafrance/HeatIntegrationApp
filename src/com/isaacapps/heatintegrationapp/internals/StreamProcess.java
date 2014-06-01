package com.heatIntegration.internals;
import java.util.ArrayList;
import java.util.Arrays;
import com.heatIntegration.graphics.CompositeCurve;
import com.heatIntegration.graphics.GrandCompositeCurve;

//TODO: Make the grand composite and composite curve object only update their data points only when the associated UI button is pressed
//TODO: Create an class that creates a plot of Pinch Temperature vs. Delta T Min
//TODO: Create an class that creates a plot of QH vs. Delta T Min and QH vs. Delta T Min

public class StreamProcess {
	public ArrayList<Stream> streams;
	public ArrayList<Column> columns;
	private ProblemTable problemTable;
	private GrandCompositeCurve gcCurve;
	private CompositeCurve cCurve;
	
	public StreamProcess(Stream[] streams, Column[] columns, float defaultDeltaTMin){
		this.streams = new ArrayList<Stream>(Arrays.asList(streams));
		this.columns = new ArrayList<Column>(Arrays.asList(columns));
		
		this.problemTable = new ProblemTable(this, defaultDeltaTMin);
		this.problemTable.performFeasibleEnergyCascade();
		
		this.gcCurve = new GrandCompositeCurve(this.problemTable);
		this.cCurve = new CompositeCurve(this, this.problemTable);
	}
	public StreamProcess(float defaultDeltaTMin){
		this(new Stream[0], new Column[0], defaultDeltaTMin);
	}
	
	public float getQH(){
		return problemTable.qH;
	}
	public float getQC(){
		return problemTable.qC;
	}
	public float getPinchTemp(){
		return problemTable.pinchTemp;
	}
	
	public GrandCompositeCurve getGrandCompositeCurve(){
		return gcCurve;
	}
	public CompositeCurve getCompositeCurve(){
		return cCurve;
	}
	
	public void addStream(Stream stream){
		if(stream.getStreamNum()>streams.size()){
			streams.add(stream);
		}else{
			streams.set(stream.getStreamNum(), stream);
		}
		
		problemTable.performFeasibleEnergyCascade();
		gcCurve.updateDataPoints();
		cCurve.updateDataPoints();
	}
	public void removeStream(int streamNum){
		for(Stream stream:streams){
			if(stream.getStreamNum() == streamNum){
				streams.remove(stream);
				break;
			}
		}
		problemTable.performFeasibleEnergyCascade();
		gcCurve.updateDataPoints();
		cCurve.updateDataPoints();
	}
	
	public void addColumn(Column column){
		if(column.getColNum()>columns.size()){
			columns.add(column);
		}else{
			columns.set(column.getColNum(), column);
		}
		
		problemTable.performFeasibleEnergyCascade();
		gcCurve.updateDataPoints();
		cCurve.updateDataPoints();
	}
	public void removeColumn(int colNum){
		for(Column column:columns){
			if(column.getColNum() == colNum){
				columns.remove(column);
				break;
			}
		}
		problemTable.performFeasibleEnergyCascade();
		gcCurve.updateDataPoints();
		cCurve.updateDataPoints();
	}
}
