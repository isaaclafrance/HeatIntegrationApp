package com.isaacapps.heatintegrationapp.internals.dao;

public class DataExtractionException extends Exception {
	public DataExtractionException(String extractionStage, String errorMsg){
		super("In Extraction Stage:{"+extractionStage+"}, the following error occured:{"+errorMsg+"}");
	}
}
