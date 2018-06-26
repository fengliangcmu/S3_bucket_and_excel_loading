package com.feng.hackathon.dao;

import com.feng.hackathon.utils.Log;

public class DAOFactory {

    private static DAOFactory instance;

    /**
     * Singleton instance access method to get the instance of the class to
     * request a specific DAO implementation.
     *
     * @return - DAOFactory instance
     */
    public static final DAOFactory getInstance() {
        if (instance == null) {
            Log.info("Creating a new DAOFactory singleton instance.");
            instance = new DAOFactory();
        }

        return instance;
    }

//    /**
//     * Method to get a new object implementing MongoTestDAO
//     *
//     * @return - Object implementing MongoTestDAO
//     */
//    public MongoTestDAO getMongoTestDAO() {
//        return new MongoTestDAOImpl();
//    }

}
