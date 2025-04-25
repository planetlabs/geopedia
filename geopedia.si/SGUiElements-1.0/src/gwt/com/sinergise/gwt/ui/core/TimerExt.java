/*
 *
 */
package com.sinergise.gwt.ui.core;

import com.google.gwt.user.client.Timer;

public abstract class TimerExt extends Timer {
    private static final boolean DEBUG=false;
    private long lastExec=0;
    private boolean running=false;
    private int period=1;
    private boolean cancelInt=false;
    
    private boolean hasExtra=false;
    
    private boolean waitForEnd=false;
    public TimerExt() {
        this(false);
    }
    public TimerExt(boolean waitForEnd) {
        this(1, waitForEnd);
    }
    public TimerExt(int delayMillis) {
        this(delayMillis, false);
    }
    public TimerExt(int delayMillis, boolean waitForEnd) {
        this.period=delayMillis;
        this.waitForEnd=waitForEnd;
    }
    
    public void setDelay(int delayMillis) {
        this.period=delayMillis;
    }
    
    @Override
	public final void run() {
        if (!running) return;
        
        if (waitForEnd) {
            if (hasExtra) {
                hasExtra=false;
            } else {
                internalExec();
                cancel();
            }
        } else if (!hasExtra || internalExec()>lastExec) {
            cancel();
        }
    }
    
    abstract public void execute();
    
    protected long internalExec() {
        hasExtra=false;
        execute();
        long t=System.currentTimeMillis();
        if (DEBUG) System.out.println("EXEC "+t);
        return t;
    }
    
    @Override
	public void cancel() {
        if (!cancelInt) {
            running=false;
            if (DEBUG) System.out.println("STOPPED "+period);
        }
        super.cancel();
    }
    
    @Override
	public void schedule(int delayMillis) {
        throw new UnsupportedOperationException("Can't schedule directly on TimerExt");
    }
    @Override
	public void scheduleRepeating(int periodMillis) {
        throw new UnsupportedOperationException("Can't schedule repeating on TimerExt");
    }

    public void schedule() {
        if (period==0) {
            cancel();
            execute();
            return;
        }
        lastExec=System.currentTimeMillis()+period;
        if (!running) {
            if (DEBUG) System.out.println("STARTING "+period);
            running=true;
            if (!waitForEnd) {
                internalExec();
            }
            cancelInt=true;
            super.scheduleRepeating(period);
            cancelInt=false;
        } else {
            hasExtra=true;
        }
        if (DEBUG) System.out.print(".");
    }
}
