package com.isaacapps.heatintegrationapp.internals;

public class DefinedPropertiesException extends Exception {
	public DefinedPropertiesException(String errorMsg, String entity){
		super("::ERROR:: "+errorMsg+" ::FOR:: "+entity);
	}
}
