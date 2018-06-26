package com.feng.hackathon.utils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

public class SecurityUtils {
	
	public static String getSalt()
	{
		String tmp = null;
		try{
			tmp = KeyGenerator.getInstance("AES").generateKey().toString();
		}
		catch(NoSuchAlgorithmException e){
			Log.error("### Failed to generate a salt due to NoSuchAlgorithmException ###");
			e.printStackTrace();
		}
		int i = tmp.indexOf("@");
	    return tmp.substring(i+1);
	}
	
	public static String encode(String input, String salt){
		
		String sha256hex = org.apache.commons.codec.digest.DigestUtils.sha256Hex(input + "," + salt); 
		return sha256hex;
	}
	
}
