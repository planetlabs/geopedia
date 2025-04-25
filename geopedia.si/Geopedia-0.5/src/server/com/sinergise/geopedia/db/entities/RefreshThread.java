package com.sinergise.geopedia.db.entities;

import java.sql.SQLException;

import com.sinergise.geopedia.app.Main;


public class RefreshThread extends Thread {
	private MetaData	metaData;
	private int refreshInterval = 1000; 
	public RefreshThread(MetaData metaData) {
		super();
		setPriority(Thread.NORM_PRIORITY-1);
		setName("GPD: RefreshThread");
		setDaemon(true);
		this.metaData = metaData;		
		System.out.println("Metadata refresh interval "+refreshInterval+"ms");
	}
	
	public void run() {
		while (Main.isRunning())
		{
			try {
				sleep(refreshInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				metaData.refresh();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
