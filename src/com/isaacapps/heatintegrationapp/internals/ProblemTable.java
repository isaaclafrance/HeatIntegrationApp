package com.isaacapps.heatintegrationapp.internals;

import java.util.*;
import java.util.stream.*;

import com.isaacapps.heatintegrationapp.graphics.*;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Column;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.EnergyTransferElement;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.Stream;

public class ProblemTable {
	private List<Stream> streams;
	private List<Column> columns;
	private List<CascadeInterval> cascadeIntervals;
	private double merQH, merQC, deltaTMin;
	private List<Set<Double>> pinchTemps; // [0] -> shifted pinch temps, [1] --> unshifted pinch temps
	Map<Double, Set<CascadeInterval>> shiftTemp_Interval_Dict;
	private GrandCompositeCurve gcCurve;
	private CompositeCurve cCurve;

	//
	public ProblemTable(List<Stream> streams, List<Column> columns, double deltaTMin) {
		this.streams = streams;
		this.columns = columns;
		this.merQH = this.merQC = 0.0f;
		this.deltaTMin = deltaTMin;
		cascadeIntervals = new ArrayList<>();
		pinchTemps = new ArrayList<Set<Double>>();
		shiftTemp_Interval_Dict = new HashMap<Double, Set<CascadeInterval>>();
		for (int i = 0; i < 2; i++) {
			this.pinchTemps.add(new HashSet<Double>());
		}
		performFeasibleEnergyCascade();
	}

	//
	private void setupCascadeIntervals() {
		createCasacadeInterval(createShiftTempVec());
		calculateIntervalSpecificHeat();
		calculateIntervalHeatLoads();
	}

	private List<Double> createShiftTempVec() {
		List<Double> shiftTempVec = new ArrayList<>();

		//
		for (EnergyTransferElement stream : streams) {
			if (!shiftTempVec.contains(stream.getSourceShiftTemp())) {
				shiftTempVec.add(stream.getSourceShiftTemp());
			}

			if (!shiftTempVec.contains(stream.getTargetShiftTemp())) {
				shiftTempVec.add(stream.getTargetShiftTemp());
			}
		}

		// Finds shift temperature of columnsMap treated as streamsMap
		long count;
		for (EnergyTransferElement column : columns.stream().flatMap(c -> c.getCondensers().stream())
				.collect(Collectors.toList())) {
			count = shiftTempVec.stream().filter(t -> t == column.getTargetShiftTemp()).count();
			if (count == 0) {
				shiftTempVec.add(column.getTargetShiftTemp());
				shiftTempVec.add(column.getSourceShiftTemp());
			} else if (count == 1) {
				shiftTempVec.add(column.getSourceShiftTemp());
			}
		}
		for (EnergyTransferElement column : columns.stream().flatMap(c -> c.getReboilers().stream())
				.collect(Collectors.toList())) {
			count = shiftTempVec.stream().filter(t -> t == column.getSourceShiftTemp()).count();
			if (count == 0) {
				shiftTempVec.add(column.getSourceShiftTemp());
				shiftTempVec.add(column.getTargetShiftTemp());
			} else if (count == 1) {
				shiftTempVec.add(column.getTargetShiftTemp());
			}
		}

		Collections.sort(shiftTempVec);

		return shiftTempVec;
	}
	private void createCasacadeInterval(List<Double> shiftTempVec) {
		int cascadeIndex = 0;
		
		cascadeIntervals.clear();
		for (int i = shiftTempVec.size() - 1; i > 0; i--) {
			double temp1 = shiftTempVec.get(i);
			double temp2 = shiftTempVec.get(i - 1);
			CascadeInterval cascadeInterval = new CascadeInterval(temp1, temp2, 0.0f, 0.0f, 0.0f,
					(Math.abs(temp1 - temp2) >= Column.UTILITY_TEMP_DIFF*2) ? "Stream Interval Containing: " : "", cascadeIndex++);

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
	private void calculateIntervalSpecificHeat() {
		for (CascadeInterval interval : cascadeIntervals) {
			if (interval.getType().contains("Stream")) {
				for (Stream stream : streams) {
					if (notOutsideOfTempRange(stream, interval.getTemp1(), interval.getTemp2())) {
						if (stream.getType().equals("Hot")) {
							interval.setTotalCP(interval.getTotalCP() + stream.getHeatTransferCoeff());
						} else {
							interval.setTotalCP(interval.getTotalCP() - stream.getHeatTransferCoeff());
						}

						interval.getEnergyTransferersCrossingInterval().add(stream);
					}
				}
				interval.setType(interval.getType() + interval.getEnergyTransferersCrossingInterval().stream()
						.map(et -> et.getName()).reduce(" ", (prev, next) -> prev + ", " + next));
				
			}
			else{
				List<EnergyTransferElement> a = columns.stream()
						.flatMap(c -> java.util.stream.Stream.concat(c.getReboilers().stream(),
								c.getCondensers().stream()))
						.collect(Collectors.toList());
				
				for (EnergyTransferElement columnEnergyTransferer : columns.stream()
						.flatMap(c -> java.util.stream.Stream.concat(c.getReboilers().stream(),
								c.getCondensers().stream()))
						.collect(Collectors.toList())) {
					if (notOutsideOfTempRange(columnEnergyTransferer, interval.getTemp1(), interval.getTemp2())) {
						interval.getEnergyTransferersCrossingInterval().add(columnEnergyTransferer);
						interval.setType(columnEnergyTransferer.getName());
					}
				}
			}	
		}
	}
	private boolean notOutsideOfTempRange(EnergyTransferElement stream, double intervalTemp1, double intervalTemp2){
		return !(intervalTemp1 <= (stream.getType().equalsIgnoreCase("cold") ? stream.getSourceShiftTemp()
						                                                     : stream.getTargetShiftTemp())
				&& intervalTemp2 <= (stream.getType().equalsIgnoreCase("cold")? stream.getSourceShiftTemp() 
						                                                      : stream.getTargetShiftTemp()))
				&& !(intervalTemp1 >= (stream.getType().equalsIgnoreCase("hot")? stream.getSourceShiftTemp() : stream.getTargetShiftTemp())
						&& intervalTemp2 >= (stream.getType().equalsIgnoreCase("hot")
								? stream.getSourceShiftTemp() : stream.getTargetShiftTemp()));
	}
	
	private void calculateIntervalHeatLoads() {
		for (CascadeInterval interval : cascadeIntervals) {
			if (interval.getType().contains("Stream")) {
				interval.setHeatLoad(interval.getTotalCP() * (interval.getTemp1() - interval.getTemp2()));
			} else {
				if(interval.getEnergyTransferersCrossingInterval().size()>0){
					EnergyTransferElement columnEnergyTransferer = interval.getEnergyTransferersCrossingInterval().get(0);
					
					interval.setHeatLoad(-columnEnergyTransferer.getHeatLoad()); 
					interval.setType(columnEnergyTransferer.getName());
					interval.getEnergyTransferersCrossingInterval().add(columnEnergyTransferer);
				}
			}
		}
	}

	private List<Integer> findPinchIntervalIndices(int infesibleIndex) {
		double maxDiff = 0.0001;

		return cascadeIntervals.stream()
				.filter(cI -> Math
						.abs(cI.getCascadeEnergy() - cascadeIntervals.get(infesibleIndex).getCascadeEnergy()) <= maxDiff
						&& cI.getCascadeIndex() != cascadeIntervals.size() - 1)
				.map(cI -> cI.getCascadeIndex()).collect(Collectors.toList());
	}

	private int findInfeasibleIntervalIndices() {
		return cascadeIntervals.stream().min(new Comparator<CascadeInterval>() {
			@Override
			public int compare(final CascadeInterval lf, CascadeInterval rt) {
				return Double.compare(lf.getCascadeEnergy(), rt.getCascadeEnergy());
			}
		}).get().getCascadeIndex();
	}
	private void calculateUnshiftedPinchTemps() {
		// Unshifted pinch temps based on whether shifted pinch temps were the
		// source or target temperatures of cold of hot streams.
		java.util.stream.Stream<EnergyTransferElement> streamTouchingPinch;
		for (double pinchShiftTemp : pinchTemps.get(0)) {
			streamTouchingPinch = shiftTemp_Interval_Dict.get(pinchShiftTemp).stream()
					.flatMap(c -> c.getEnergyTransferersCrossingInterval().stream())
					.filter(s -> s.getSourceShiftTemp() == pinchShiftTemp || s.getTargetShiftTemp() == pinchShiftTemp);

			pinchTemps.get(1)
					  .addAll(streamTouchingPinch.map(
							s -> (s.getSourceShiftTemp() == pinchShiftTemp) ? s.getSourceTemp() : s.getTargetTemp())
							.collect(Collectors.toSet()));
		}
	}
	private void performEnergyCascade(double qHAdjustment) {
		cascadeIntervals.get(0).setCascadeEnergy(qHAdjustment + cascadeIntervals.get(0).getHeatLoad());
		for (int i = 1; i < cascadeIntervals.size(); i++) {
			cascadeIntervals.get(i).setCascadeEnergy(
					cascadeIntervals.get(i - 1).getCascadeEnergy() + cascadeIntervals.get(i).getHeatLoad());
		}

		merQH = qHAdjustment;
		merQC = cascadeIntervals.get(cascadeIntervals.size() - 1).getCascadeEnergy();
	}
	public void performFeasibleEnergyCascade() {
		setupCascadeIntervals();

		performEnergyCascade(0.0f);

		int infesibleIndex = findInfeasibleIntervalIndices();
		double infesibleCascadeEnergy = cascadeIntervals.get(infesibleIndex).getCascadeEnergy();
		
		if (infesibleCascadeEnergy < 0) performEnergyCascade(-infesibleCascadeEnergy);

		findPinchIntervalIndices(infesibleIndex).stream().forEach((pIndex)->pinchTemps.get(0).add(cascadeIntervals.get(pIndex).getTemp2()));
		
		calculateUnshiftedPinchTemps();
		
		updateGraphs();
	}

	private void updateGraphs(){
		getGrandCompositeCurve().updateGraph();
		getCompositeCurve().updateGraph();
	}
	
	//
	public Set<Double> getShiftPinchTemps() {
		return pinchTemps.get(0);
	}
	public Set<Double> getUnshiftedPinchTemps() {
		return pinchTemps.get(1);
	}
	public List<CascadeInterval> getCascadeIntervals() {
		return cascadeIntervals;
	}

	public double getMERQH() {
		return merQH;
	}
	public double getMERQC() {
		return merQC;
	}
	
	public double getDeltaTMin() {
		return deltaTMin;
	}
	public void setDeltaTMin(double deltaTMin) {
		this.deltaTMin = deltaTMin;
		performFeasibleEnergyCascade();
	}

	public GrandCompositeCurve getGrandCompositeCurve(){
		if(gcCurve == null){
			gcCurve = new GrandCompositeCurve(this);
		}
		return gcCurve;
	}
	public CompositeCurve getCompositeCurve(){
		if(cCurve == null){
			cCurve = new CompositeCurve(streams, merQH);
		}
		return cCurve;
	}
	
	//
	private String printEnergyCascade() {
		String cascade = "", boxTopNBottom = "", arrowPadding = "", heatUnit = " kW", tempUnit = " K",
				merQHStr = "MER QH: " + merQH + heatUnit;
		int maxTempNLoadCharLenth = merQHStr.length();
		int initialTempNLoadPadding = 1;

		for (CascadeInterval cascadeInterval : cascadeIntervals) {
			if ((cascadeInterval.getTemp2() + tempUnit).length() > maxTempNLoadCharLenth) {
				maxTempNLoadCharLenth = (cascadeInterval.getTemp2() + tempUnit).length();
			}
			if ((cascadeInterval.getHeatLoad() + heatUnit).length() > maxTempNLoadCharLenth) {
				maxTempNLoadCharLenth = (cascadeInterval.getHeatLoad() + heatUnit).length();
			}
		}

		boxTopNBottom = new String(new char[maxTempNLoadCharLenth]).replace("\0", "*");
		arrowPadding = new String(new char[(int) Math.floor(maxTempNLoadCharLenth / 2.0f)]).replace("\0", " ");

		//Minimum Energy Requirement QH
		cascade = String.format("\n%2$s \n%1$s| \n%1$sV \n%3$s", arrowPadding,
								new String(
										new char[(initialTempNLoadPadding + maxTempNLoadCharLenth - merQHStr.length()) / 2])
												.replace("\0"," ")
										+ merQHStr
								,new String(new char[(initialTempNLoadPadding + maxTempNLoadCharLenth
										- (cascadeIntervals.get(0).getTemp1() + tempUnit).length()) / 2]).replace("\0", " ")
										+ cascadeIntervals.get(0).getTemp1() + tempUnit);

		//Cascades
		for (CascadeInterval cascadeInterval : cascadeIntervals) {
			cascade += String.format("\n#%1$s# \n#%2$s%2$s # \n#%3$s %4$s \n#%2$s%2$s # \n#%1$s# \n%2$s| \n%5$s \n%2$s| \n%2$sV \n%6$s",
									boxTopNBottom, arrowPadding,
									new String(new char[(initialTempNLoadPadding + maxTempNLoadCharLenth
											- (cascadeInterval.getHeatLoad() + heatUnit).length()) / 2]).replace("\0", " ")
											+ cascadeInterval.getHeatLoad() + heatUnit
									,((!cascadeInterval.getType().contains("Stream")) ? " " + cascadeInterval.getType(): "")
									,new String(
											new char[(1 + initialTempNLoadPadding + maxTempNLoadCharLenth
													- (cascadeInterval.getCascadeEnergy() + heatUnit).length()) / 2])
															.replace("\0",
																	" ")
											+ cascadeInterval.getCascadeEnergy() + heatUnit
									,new String(new char[(initialTempNLoadPadding + maxTempNLoadCharLenth
											- (cascadeInterval.getTemp2() + tempUnit).length()) / 2]).replace("\0", " ")
											+ cascadeInterval.getTemp2() + tempUnit
											+ ((pinchTemps.get(0).contains(cascadeInterval.getTemp2())) ? " <<<---PINCH" : ""));
		}

		return cascade;
	}
	@Override
	public String toString() {
		return "\"problemTableEnergyCascade\":{\"" + printEnergyCascade() + "\"}";
	}
	
}
