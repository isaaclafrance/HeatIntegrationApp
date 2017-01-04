package com.isaacapps.heatintegrationapp.internals.dao;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

import com.isaacapps.heatintegrationapp.internals.CostEvaluator;
import com.isaacapps.heatintegrationapp.internals.HXNetwork;
import com.isaacapps.heatintegrationapp.internals.IndustrialProcess;
import com.isaacapps.heatintegrationapp.internals.energytransferelements.*;

public class IndustrialProcessDataExtractor{
	BufferedReader bufferedExtractionDataReader;

	public IndustrialProcessDataExtractor(String fileName) throws FileNotFoundException{
		bufferedExtractionDataReader = new BufferedReader(new FileReader(fileName));
	}
	
	public List<IndustrialProcess> extractIndustrialProcessesFromFile() throws DefinedPropertiesException{
		//'#' is terminating character for each string instance of industrial process.
		Pattern industrialProcessPattern = Pattern.compile("{Industrial Process\": {[^#]+, [^#]+ \n, [^#]* \n, [^#]* \n, [^#]+ \n, [^#]+ \n, [^#]+}}");
		
		Matcher industrialProcessPatternMatcher = industrialProcessPattern.matcher(bufferedExtractionDataReader.lines()
																	 	          .reduce("", (prevLine, currLine)->prevLine+currLine));
		
		return IntStream.range(1, industrialProcessPatternMatcher.groupCount()+1).boxed()
				        .map(index -> industrialProcessPatternMatcher.group(index)).map(ipStr -> extractIndustrialProcess(ipStr))
				        .filter(ip -> ip != null).collect(toList());		                         
	}
	
	//
	private IndustrialProcess extractIndustrialProcess(String industrialProcessStr){
		//TODO: extract industrial info from string and convert into a list of streams
		IndustrialProcess industrialProcess;
		try {
			String preMerHotUtilityWithUnit = extractPreMERHotUtilityWithUnit(industrialProcessStr);
			String preMerColdUtilityWithUnit = extractPreMERColdUtilityWithUnit(industrialProcessStr);
			String preMerUtilityUnit = "";
			
			if(preMerHotUtilityWithUnit.split(" ")[1].equalsIgnoreCase(preMerHotUtilityWithUnit.split(" ")[1])){
				preMerUtilityUnit = preMerHotUtilityWithUnit.split(" ")[1];
			}
			else{
				throw new DataExtractionException("Extraction of IndustrialProcess", "Unit of preMerHotUtility and preMerColdUtility do not match.");
			}
			
			industrialProcess = new IndustrialProcess(Double.parseDouble(preMerHotUtilityWithUnit.split(" ")[0])
					                                  , Double.parseDouble(preMerColdUtilityWithUnit.split(" ")[0])
					                                  , preMerUtilityUnit);
			
			extractStreams(industrialProcessStr).stream().forEach(s->industrialProcess.addStream(s));
			extractColumns(industrialProcessStr).stream().forEach(c->industrialProcess.addColumn(c));
			
			industrialProcess.setHXNetwork(new HXNetwork(industrialProcess.getProblemTable(), extractHXExchangers(industrialProcessStr)));
			industrialProcess.setGlobalCostEvaluator(extractCostEvaluator(industrialProcessStr));
		} catch (DefinedPropertiesException e) {
			e.printStackTrace();
			return null;
		} catch (DataExtractionException e) {
			e.printStackTrace();
			return null;
		}	
		
		return industrialProcess;
	}
	private List<Stream> extractStreams(String industrialProcessStr)throws DefinedPropertiesException, DataExtractionException{
		//TODO: extract stream info from string and convert into a list of streams
		
		return new ArrayList<>();
	}
	
	private List<Column> extractColumns(String industrialProcessStr)throws DefinedPropertiesException, DataExtractionException{
		//TODO: extract column info from string and convert into a list of column
		
		return new ArrayList<>();
	}
	
	private List<HeatExchanger> extractHXExchangers(String industrialProcessStr)throws DefinedPropertiesException{
		//TODO: extract heat exchangers from a HXNetwork info from a string
		
		return null;
	}
	
	private CostEvaluator extractCostEvaluator(String industrialProcessStr)throws DefinedPropertiesException{
		//TODO: extract global cost info from a string
		
		return null;
	}
	
	
	private String extractPreMERHotUtilityWithUnit(String industrialProcessStr) throws DataExtractionException{
		//TODO: extract pre MER QH info from file
		try{
			return Pattern.compile("\"preMERHotUtility\": \"[\\d\\.]+ \\w+\"")
					      .matcher(industrialProcessStr).group()
					      .split(":")[1].trim();
		}catch(Exception e){
			throw new DataExtractionException("Extraction of PreMERHotUtility", e.getMessage());
		}
	}
	
	private String extractPreMERColdUtilityWithUnit(String industrialProcessStr) throws DataExtractionException{
		//TODO: extract pre MER QC info from file
		try{
			return Pattern.compile("\"preMERColdUtility\": \"[\\d\\.]+ \\w+\"")
					      		.matcher(industrialProcessStr).group()
					      		.split(":")[1].trim();
		}catch(Exception e){
			throw new DataExtractionException("Extraction of PreMERColdUtility", e.getMessage());
		}
	}
	
}
