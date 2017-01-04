package com.isaacapps.heatintegrationapp.internals;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import java.util.stream.*;
import static java.util.stream.Collectors.*;

import com.isaacapps.heatintegrationapp.graphics.*;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.*;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class ProblemTable {
	private List<Stream> streams;
	private List<Column> columns;
	private List<CascadeInterval> cascadeIntervals;
	private double merQH, merQC, deltaTMin;
	private List<Set<Double>> pinchTemps; // [0] -> shifted pinch temps, [1] --> unshifted pinch temps
	private Map<Double, Set<CascadeInterval>> shiftTemp_Interval_Dict;
	private GrandCompositeCurve gcCurve;
	private CompositeCurve cCurve;
	private PinchTempVsDT ptVsDt;
	private UtilityVsDT uVsDt;
	private EnergyCascadeDiagram ecDiagram;
	private DecimalFormat tempFormat, heatFormat;
	private Function<Double, Double> formatHeat, formatTemp;

	//
	public ProblemTable(List<Stream> streams, List<Column> columns, double deltaTMin) throws DefinedPropertiesException {
		this.streams = streams;
		this.columns = columns;
		this.merQH = this.merQC = 0.0f;
		this.deltaTMin = deltaTMin;
		
		tempFormat = new DecimalFormat("#.##");
		heatFormat = new DecimalFormat("#.####");
		formatHeat = (h)-> Double.parseDouble(heatFormat.format(h));
		formatTemp = (T)-> Double.parseDouble(tempFormat.format(T));
		cascadeIntervals = new ArrayList<>();
		shiftTemp_Interval_Dict = new HashMap<Double, Set<CascadeInterval>>();
		
		pinchTemps = new ArrayList<Set<Double>>();
		for (int i = 0; i < 2; i++) 
			this.pinchTemps.add(new HashSet<Double>());
		
		performFeasibleEnergyCascade();
	}

	//
	private void setupCascadeIntervals() throws DefinedPropertiesException {
		createCasacadeInterval(createShiftTempVec());
		calculateIntervalSpecificHeat();
		calculateIntervalHeatLoads();
	}

	private List<Double> createShiftTempVec() {
		Set<Double> shiftTempVec = new HashSet<Double>();
		
		for (EnergyTransferElement stream : streams) {
			shiftTempVec.add(stream.getSourceShiftTemp());
			shiftTempVec.add(stream.getTargetShiftTemp());
		}

		// Adds shift temperature of column elements treated as streams only as is needed. There needs to be two identical shift temps for every column element.
		long count;
		for (EnergyTransferElement column : columns.stream().flatMap(c -> java.util.stream.Stream.concat(c.getCondensers().stream(),c.getReboilers().stream()))
				.collect(toList())) {
			count = shiftTempVec.stream().filter(t -> t == column.getTargetShiftTemp()||t == column.getTargetShiftTemp()).count();
			if (count == 0) {
				shiftTempVec.add(column.getTargetShiftTemp());
				shiftTempVec.add(column.getSourceShiftTemp());
			} else if (count == 1) {
				shiftTempVec.add(column.getSourceShiftTemp());
			}
		}

		return shiftTempVec.stream().sorted().collect(Collectors.toList());
	}
	private void createCasacadeInterval(List<Double> shiftTempVec) throws DefinedPropertiesException {
		int cascadeIndex = 0;
		
		cascadeIntervals.clear();
		for (int i = shiftTempVec.size() - 1; i > 0; i--) {
			double temp1 = shiftTempVec.get(i);
			double temp2 = shiftTempVec.get(i - 1);
			CascadeInterval cascadeInterval = new CascadeInterval(temp1, temp2, 0.0f, 0.0f, 0.0f,
					(Math.abs(temp1 - temp2) >= Column.UTILITY_TEMP_DIFF*2) ? "Stream Interval Containing: " : "Column Interval Containing: ", cascadeIndex++);

			cascadeIntervals.add(cascadeInterval);

			if (!shiftTemp_Interval_Dict.containsKey(temp1)) {
				shiftTemp_Interval_Dict.put(temp1, new HashSet<CascadeInterval>());
			}
			if (!shiftTemp_Interval_Dict.containsKey(temp2)) {
				shiftTemp_Interval_Dict.put(temp2, new HashSet<CascadeInterval>());
			}

			shiftTemp_Interval_Dict.get(temp1).add(cascadeInterval);
			shiftTemp_Interval_Dict.get(temp2).add(cascadeInterval);
		}
	}
	private void calculateIntervalSpecificHeat() throws DefinedPropertiesException {				
		BiConsumer<CascadeInterval, EnergyTransferElement> setHeatTransferCoeff = (cI, et) -> {
			if (et.getType().equals("Hot") && cI.getName().contains("Stream")) {
				cI.setHeatTransferCoeff(cI.getHeatTransferCoeff() + et.getHeatTransferCoeff());
			} else if(et.getType().equals("Cold")&& cI.getName().contains("Stream")) {
				cI.setHeatTransferCoeff(cI.getHeatTransferCoeff() - et.getHeatTransferCoeff());
			}};
		
		BiConsumer<CascadeInterval, EnergyTransferElement> setEnergyTransferersCrossingInterval = (cI, et) -> {
			if (et.getClass() == Stream.class && cI.getName().contains("Stream")|| ColumnEnergyTransferElement.class.isAssignableFrom(et.getClass()) 
					                                                               && !cI.getName().contains("Stream")) {
				cI.getEnergyTransferersCrossingInterval().add(et);
			}};
				
		Consumer<CascadeInterval> updateIntervalWithEnergyTransferElements = cI -> 
				 java.util.stream.Stream.concat(streams.stream(), columns.stream().flatMap(c -> java.util.stream.Stream.concat(c.getReboilers().stream()
						                                                                   ,c.getCondensers().stream())))
				.filter(et -> notOutsideOfTempRange(et, cI.getSourceShiftTemp(), cI.getTargetShiftTemp()))
				.forEach(et -> {setHeatTransferCoeff.accept(cI, et); setEnergyTransferersCrossingInterval.accept(cI, et);});	
		
		Consumer<CascadeInterval> setIntervalName = cI -> 
				 cI.setName(cI.getName() + cI.getEnergyTransferersCrossingInterval().stream().map(et -> et.getName()).sorted().reduce((prev, next) -> prev + ", " + next).orElse(""));
			
		cascadeIntervals.stream().forEach(cI -> {updateIntervalWithEnergyTransferElements.accept(cI); setIntervalName.accept(cI);});
	}
	private boolean notOutsideOfTempRange(EnergyTransferElement stream, double intervalTemp1, double intervalTemp2){
		double streamTemp1 = stream.getType().equalsIgnoreCase("cold")?stream.getSourceShiftTemp() : stream.getTargetShiftTemp();
		double streamTemp2 = stream.getType().equalsIgnoreCase("hot")?stream.getSourceShiftTemp() : stream.getTargetShiftTemp();	
		return !(intervalTemp1 <= streamTemp1 && intervalTemp2 <= streamTemp1 )
				&& !(intervalTemp1 >= streamTemp2 && intervalTemp2 >= streamTemp2);
	}
	
	private void calculateIntervalHeatLoads() throws DefinedPropertiesException {
		for (CascadeInterval interval : cascadeIntervals) {
			if (interval.getName().contains("Stream")) {
				interval.setHeatLoad(interval.getHeatTransferCoeff() * (interval.getSourceShiftTemp() - interval.getTargetShiftTemp()));
			} else { //Column energy transfer elements
				for(EnergyTransferElement colET:interval.getEnergyTransferersCrossingInterval())
					interval.setHeatLoad(interval.getHeatLoad()-colET.getHeatLoad());
			}
		}
	}

	private List<Integer> findPinchIntervalIndices(int infesibleIndex) {
		double maxDiff = 0.000001;
		return cascadeIntervals.stream().parallel()
				.filter(cI -> Math.abs(cI.getCascadeEnergy() - cascadeIntervals.get(infesibleIndex).getCascadeEnergy()) <= maxDiff
						      && cI.getIndex() != cascadeIntervals.size() - 1)
				.map(cI -> cI.getIndex()).collect(toList());
	}

	private int findInfeasibleIntervalIndex() {
		return cascadeIntervals.stream()
			   .min(new Comparator<CascadeInterval>() {
						@Override
						public int compare(final CascadeInterval lf, CascadeInterval rt) {
							return Double.compare(lf.getCascadeEnergy(), rt.getCascadeEnergy());
						}}
		            ).get().getIndex();
	}
	private void setUnshiftedPinchTemps() {
		// Unshifted pinch temps based on whether shifted pinch temps were the
		// source or target temperatures of cold of hot streams.
		pinchTemps.set(1, new HashSet<Double>());
		for (double pinchShiftTemp : pinchTemps.get(0)) {
			pinchTemps.get(1).addAll(shiftTemp_Interval_Dict.get(pinchShiftTemp).stream().parallel()
					.flatMap(c -> c.getEnergyTransferersCrossingInterval().stream())
					.filter(s -> s.getSourceShiftTemp() == pinchShiftTemp || s.getTargetShiftTemp() == pinchShiftTemp)
					.map(s -> (s.getSourceShiftTemp() == pinchShiftTemp)?s.getSourceTemp():s.getTargetTemp()) 
					.collect(toSet()));
		}
	}
	private void performEnergyCascade(double qHAdjustment) {
		cascadeIntervals.get(0).setCascadeEnergy(qHAdjustment + cascadeIntervals.get(0).getHeatLoad());
		
		cascadeIntervals.stream().filter(cI->cI.getIndex()>0)
		                         .forEachOrdered(cI->cI.setCascadeEnergy(cI.getHeatLoad()+cascadeIntervals.get(cI.getIndex()-1).getCascadeEnergy()));
		merQH = qHAdjustment;
		merQC = cascadeIntervals.get(cascadeIntervals.size() - 1).getCascadeEnergy();
	}
	public void performFeasibleEnergyCascade() throws DefinedPropertiesException {
		setupCascadeIntervals();

		performEnergyCascade(0.0f);

		int infesibleIndex = findInfeasibleIntervalIndex();
		double infesibleCascadeEnergy = cascadeIntervals.get(infesibleIndex).getCascadeEnergy();	
		
		if (infesibleCascadeEnergy < 0) performEnergyCascade(-infesibleCascadeEnergy);

		pinchTemps.set(0, new HashSet<Double>());
		findPinchIntervalIndices(infesibleIndex).stream().forEach(pIndex->pinchTemps.get(0).add(cascadeIntervals.get(pIndex).getTargetShiftTemp()));
		setUnshiftedPinchTemps();
		
		formatTempNHeatValuesOfCascadeElements();
		formatMERElements();
	}
	
	private void formatTempNHeatValuesOfCascadeElements() throws DefinedPropertiesException{
		for(CascadeInterval cI:cascadeIntervals){
			cI.setHeatLoad(formatHeat.apply(cI.getHeatLoad()));
			cI.setCascadeEnergy(formatHeat.apply(cI.getCascadeEnergy()));
			cI.setSourceTemp(formatTemp.apply(cI.getSourceShiftTemp()));
			cI.setTargetTemp(formatTemp.apply(cI.getTargetShiftTemp()));
			cI.setHeatTransferCoeff(formatTemp.apply(cI.getHeatTransferCoeff())); //Give heat transfer coeff same formating as temperature 
		}
	}
	private void formatMERElements(){
		merQH = formatHeat.apply(merQH);
		merQC = formatHeat.apply(merQC);
		
		pinchTemps = pinchTemps.stream().map(pSet -> pSet.stream().map(formatTemp)
				                                                  .collect(toSet()))
				                        .collect(toList());
	}
	
	//
	public Set<Double> getShiftPinchTemps() {
		return pinchTemps.get(0);
	}
	public Set<Double> getUnshiftedPinchTemps() {
		return pinchTemps.get(1);
	}
	public Set<String> getShiftPinchTempsWithUnit() {
		return getPinchTempsWithUnit(0);
	}
	public Set<String> getUnshiftedPinchTempsWithUnit() {
		return getPinchTempsWithUnit(1);
	}
	private Set<String> getPinchTempsWithUnit(int index) {
		String tempUnit = cascadeIntervals.stream().findFirst().map(c->c.getTempUnit()).orElse("");
		return pinchTemps.get(index).stream().map(T->T+" "+tempUnit).collect(toSet());
	}
	
	public List<CascadeInterval> getCascadeIntervals() {
		return cascadeIntervals;
	}

	public double getMERQH() {
		return merQH;
	}
	public String getMerQhWithUnit(){
		return getMERQH() +" "+ cascadeIntervals.stream().findFirst().map(c->c.getHeatUnit()).orElse("");
	}
	public double getMERQC() {
		return merQC;
	}
	public String getMerQcWithUnit(){
		return getMERQC() +" "+ cascadeIntervals.stream().findFirst().map(c->c.getHeatUnit()).orElse("");
	}
	
	public double getDeltaTMin() {
		return deltaTMin;
	}
	public void setDeltaTMin(double deltaTMin){
		this.deltaTMin = deltaTMin;
		streams.stream().forEach(s -> s.setShiftTemps(deltaTMin));
		columns.stream().forEach(c -> c.setShiftTemps(deltaTMin));
		try {performFeasibleEnergyCascade();} catch (DefinedPropertiesException e) {}
	}

	public GrandCompositeCurve getGrandCompositeCurve(){
		//Only perform the curve calculations if necessary
		if(gcCurve == null)
			gcCurve = new GrandCompositeCurve(this);
		return gcCurve;
	}
	public CompositeCurve getCompositeCurve(){
		//Only perform the curve calculations if necessary
		if(cCurve == null)
			cCurve = new CompositeCurve(streams, merQH);
		return cCurve;
	}
	public PinchTempVsDT getPinchTempVsDT(){
		//Only perform the curve calculations if necessary
		if(ptVsDt == null)
			try {ptVsDt = new PinchTempVsDT(this);} catch (DefinedPropertiesException e) {}			
		return ptVsDt;
	}
	public UtilityVsDT getUtilityVsDT(){
		//Only perform the curve calculations if necessary
		if(uVsDt == null)
			uVsDt = new UtilityVsDT(this);
		return uVsDt;
	}
	
	private EnergyCascadeDiagram getEnergyCascadeDiagram(){
		if(ecDiagram == null)
			ecDiagram = new EnergyCascadeDiagram(this);
		return ecDiagram;
	}
	
	public List<Stream> getStreams(){
		return streams;
	}
	public List<Column> getColumns(){
		return columns;
	}
	
	//
	@Override
	public String toString() {
		return String.format("\"ProblemTable\":{\"Unshifted Pinch Temps\":\"%s\", \"MER QH\":\"%s\", \"MER QC\":\"%s\", \"energyCascade\":\"%s\"}"
				             ,getShiftPinchTempsWithUnit().stream().reduce((prev,curr)->prev+", "+curr).orElse("")
				             ,getMerQhWithUnit()
				             ,getMerQcWithUnit()
				             ,getEnergyCascadeDiagram());
	}
	
}
