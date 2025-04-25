package com.sinergise.gwt.ui.maingui.extwidgets;


import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.datepicker.client.CalendarModel;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.datepicker.client.DefaultCalendarView;
import com.google.gwt.user.datepicker.client.MonthSelector;
import com.sinergise.common.ui.i18n.Tooltips;
import com.sinergise.gwt.ui.editor.IntegerEditor;

public class DatePickerWithYearSelector extends DatePicker {
	@SuppressWarnings("deprecation")
	private static class MonthAndYearSelector extends MonthSelector {
		  private static final String BASE_NAME = "datePicker" ;
	
		  private PushButton backwards;
		  private PushButton forwards;
		  private PushButton backwardsYear;
		  private PushButton forwardsYear;
		  private Grid grid;
		  private int previousYearColumn = 0;
		  private int previousMonthColumn = 1 ;
		  private int monthColumn = 2 ;
		  private int nextMonthColumn = 3;
		  private int nextYearColumn = 4 ;
		  private IntegerEditor yearInput;
		  private boolean manualYearInput;
		  private InlineLabel dateText = new InlineLabel();
		  
		  private DatePickerWithYearSelector picker;
		  
		public MonthAndYearSelector(boolean manualYearInput) {
			this.manualYearInput = manualYearInput;  
		}

		public void setPicker(DatePickerWithYearSelector picker) {
		    this.picker = picker;
		    reloadYearInput();
		}
	
		@Override
		  protected void refresh() {
		    String formattedMonth = getModel().formatCurrentMonth();
		    if (manualYearInput)
		    	formattedMonth = formattedMonth.substring(0, 4);
		    
		    dateText.setText(formattedMonth);
		  }
	
		  @Override
		  protected void setup() {
		    // Set up backwards.
		    backwards = new PushButton();
		    backwards.getUpFace().setHTML("&lsaquo;");
		    backwards.setStyleName("control "+BASE_NAME + "PreviousMonthButton");
		    backwards.setTitle(Tooltips.INSTANCE.prevMonth());
		    backwards.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		    	 setMonth(-1);
		      }
		    });
		    
		    yearInput = new IntegerEditor("####", false);
		    yearInput.setStyleName("yearInput");
		    yearInput.setVisibleLength(4);
		    yearInput.setMaxLength(4);
		    yearInput.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (yearInput == null || yearInput.getText().length() <= 3) {
						reloadYearInput();
					} else {
						applyYearFromInput();
					}
					
				}
			});
		    
		    yearInput.addKeyDownHandler(new KeyDownHandler() {
				
				@Override
				public void onKeyDown(KeyDownEvent event) {
					int keyPress = event.getNativeKeyCode();
					if (keyPress == KeyCodes.KEY_UP) {
						setYear(getCurrentYear() + 1);
					}
					if (keyPress == KeyCodes.KEY_DOWN) {
						setYear(getCurrentYear() - 1);
					}
				}
			});
		    
	
		    forwards = new PushButton();
		    forwards.getUpFace().setHTML("&rsaquo;");
		    forwards.setTitle(Tooltips.INSTANCE.nextMonth());
		    forwards.setStyleName("control "+BASE_NAME + "NextMonthButton");
		    forwards.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		    	  setMonth(+1);
		      }
		    });
	
		    backwardsYear = new PushButton();
		    backwardsYear.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		    	  setMonth(-12);
		      }
		    });
	
		    backwardsYear.getUpFace().setHTML("&laquo;");
		    backwardsYear.setTitle(Tooltips.INSTANCE.prevYear());
		    backwardsYear.setStyleName("control "+BASE_NAME + "PreviousYearButton");
	
		    forwardsYear = new PushButton();
		    forwardsYear.getUpFace().setHTML("&raquo;");
		    forwardsYear.setTitle(Tooltips.INSTANCE.nextYear());
		    forwardsYear.setStyleName("control "+BASE_NAME + "NextYearButton");
		    forwardsYear.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		    	  setMonth(+12);
		      }
		    });
		    
		    FlowPanel dateTextHolder = new FlowPanel();
		    dateTextHolder.add(dateText);
		    if (manualYearInput) {
		    	dateTextHolder.add(yearInput);
		    }
		    
		    // Set up grid.
		    grid = new Grid(1, 5);
		    grid.setWidget(0, previousYearColumn, backwardsYear);
		    grid.setWidget(0, previousMonthColumn, backwards);
	    	grid.setWidget(0, monthColumn, dateTextHolder);
		    
		    grid.setWidget(0, nextMonthColumn, forwards);
		    grid.setWidget(0, nextYearColumn, forwardsYear);
	
		    CellFormatter formatter = grid.getCellFormatter();
		    formatter.setStyleName(0, monthColumn, BASE_NAME + "Month");
		    formatter.setWidth(0, previousYearColumn, "1");
		    formatter.setWidth(0, previousMonthColumn, "1");
		    formatter.setWidth(0, monthColumn, "100%");
		    formatter.setWidth(0, nextMonthColumn, "1");
		    formatter.setWidth(0, nextYearColumn, "1");
		    grid.setStyleName(BASE_NAME + "MonthSelector");
		    
		    initWidget(grid);
		  }
		  
		  public void setMonth(int numMonths) {
			  
			  Date dt = new Date(picker.getModel().getCurrentMonth().getTime());
			  dt.setMonth(dt.getMonth() + numMonths);
			  picker.getModel().setCurrentMonth(dt);    		 
			  reloadYearInput();
			  picker.refreshComponents();
	        }
		  
		  protected void reloadYearInput() {
			  yearInput.setText(String.valueOf(getCurrentYear()));
		  }
		  
		  protected void applyYearFromInput() {
			  setYear(Integer.parseInt(yearInput.getText()));
		  }
		  

		  private void setYear(int year) {
			  Date dt = new Date(picker.getModel().getCurrentMonth().getTime());
			  dt.setYear(year - 1900);
			  picker.getModel().setCurrentMonth(dt);    		 
			  reloadYearInput();
			  picker.refreshComponents();
		  }
		  
		  protected int getCurrentYear() {
			  return picker.getModel().getCurrentMonth().getYear() + 1900;
		  }
	}
	MonthAndYearSelector monthSelector;
	boolean manualYearInput;
	public static boolean myi;
	
    public DatePickerWithYearSelector() {
		super(new MonthAndYearSelector(false), new DefaultCalendarView(), new CalendarModel());
		monthSelector = (MonthAndYearSelector)this.getMonthSelector();
		monthSelector.setPicker(this);
    }
    
    public DatePickerWithYearSelector(boolean manualYearInput) {
    	super(new MonthAndYearSelector(manualYearInput), new DefaultCalendarView(), new CalendarModel());
		monthSelector = (MonthAndYearSelector)this.getMonthSelector();
		monthSelector.setPicker(this);
    }

    public void refreshComponents() {
            super.refreshAll() ;
    }
    
	public final void setValueForYearSelector(Date date) {
		super.setValue(date, false);
		this.getModel().setCurrentMonth(date);
		monthSelector.refresh();
		monthSelector.setYear(monthSelector.getCurrentYear());
	}
} 