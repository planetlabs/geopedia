package com.sinergise.common.gis.map.render;

public class RepaintListenerAdapter implements RepaintListener {
	@Override
	public boolean beforeRepaint(boolean hard) {
		return true;
	}
	@Override
	public boolean beforeRepaintScheduled(int millis, boolean continuous, boolean hard) {
		return true;
	}
	@Override
	public void onRepaint(boolean hard) {
	}
	@Override
	public void onRepaintScheduled(int millis, boolean continuous, boolean hard) {
	}
}
