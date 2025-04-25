/**
 * 
 */
package com.sinergise.java.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A StreamGobbler is an InputStream that uses an internal worker thread to constantly consume input from another InputStream.
 * 
 * @author tcerovski
 */
public class StreamGobbler extends Thread {
	
	InputStream  is;
	String       type;
	OutputStream os;
	
	public StreamGobbler(final InputStream is, final String type) {
		this(is, type, null);
	}
	
	public StreamGobbler(final InputStream is, final String type, final OutputStream redirect) {
		this.is = is;
		this.type = type;
		this.os = redirect;
	}
	
	public synchronized void startDaemon() {
		setDaemon(true);
		super.start();
	}
	
	@Override
	public void run() {
		try {
			PrintWriter pw = null;
			if (os != null) {
				pw = new PrintWriter(os);
			}
			
			final InputStreamReader isr = new InputStreamReader(is);
			final BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (pw != null) {
					pw.println(line);
				}
				System.out.println(type + ">" + line);
			}
			if (pw != null) {
				pw.flush();
			}
		} catch(final IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
}
