package com.feng.hackathon.rest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class AppInitializer extends HttpServlet {

    private static final long serialVersionUID = 5446123039087841570L;

    public void init(ServletConfig config) throws ServletException {

    	/*
    	 * anything that need to be run at the startup
    	 */
    	//DBUtils.InitializeDatabase();
    	System.out.println("####### HACKATHON BACKEND STARTED!!!!!");

    }
}
