package com.sinergise.geopedia.style.symbology.rhino;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.Symbolizer;

public class PaintingPassImpl extends ScriptableObject implements PaintingPass {

	private static final long serialVersionUID = 2199092177562802756L;
	private Symbolizer[] symbolizers;
	
	public PaintingPassImpl() {
		symbolizers = null;
	}
	
	public PaintingPassImpl(NativeArray nativeArray) {
		symbolizers = new Symbolizer[(int) nativeArray.getLength()];
		for (int i=0;i<symbolizers.length;i++) {
			symbolizers[i] = (Symbolizer)nativeArray.get(i);
		}
	}
	
	@JSSetter
	public void setSymbolizers(Symbolizer[] symbolizers) {
		this.symbolizers = symbolizers;
	}
	
	@Override
	@JSGetter
	public Symbolizer[] getSymbolizers() {
		return symbolizers;
	}

	@Override
	public String getClassName() {
		return "PaintingPass";
	}

}
