package com.sinergise.gwt.ui.calendar;

import static com.sinergise.common.util.lang.TypeUtil.boxC;
import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

public class SGDateTimeBox extends SGTextBox {

	
	public static DateTimeFormat defaultFormat = DateTimeFormat.getFormat("dd/MM/yyyy");
	private DateTimeFormat dtFormat = null;
	private char[] formatParts;
	private int selectedPart = Integer.MIN_VALUE;
	
	private Date value = null;
	
	public SGDateTimeBox() {
		this(defaultFormat);
	}
	
	public SGDateTimeBox(DateTimeFormat dateFormat) {
		this.dtFormat = dateFormat;
		selectAllOnFocus = false;
		ArrayList<Character> parts = new ArrayList<Character>();
		char [] format = dtFormat.getPattern().toCharArray();
		Character part = null;
		for (int i=0;i<format.length;i++) {
			if (part==null) {
				if (!isDelimiter(format[i]))
					part=boxC(format[i]);
			} else if (isDelimiter(format[i]) || i==(format.length-1)) {
				parts.add(part);
				part=null;
			}
		}
		formatParts = new char[parts.size()];		
		for (int i=0;i<parts.size();i++) {
			formatParts[i]=parts.get(i).charValue();
		}
		
		int wgLength = dtFormat.getPattern().length()+1;
		setMaxLength(wgLength);
		setVisibleLength(wgLength);
		
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setDate(getText());
			}
		});
		
		addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int selPart = getSelectPart();
				if (selPart==Integer.MIN_VALUE) return;
				selectedPart=selPart;
				selectPart(selPart);
			}
		});
		

		addKeyDownHandler(new KeyDownHandler() {
			
			public void onKeyDown(KeyDownEvent event) {
				if (selectedPart == Integer.MIN_VALUE) {
					selectedPart = getSelectPart();
					if (selectedPart == Integer.MIN_VALUE) return;
				}
				if (event.isLeftArrow()) {
					selectedPart--;
					if (selectedPart<0) selectedPart=formatParts.length-1;
					selectPart(selectedPart);
				} else if(event.isRightArrow()) {
					selectedPart++;
					if (selectedPart>=formatParts.length) selectedPart=0;
					selectPart(selectedPart);
				} else if (event.isUpArrow()) {
					changePartValue(selectedPart, true);
				} else if (event.isDownArrow()) {
					changePartValue(selectedPart, false);
				}else {
					return;
				}
				event.preventDefault();
				event.stopPropagation();
			}
		});
	}
	
	private void updateUI() {
		if (value == null) {
			setText("");
		} else { 
			setText(dtFormat.format(value), true);
		}
	}
	
	public void setDate(Date date) {
		this.value = date;
		updateUI();
	}
	
	private void setDate(String date) {
		try {
			if (isNullOrEmpty(date)) {
				setDate((Date)null);
			} else {
												
				setDate(dtFormat.parse(preprocessDateString(date)));
			}
		} catch (IllegalArgumentException ingore) {
			updateUI();
		}
	}
	
	protected String preprocessDateString(String date){
		return date;
	}
	
	
	public Date getDate() {
		return value;
	}
	
	@SuppressWarnings("deprecation")
	private void changePartValue(int part, boolean increase) {
		Date dt = getDate();
		if (dt==null)
			return;
		int add=1;
		if (!increase)
			add=-1;
		switch (formatParts[part]) {
			case 'd':
			case 'E':
			case 'c':
				dt.setDate(dt.getDate()+add);
				break;
			case 'M':
			case 'L':
				dt.setMonth(dt.getMonth()+add);
				break;
			case 'y':
				dt.setYear(dt.getYear()+add);
				break;
			case 'h':
			case 'H':
			case 'k':
			case 'K':
				dt.setHours(dt.getHours()+add);
				break;
			case 'm':
				dt.setMinutes(dt.getMinutes()+add);
				break;
		}
		setText(dtFormat.format(dt),true);
		ValueChangeEvent.fire(this, getText());
	}
	private void selectPart(int part) {
		if (part < 0) {
			selectAll();
			return;
		}
		int start = 0;
		int length = 0;

		String text = getText();
		char[] textArry = text.toCharArray();
		int p = 0;
		boolean delimiter = false;
		for (int i = 0; i < textArry.length; i++) {
			if (isDelimiter(textArry[i])) {
				delimiter = true;
				if (p == part) {
					length = i - start;
					break;
				}
			} else {
				if (delimiter) {
					p++;
					if (p == part) start = i;
				}
				delimiter = false;
			}
		}
		if (length==0) length = textArry.length - start;
		setSelectionRange(start,length);
	}
	private int getSelectPart() {
		int part=0;
		int pos = getCursorPos();
		final String text = getText();
		if (text == null || text.length() == 0 
			|| pos < 0 || pos > (text.length() - 1) 
			|| getDate() == null 
			|| (pos == 0 && getSelectionLength() == text.length())) {
			return Integer.MIN_VALUE; 
		}
		boolean delimiter = false;
		while (pos >= 0) {
			if (isDelimiter(text.charAt(pos))) {
				if (!delimiter) part++;
				else delimiter = true;
			} else {
				delimiter = false;
			}
			pos--;
		}
		return part;
	}
	
	protected boolean isDelimiter(char ch) {
		if (ch=='.' || ch=='/' || ch==' '|| ch==':' || ch=='-')
			return true;
		return false;
	}
}
