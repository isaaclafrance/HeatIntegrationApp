package com.isaacapps.heatintegrationapp.internals.energytransferelements;
import java.util.List;
import java.util.stream.Collectors;

import com.isaacapps.heatintegrationapp.internals.IndustrialProcess;


public class Stream extends EnergyTransferElement {
	//
	private IndustrialProcess industrialProcess;
	private Stream parentStream;
	public List<Stream> subStreams;
	
	//
	public Stream(String name, double sourceTemp, double targetTemp,  double cp, double enthalpyChange, double deltaTMin) throws DefinedPropertiesException{
		super(name, sourceTemp, targetTemp, cp, enthalpyChange, deltaTMin);
	}
	public Stream(String name, double sourceTemp, double targetTemp,  double cp, double enthalpyChange) throws DefinedPropertiesException{
		this(name, sourceTemp, targetTemp, cp, enthalpyChange, 0.0f);
	}
	public Stream(double sourceTemp, double targetTemp,  double cp, double enthalpyChange, double deltaTMin) throws DefinedPropertiesException{
		this("", sourceTemp, targetTemp, cp, enthalpyChange, deltaTMin);
		name = "Stream " + ID;
	}
	
	/**
	 * Create the specified substream for the current stream only if the energy constraints are not violated.
	 * @param cp
	 * @param heatLoad
	 * @return Boolean indicating whether or not the substream was created.
	 */
	public boolean createSubStream(double cp, double heatLoad){
		try{	
			Stream candidateSubStream = new Stream(getSourceTemp(), getTargetTemp(), cp, heatLoad, getDeltaTMin());
			if(Math.abs(heatLoad) > Math.abs(candidateSubStream.getHeatLoad())){
				candidateSubStream.setParentStream(this);
				subStreams.add(candidateSubStream);
				industrialProcess.addStream(candidateSubStream);
				return true;
			}
		}
		catch(DefinedPropertiesException e){
			return false;
		}
		return false;
	}
	
	public boolean removeSubstream(String name){
		java.util.stream.Stream<Stream> subStreamsToBeRemoved = subStreams.stream().filter(s -> s.name.equalsIgnoreCase(name));
		subStreamsToBeRemoved.forEach(s -> s.setParentStream(s));
		return subStreams.removeAll(subStreamsToBeRemoved.collect(Collectors.toList()));
	}
	
	//
	public Stream getParentStream(){
		return parentStream;
	}
	private void setParentStream(Stream parentStream){
		this.parentStream = parentStream;
	}

	public List<Stream> getSubStreams(){
		return subStreams;
	}	
	
	public void setIndustrialProcessRef(IndustrialProcess industrialProcess){
		this.industrialProcess = industrialProcess;
	}
	
	//
	@Override
	public String toString(){
		return String.format("\"stream\":%s", super.toString().substring(super.toString().indexOf(":")+1));
	}
}


