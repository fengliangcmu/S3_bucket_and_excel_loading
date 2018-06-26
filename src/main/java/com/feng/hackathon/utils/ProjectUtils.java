package com.feng.hackathon.utils;

import java.util.Properties;

public class ProjectUtils {
	
	private static Properties props = null;

	public static Properties getPropertyObj(){
		if(props != null) return props;
		try{
			props = new Properties();
			props.load(Thread.currentThread().getContextClassLoader().getResource("uwillneverknow.properties").openStream());
		}
		catch(Exception e){
			Log.error("Failed to get properties file!!!");
			e.printStackTrace();
		}
		return props;
	}
	
	public static String getFilePathFromResource(String fileName){
		String filePath = null;
		filePath = Thread.currentThread().getContextClassLoader().getResource(fileName).getFile();	
		return filePath;
	}
}
