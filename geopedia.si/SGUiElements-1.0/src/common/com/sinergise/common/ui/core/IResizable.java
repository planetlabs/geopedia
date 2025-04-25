package com.sinergise.common.ui.core;


public interface IResizable {

	 public boolean isResizable();
	 
	 public int getWidth();
	 
	 public int getHeight();
	 
	 public void setWidth(int w);
	 
	 public void setHeight(int h);
	 
	 public void setLeft(int l);
	 
	 public void setTop(int t);
	 
	 public int getLeft();
	 
	 public int getTop();
	 
	 public int getMinimalWidth();
	 
	 public int getMaximalWidth();
	 
	 public int getMinimalHeight();
	 
	 public int getMaximalHeight();

}
