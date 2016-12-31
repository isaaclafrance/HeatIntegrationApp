package com.isaacapps.heatintegrationapp.internals.dao;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.isaacapps.heatintegrationapp.internals.DefinedPropertiesException;
import com.isaacapps.heatintegrationapp.internals.HXNetwork;
import com.isaacapps.heatintegrationapp.internals.IndustrialProcess;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.*;

public class IndustrialProcessDataExtractor {
	InputStream extractionInputStream;
	
	public IndustrialProcessDataExtractor(String fileName){
		
	}
	
	public IndustrialProcess readIndustrialProcess() throws DefinedPropertiesException{

		IndustrialProcess industrialProcess = new IndustrialProcess(readStreamsFromFile(), readColumnsFromFile(), readDeltaTMin(), readPreMERHotUtility(), readPreMERColdUtility());
		industrialProcess.setHXNetwork(new HXNetwork(industrialProcess.getProblemTable(), readHXExchangersFromFile()));		
		
		return industrialProcess;
	}
	
	//
	private List<Stream> readStreamsFromFile(){
		//TODO: extract stream info from file and convert into a list of streams
		
		return new ArrayList<>();
	}
	
	private List<Column> readColumnsFromFile(){
		//TODO: extract column info from file and convert into a list of column
		
		return new ArrayList<>();
	}
	
	private List<HeatExchanger> readHXExchangersFromFile(){
		//TODO: extract heat exchangers from a HXNetwork info from file
		
		return null;
	}
	
	private double readDeltaTMin(){
		//TODO: extract delta T Min info from file
		
		return 10.0f;
	}

	private double readPreMERHotUtility(){
		//TODO: extract pre MER QH info from file
		
		return 0.0f;
	}
	
	private double readPreMERColdUtility(){
		//TODO: extract pre MER QC info from file
		
		return 0.0f;
	}
	
}
