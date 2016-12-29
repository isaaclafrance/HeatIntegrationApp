package com.isaacapps.heatintegrationapp.internals.dao;

import java.io.OutputStream;

import com.isaacapps.heatintegrationapp.internals.IndustrialProcess;

public class IndustrialProcessDataSaver {
	private OutputStream saverOutputStream;
	
	public IndustrialProcessDataSaver(String fileName){
		
	}
	
	public boolean saveIndustrialProcess(IndustrialProcess industrialProcess){
		//TODO: save string representation of industrial process to file.
		
		industrialProcess.toString();
		
		return false;
	}
}
