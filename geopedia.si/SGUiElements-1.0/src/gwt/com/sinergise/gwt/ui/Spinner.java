package com.sinergise.gwt.ui;


import static com.sinergise.common.util.string.StringUtil.times;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sinergise.common.ui.action.ActionListener;
import com.sinergise.common.ui.action.SourcesActionEvents;
import com.sinergise.common.ui.core.KeyCodes;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;
import com.sinergise.gwt.util.html.CSS;


public class Spinner extends CompositeExt implements HasValueChangeHandlers<Double> {
	private static final boolean	DEBUG	= false;

	public static final Spinner getIntegerSpinner() {
		Spinner sp = new Spinner();
		sp.setNumDecimals(0);
		return sp;
	}

	private double			inc				= 1;
	private double			incFact			= 10;
	private double			min				= 0;
	private double			max				= 100;
	private NumberFormat 	nf				= NumberFormat.getFormat("0.#");
	private String			suffix			= null;

	private UpDownButton	but				= new UpDownButton();
	{
		but.addActionListener(new ActionListener() {
			public void actionPerformed(SourcesActionEvents sender, Object eventType) {
				if (Boolean.FALSE.equals(eventType)) {
					addToVal(-inc);
				} else {
					addToVal(inc);
				}
			}
		});
	}

	protected SGTextBox		text			= new SGTextBox();
	{
		text.setMaxLength(5);
		text.setVisibleLength(5);
		CSS.textAlign(text.getElement(), CSS.RIGHT);
		text.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (DEBUG) System.out.println("DOWN " + text.getText() + " EMPTY: " + text.isEmpty());

				switch (event.getNativeKeyCode()) {
					case KeyCodes.KEY_ENTER:
						updateValue();
						break;
					case KeyCodes.KEY_PGUP:
						addToVal(incFact * inc);
						break;
					case KeyCodes.KEY_PGDOWN:
						addToVal(-incFact * inc);
						break;
					case KeyCodes.KEY_UP:
						if (event.isControlKeyDown()) {
							addToVal(incFact * inc);
						} else {
							addToVal(inc);
						}
						break;
					case KeyCodes.KEY_DOWN:
						if (event.isControlKeyDown()) {
							addToVal(-incFact * inc);
						} else {
							addToVal(-inc);
						}
						break;
					default:
						return;
				}
				event.preventDefault();
				event.stopPropagation();
			}

		});
		text.addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				if (DEBUG) System.out.println("BLUR " + text.getText() + " EMPTY: " + text.isEmpty());
				updateValue();
			}
		});
		text.addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				if (DEBUG) System.out.println("FOCUS " + text.getText() + " EMPTY: " + text.isEmpty());
				int sufLen = 0;
				if (suffix != null) sufLen = suffix.length();
				text.setSelectionRange(0, Math.max(0, text.getText().length() - sufLen));
				event.preventDefault();
			}
		});
	}
	private double			curVal			= Double.NaN;

	/**
	 * Constructor
	 */
	public Spinner() {
		HorizontalPanel cont = new HorizontalPanel();
		cont.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		cont.add(text);
		cont.add(but);
		initWidget(cont);
		setValue(curVal, false);
	}
	
	public Spinner (double min, double max, int numDecimals) {
		this();
		setMin(min);
		setMax(max);
		setNumDecimals(numDecimals);
	}

	public void setValue(int val) {
		setValue(val, true);
	}

	public void setValue(double val) {
		setValue(val, true);
	}
	
	public void setEnabled(boolean enabled) {
		but.setEnabled(enabled);
		text.setEnabled(enabled);
	}
	
	public boolean isEnabled() {
		return but.isEnabled() && text.isEnabled();
	}

	public boolean setValue(double val, boolean fireEvents) {
		val = val < min ? min : val > max ? max : val;
		if (val == curVal) {
			return false;
		}
		curVal = val;
		updateText();
		if (fireEvents) {
			fireChange();
		}
		return true;
	}

	protected void addToVal(double what) {
		updateValue();
		final double toSet = Double.isNaN(curVal) ? min : curVal + what;
		setValue(toSet);
	}

	protected void updateValue() {
		if (DEBUG) System.out.println("UPD VAL: " + text.getText() + " (oldVal = " + curVal + ")");

		String txt = text.getText();
		if (suffix != null && suffix.length() > 0 && txt.endsWith(suffix)) {
			txt = txt.substring(0, txt.length() - suffix.length()).trim();
		}
		txt = txt.trim();
		if (txt.length() < 1) setValue(Double.NaN);
		else {
			try {
				if (setValue(NumberFormat.getDecimalFormat().parse(txt), true)) {
					return;
				}
			} catch(NumberFormatException ignore) { }
			updateText();
		}
	}

	protected void updateText() {
		if (DEBUG) System.out.println("UPD TXT: " + curVal + " (oldTxt = " + text.getText() + ")");
		if (Double.isNaN(curVal)) {
			if (!text.isEmpty()) {
				text.clear();
			}
			return;
		}
		text.setText(nf.format(curVal) + (suffix == null ? "" : suffix), true);
	}

	public double getValue() {
		updateValue();
		return curVal;
	}

	public int getIntValue() {
		return (int)getValue();
	}

	private void updateUI() {
		//TODO: Support for double
		int maxLen = String.valueOf((int)max).length();
		int minLen = String.valueOf((int)min).length();
		int len = Math.max(minLen, maxLen);
		len += nf.getPattern().length()-1;
		if (suffix != null) len += suffix.length();
		text.setMaxLength(len);
		text.setVisibleLength(len);
	}

	public void setIncrement(double inc) {
		this.inc = inc;
		updateUI();
	}

	public void setLargeIncrementFactor(double incFact) {
		this.incFact = incFact;
		updateUI();
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
		updateUI();
		updateText();
	}

	public void setNumDecimals(int numDec) {
		nf = NumberFormat.getFormat("0."+times("#", numDec));
		updateUI();
	}

	public void setMin(double min) {
		this.min = min;
		updateUI();
	}

	public void setMax(double max) {
		this.max = max;
		updateUI();
	}

	private void fireChange() {
		ValueChangeEvent.fire(this, Double.valueOf(curVal));
	}
	
	public void setFocus() {
		text.setFocus(true);
		text.setSelectionRange(0, text.getValue().length());
	}

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Double> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
	
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return text.addKeyDownHandler(handler);
	}
}
