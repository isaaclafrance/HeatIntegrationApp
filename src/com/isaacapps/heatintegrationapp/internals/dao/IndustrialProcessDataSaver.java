package com.isaacapps.heatintegrationapp.internals.dao;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.isaacapps.heatintegrationapp.internals.IndustrialProcess;

public class IndustrialProcessDataSaver {
	private BufferedWriter bufferedSavedDataWriter;
	
	public IndustrialProcessDataSaver(String fileName, boolean append) throws IOException{
		bufferedSavedDataWriter = new BufferedWriter(new FileWriter(fileName, append));
	}
	
	public boolean saveIndustrialProcess(IndustrialProcess industrialProcess){
		//TODO: Save string representation of industrial process to file.
		try{
			bufferedSavedDataWriter.write(industrialProcess.toString());
			bufferedSavedDataWriter.flush();
			bufferedSavedDataWriter.close();
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
}
