package com.sinergise.geopedia.style.symbology.rhino;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.Symbology;

public class SymbologyImpl extends ScriptableObject implements Symbology {

	private static final long serialVersionUID = -1324328174861936520L;
	private PaintingPass[] paintingPasses;
	
	public SymbologyImpl() {
		paintingPasses=null;
	}
	
	
	public SymbologyImpl(NativeArray nativeArray) {
		paintingPasses = new PaintingPass[(int) nativeArray.getLength()];
		for (int i=0;i<paintingPasses.length;i++) {
			paintingPasses[i] = (PaintingPass)nativeArray.get(i);
		}
	}
	
	
	@JSSetter
	public void setPaintingPasses(PaintingPass[] paintingPasses) {
		this.paintingPasses = paintingPasses;
	}
	
	@Override
	@JSGetter
	public PaintingPass[] getPaintingPasses() {
		return paintingPasses;
	}

	@Override
	public String getClassName() {
		return "Symbology";
	}

}
