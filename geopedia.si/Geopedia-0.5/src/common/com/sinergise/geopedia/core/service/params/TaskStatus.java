package com.sinergise.geopedia.core.service.params;

import java.io.Serializable;

public class TaskStatus implements Serializable{
	private static final long serialVersionUID = 6482921454821670679L;
	public static final int GENERAL_ERROR = 1;
	public static enum Status {
		NOP, WORKING, DONE, FINAL, ERROR;

		public boolean isBefore(Status other) {
			return ordinal() < other.ordinal();
		}
		
		public boolean isAfter(Status other) {
			return ordinal() > other.ordinal();
		}
	}
	
	
	private Status status;
	private int error;
	private double progress;
	
	public TaskStatus () {
		status = Status.NOP;
		error=Integer.MIN_VALUE;
		progress = 0;
	}
	
	
	public void setProgress(double progress) {
		this.progress=progress;
	}
	public double getProgress() {
		return progress;
	}
	
	public void setStatus (Status s) {
		status = s;
	}

	public Status getStatus() {
		return status;
	}

	public void setError(int error) {
		this.error = error;
	}
	public int getError() {
		return error;
	}
}
