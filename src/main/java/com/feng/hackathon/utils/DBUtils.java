package com.feng.hackathon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class DBUtils {

    private static MongoClient mongoClient;
    
    public static void InitializeDatabase(){
        // Manage the mongo db connection...
    	//http://www.programcreek.com/java-api-examples/index.php?class=com.mongodb.MongoClient&method=setWriteConcern
    	Log.enter("### trying to connect mongo db ###");
    	
    	List<ServerAddress> seeds = new ArrayList<ServerAddress>();
  		
    	Properties pro = ProjectUtils.getPropertyObj();
    	
    	String db_uri = pro.getProperty("db_uri");
    	StringTokenizer st = new StringTokenizer(db_uri, ",");
		while (st.hasMoreElements()) {
			StringTokenizer tmp = new StringTokenizer(st.nextToken(),":");
			String hostUri = "";
			String hostPort = "";
			int hostport = 0;
			if(tmp.hasMoreTokens()) hostUri = tmp.nextToken();
			if(tmp.hasMoreTokens()) hostPort = tmp.nextToken();
			try{
				hostport = Integer.parseInt(hostPort);
			}
			catch(Exception e){
				Log.info("failed to part port str to int~");
			}
			seeds.add(new ServerAddress(hostUri, hostport));
		}
    
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
			credentials.add(
					MongoCredential.createScramSha1Credential(
							pro.getProperty("db_user"),
							pro.getProperty("db_name"),
							pro.getProperty("db_pwd").toCharArray()
							));
		mongoClient = new MongoClient( seeds, credentials );
		Log.info("### mongo client instance is created! ###");
		
		Log.info("### testing database connection ###");
		if(!isDatabaseConnected(mongoClient)){
			Log.exit("### failed to get hackathon database!!! ###");
		}
		else{
			Log.exit("### already got the hackathon database!!! ###");
			System.out.println("### Hey, hackathon restful service is up and running!!! ###");
		}
		
    }
    
    public static MongoDatabase getDatabase(){
    	if(mongoClient == null){
    		InitializeDatabase();
    	}
    	Properties pro = ProjectUtils.getPropertyObj();
    	MongoDatabase db = mongoClient.getDatabase(pro.getProperty("db_name"));
    	return db;
    }
    
    public static boolean isDatabaseConnected(MongoClient mongoClient){
    	try{
        	Properties pro = ProjectUtils.getPropertyObj();
        	MongoDatabase database = mongoClient.getDatabase(pro.getProperty("db_name"));
        	Document serverStatus = database.runCommand(new Document("serverStatus", 1));
        	Map connections = (Map) serverStatus.get("connections");
        	Integer current = (Integer) connections.get("current");
        	if(current > 0) return true;
        	else return false;		
    	}
    	catch(Exception e){
    		Log.error("### exception when trying to connect hackathon database ###");
    		return false;
    	}

    }
    
}
