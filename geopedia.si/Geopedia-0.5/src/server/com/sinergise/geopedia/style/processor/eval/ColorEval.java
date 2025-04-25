package com.sinergise.geopedia.style.processor.eval;

import com.sinergise.geopedia.core.style.colors.ARGB;
import com.sinergise.geopedia.core.style.colors.ColorBlend;
import com.sinergise.geopedia.core.style.colors.ColorMap;
import com.sinergise.geopedia.core.style.colors.ColorProcess;
import com.sinergise.geopedia.core.style.consts.ConstColor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.proxys.BgColorOf;
import com.sinergise.geopedia.core.style.proxys.FgColorOf;
import com.sinergise.geopedia.core.style.proxys.FontColorOf;
import com.sinergise.geopedia.core.style.proxys.LineColorOf;
import com.sinergise.geopedia.core.style.proxys.SymbolColorOf;
import com.sinergise.geopedia.core.style.ternaries.ColorTernary;
import com.sinergise.geopedia.style.processor.Evaluator;
import com.sinergise.geopedia.style.processor.extra.FillBgColorFromField;
import com.sinergise.geopedia.style.processor.extra.FillFgColorFromField;
import com.sinergise.geopedia.style.processor.extra.FontColorFromField;
import com.sinergise.geopedia.style.processor.extra.LineColorFromField;
import com.sinergise.geopedia.style.processor.extra.SymbolColorFromField;

public interface ColorEval
{
	public int eval();
	public boolean isConst();
	
	class Factory {
		public static ColorEval create(ColorSpec spec, final Evaluator evaluator)
        {
			if (spec instanceof ARGB) {
				ARGB argb = (ARGB) spec;
				
				final LongEval alpha = LongEval.Factory.create(argb.alpha, evaluator);
				final LongEval red = LongEval.Factory.create(argb.red, evaluator);
				final LongEval green = LongEval.Factory.create(argb.green, evaluator);
				final LongEval blue = LongEval.Factory.create(argb.blue, evaluator);
				
				return new ColorEval() {
					boolean saidConst = false;
					
					public int eval()
					{
						long la = alpha.eval(); if (la < 0) la = 0; else if (la > 255) la = 255;
						int result = ((int)la) << 24;
						if (saidConst) {
							if (la == 0)
								return 0;
						}
						long lr = red.eval(); if (lr < 0) lr = 0; else if (lr > 255) lr = 255;
						result |= ((int)lr) << 16;
						long lg = green.eval(); if (lg < 0) lg = 0; else if (lg > 255) lg = 255;
						result |= ((int)lg) << 8;
						long lb = blue.eval(); if (lb < 0) lb = 0; else if (lb > 255) lb = 255;
						return result | (int)lb;
					}
					
					public boolean isConst()
					{
						if (alpha.isConst()) {
							if ((alpha.eval() & 0xFF000000L) == 0)
								return saidConst = true;
							
							return saidConst = red.isConst() && green.isConst() && blue.isConst();
						} else {
						    return false;
						}
					}
				};
			} else
			if (spec instanceof BgColorOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof ColorBlend) {
				ColorBlend map = (ColorBlend) spec;
				final DoubleEval value = DoubleEval.Factory.create(map.number, evaluator);
				final DoubleEval[] limits = new DoubleEval[map.limits.length];
				for (int a=0; a<limits.length; a++)
					limits[a] = DoubleEval.Factory.create(map.limits[a], evaluator);
				final ColorEval[] colors = new ColorEval[map.colors.length];
				for (int a=0; a<colors.length; a++)
					colors[a] = ColorEval.Factory.create(map.colors[a], evaluator);
				
				return new ColorEval() {
					public int eval()
					{
						double val = value.eval();
						double prev = limits[0].eval();
						if (val <= prev)
							return colors[0].eval();
						
						for (int a=1; a<limits.length; a++) {
							double next = limits[a].eval();
							if (val <= next) {
								double at = (val - prev) / (next - prev);
								double re = 1.0 - at;
								
								int argb0 = colors[a-1].eval();
								int argb1 = colors[a].eval();
								
								int alpha = (int)(0.5 + re * ((argb0 >>> 24)       ) + at * ((argb1 >>> 24)       ));
								int red   = (int)(0.5 + re * ((argb0 >>> 16) & 0xFF) + at * ((argb1 >>> 16) & 0xFF));
								int green = (int)(0.5 + re * ((argb0 >>>  8) & 0xFF) + at * ((argb1 >>>  8) & 0xFF));
								int blue  = (int)(0.5 + re * ((argb0       ) & 0xFF) + at * ((argb1       ) & 0xFF));
								
								return (alpha << 24) | (red << 16) | (green << 8) | blue;
							} else {
								prev = next;
							}
						}
						return colors[colors.length-1].eval();
					}
					
					public boolean isConst()
					{
						return false; // TODO
					}
				};
			} else
			if (spec instanceof ColorMap) {
				ColorMap map = (ColorMap) spec;
				final DoubleEval value = DoubleEval.Factory.create(map.number, evaluator);
				final DoubleEval[] limits = new DoubleEval[map.limits.length];
				for (int a=0; a<limits.length; a++)
					limits[a] = DoubleEval.Factory.create(map.limits[a], evaluator);
				final ColorEval[] colors = new ColorEval[map.colors.length];
				for (int a=0; a<colors.length; a++)
					colors[a] = ColorEval.Factory.create(map.colors[a], evaluator);
				
				return new ColorEval() {
					public int eval()
					{
						double val = value.eval();
						for (int a=0; a<limits.length; a++)
							if (val < limits[a].eval())
								return colors[a].eval();
						return colors[limits.length].eval();
					}
					
					public boolean isConst()
					{
					    return false;
					    // TODO
					}
				};
			} else
			if (spec instanceof ColorProcess) {
				ColorProcess cp = (ColorProcess) spec;
				final ColorEval base = create(cp.base, evaluator);
				final int op = cp.type;
				return new ColorEval() {
					public int eval()
					{
						int argb = base.eval();
						switch(op) {
						/** r,g,b = 0.3*r + 0.59*g + 0.11*b */
						case ColorProcess.T_GRAYSCALE:
							int r = (argb >>> 16) & 0xFF;
							int g = (argb >>>  8) & 0xFF;
							int b = (argb       ) & 0xFF;
							int gray = (r*30 + g*59 + b*11 + 50) / 100;
							return (argb & 0xFF000000) | (gray * 0x10101);
							
						case ColorProcess.T_INVERT:
							return argb ^ 0x00FFFFFF;

						case ColorProcess.T_SHIFT:
							return argb ^ 0x00808080;

						case ColorProcess.T_BRIGHTER:
							return (0xFF000000 & argb) | // alpha
							       ((((0x00FF00FF & argb) + 0x00FF00FF) >>> 1) & 0x00FF00FF) | // red + blue
							       ((((0x0000FF00 & argb) + 0x0000FF00) >>> 1) & 0x0000FF00); // green

						case ColorProcess.T_DARKER:
							return (0xFF000000 & argb) |
							     (((0x00FFFFFF & argb) >>> 1) & 0x007F7F7F);
						
						default:
							throw new IllegalStateException();
						}
					}
					
					public boolean isConst()
					{
						return base.isConst();
					}
				};
			} else
			if (spec instanceof ColorTernary) {
				ColorTernary ct = (ColorTernary) spec;
				
				final BooleanEval cond = BooleanEval.Factory.create(ct.condition, evaluator);
				final ColorEval ifTrue = create(ct.ifTrue, evaluator);
				final ColorEval ifFalse = create(ct.ifFalse, evaluator);
				
				return new ColorEval() {
					boolean saidConst = false;
					
					public int eval()
					{
						return saidConst ? ifTrue.eval() :
							(cond.eval() ? ifTrue.eval() : ifFalse.eval());
					}
					
					public boolean isConst()
					{
						if (cond.isConst()) {
							if (cond.eval()) {
								return ifTrue.isConst();
							} else {
								return ifFalse.isConst();
							}
						} else {
							return saidConst = ifTrue.isConst() && ifFalse.isConst() && ifTrue.eval() == ifFalse.eval();
						}
					}
				};
			} else
			if (spec instanceof FgColorOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof FillBgColorFromField) {
				FillBgColorFromField ff = (FillBgColorFromField) spec;
				final int styleIdx = evaluator.getFieldStyleIdx(ff.field);
				return new ColorEval() {
					public int eval()
					{
						return evaluator.valueFieldStyles(styleIdx).getBackFillColor();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof FillFgColorFromField) {
				FillFgColorFromField ff = (FillFgColorFromField) spec;
				final int styleIdx = evaluator.getFieldStyleIdx(ff.field);
				return new ColorEval() {
					public int eval()
					{
						return evaluator.valueFieldStyles(styleIdx).getForeFillColor();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof FontColorFromField) {
				FontColorFromField ff = (FontColorFromField) spec;
				final int styleIdx = evaluator.getFieldStyleIdx(ff.field);
				return new ColorEval() {
					public int eval()
					{
						return evaluator.valueFieldStyles(styleIdx).getTextColor();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof FontColorOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof LineColorFromField) {
				LineColorFromField ff = (LineColorFromField) spec;
				final int styleIdx = evaluator.getFieldStyleIdx(ff.field);
				return new ColorEval() {
					public int eval()
					{
						return evaluator.valueFieldStyles(styleIdx).getLineColor();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof LineColorOf) {
				throw new IllegalStateException();
			} else
			if (spec instanceof ConstColor) {
				final int argb = ((ConstColor) spec).argb;
				return new ColorEval() {
					public int eval()
					{
						return argb;
					}
					
					public boolean isConst()
					{
						return true;
					}
				};
			} else
			if (spec instanceof SymbolColorFromField) {
				SymbolColorFromField ff = (SymbolColorFromField) spec;
				final int styleIdx = evaluator.getFieldStyleIdx(ff.field);
				return new ColorEval() {
					public int eval()
					{
						return evaluator.valueFieldStyles(styleIdx).getSymbolColor();
					}
					
					public boolean isConst()
					{
					    return false;
					}
				};
			} else
			if (spec instanceof SymbolColorOf) {
				throw new IllegalStateException();
			} else
				throw new UnsupportedOperationException();
        }
	}
}
