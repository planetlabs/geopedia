package com.sinergise.java.web;

import javax.servlet.http.HttpServlet;

import com.sinergise.java.util.UtilJava;

public class ServerSideInitializer extends HttpServlet {
	private static final long serialVersionUID = 4260455990346974905L;
	
	static {
		UtilJava.initStaticUtils();
	}
	
	public static void initialize() {
	}
}
