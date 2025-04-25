package com.sinergise.common.util;

import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;

public class Timing {
	public interface Sink {
		public static final int TYPE_START    = 0;
		public static final int TYPE_PROGRESS = 1;
		public static final int TYPE_END      = 2;
		
		public void processTimingEvent(String desc, long sincestart, long sincelast, int done, int total, int type);
	}
	
	static Sink defaultSink;
	
	public static void setDefaultSink(final Sink sink) {
		defaultSink = sink;
	}
	
	private static class DefaultSink implements Sink {
		@Override
		public void processTimingEvent(final String desc, final long sincestart, final long sincelast, final int done, final int total, final int type) {
			if (type != TYPE_START) {
				System.out.println(desc + ": " + (sincelast) + "ms (total " + sincestart + "ms)");
			}
		}
		
		static Sink instance = null;
		
		public static Sink getInstance() {
			if (instance == null) {
				if (defaultSink != null) {
					instance = defaultSink;
				} else {
					instance = new DefaultSink();
				}
			}
			return instance;
		}
	}
	
	private final Sink sink;
	
	public Timing() {
		sink = DefaultSink.getInstance();
	}
	
	public Timing(final Sink sink) {
		this.sink = sink;
	}
	
	long start = -1, last = -1;
	
	public void start(final String desc) {
		start = System.currentTimeMillis();
		last = start;
		if (sink != null) {
			sink.processTimingEvent(desc, 0, 0, -1, -1, Sink.TYPE_START);
		}
	}
	
	public void step(final String desc) {
		final long curr = System.currentTimeMillis();
		if (sink != null) {
			sink.processTimingEvent(desc, curr - start, curr - last, -1, -1, Sink.TYPE_PROGRESS);
		}
		last = curr;
	}
	
	public void end(final String desc) {
		final long curr = System.currentTimeMillis();
		if (sink != null) {
			sink.processTimingEvent(desc, curr - start, curr - last, -1, -1, Sink.TYPE_END);
		}
		last = curr;
	}
	
	public void progress() {
		progress("Tick");
	}
	
	public void progress(final String msg) {
		progress(msg, 0);
	}
	
	public void progress(final int done) {
		progress("Progress", done);
	}
	
	public void progress(final String msg, final int done) {
		progress(msg, done, -1);
	}
	
	public void progress(final int done, final int total) {
		progress("Progress", done, total);
	}
	
	NumberFormatter df = NumberFormatUtil.create("####.##");
	
	public void progress(final String msg, final int done, final int total) {
		if (sink == null) {
			return;
		}
		
		final long curr = System.currentTimeMillis();
		
		if (total > 0) {
			final double perSec = (double)done / ((double)(curr - last)) * 1000.0;
			final double msecPer = 1000.0 / perSec;
			final double remTime = (total - done) / perSec;
			
			final StringBuffer sb = new StringBuffer();
			sb.append(msg);
			sb.append(" (");
			sb.append(done);
			sb.append("/");
			sb.append(total);
			sb.append(" done; ");
			sb.append(df.format(perSec));
			sb.append(" items/sec; ");
			sb.append(df.format(msecPer));
			sb.append(" ms/item; ");
			sb.append(df.format(remTime));
			sb.append(" secs remain)");
			
			sink.processTimingEvent(sb.toString(), curr - start, curr - last, done, total, Sink.TYPE_PROGRESS);
		} else if (done > 0) {
			final double perSec = (double)done / ((double)(curr - last)) * 1000.0;
			
			final StringBuffer sb = new StringBuffer();
			sb.append(msg);
			sb.append(" (");
			sb.append(done);
			sb.append(" done; ");
			sb.append(perSec);
			sb.append(" items/sec)");
			
			sink.processTimingEvent(sb.toString(), curr - start, curr - last, done, total, Sink.TYPE_PROGRESS);
		} else {
			sink.processTimingEvent(msg, curr - start, curr - last, done, total, Sink.TYPE_PROGRESS);
		}
	}
	
	public void startIfNotStarted() {
		if (start < 0) {
			start();
		}
	}
	
	public void start() {
		start("Started");
	}
	
	public void step() {
		step("Progress");
	}
	
	public void end() {
		end("Done");
	}
	
	public long getTotalTime() {
		return last - start;
	}
	
	static Timing defInst = null;
	
	public static Timing defInst() {
		if (defInst == null) {
			defInst = new Timing();
		}
		
		return defInst;
	}
	
	public static void dend() {
		defInst().end();
	}
	
	public static void dend(final String s) {
		defInst().end(s);
	}
	
	public static long dgetTotalTime() {
		return defInst().getTotalTime();
	}
	
	public static void dprogress() {
		defInst().progress();
	}
	
	public static void dprogress(final int a) {
		defInst().progress(a);
	}
	
	public static void dprogress(final String s) {
		defInst().progress(s);
	}
	
	public static void dprogress(final String s, final int a) {
		defInst().progress(s, a);
	}
	
	public static void dprogress(final String s, final int a, final int b) {
		defInst().progress(s, a, b);
	}
	
	public static void dstart() {
		defInst().start();
	}
	
	public static void dstart(final String s) {
		defInst().start(s);
	}
	
	public static void dstartIfNotStarted() {
		defInst().startIfNotStarted();
	}
	
	public static void dstep() {
		defInst().step();
	}
	
	public static void dstep(final String s) {
		defInst().step(s);
	}
}
