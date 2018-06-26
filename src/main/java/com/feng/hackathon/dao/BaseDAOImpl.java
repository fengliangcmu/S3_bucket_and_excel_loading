package com.feng.hackathon.dao;

import java.sql.SQLException;

import com.feng.hackathon.exceptions.DBException;
import com.feng.hackathon.utils.DBUtils;
import com.feng.hackathon.utils.Log;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * This is the base class for all DAO implementation classes. Common methods
 * like getting a database connection, and closing ResultSets are available in
 * this class.
 *
 */
public class BaseDAOImpl {

//    /**
//     * Utility method to close a ResultSet.
//     *
//     * @param rs - ResultSet to be closed
//     */
//    protected void closeResultSet(ResultSet rs) {
//        if (rs != null) {
//            try {
//                rs.close();
//            } catch (SQLException e) {
//                Log.error("Error when closing ResultSet", e);
//                throw new DBException("Error when closing ResultSet", e);
//            }
//        }
//    }

    /**
     * Utility method to close a databse connection.
     *
     * @param conn - Connection to be closed
     */
    protected void closeConnection(MongoClient mongoClient) {
        if (mongoClient != null) {
        	Log.enter("trying to close mongo client");
        	mongoClient.close();
        	Log.exit("mongo client successfully closed");
        
        }
    }
    
    /**
     * Utility method to get a connected Mongo database instance.
     *
     * @return - Mongo database instance.
     *
     * @throws SQLException
     */
    protected MongoDatabase getConnection() {
        return DBUtils.getDatabase();
    }

    protected void handleException(Exception e) {
    	Log.error("=== DB OPERATION ERROR ===  CHECK FOLLOWING TRACE: ");
        Log.error(e);
        throw new DBException(e);
    }

}
