package com.sinergise.geopedia.client.ui.feature;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.core.constants.Icons;
import com.sinergise.geopedia.core.style.consts.ConstBool;
import com.sinergise.geopedia.core.style.consts.ConstColor;
import com.sinergise.geopedia.core.style.consts.ConstDouble;
import com.sinergise.geopedia.core.style.consts.ConstFillType;
import com.sinergise.geopedia.core.style.consts.ConstFontId;
import com.sinergise.geopedia.core.style.consts.ConstLineType;
import com.sinergise.geopedia.core.style.consts.ConstLong;
import com.sinergise.geopedia.core.style.consts.ConstSymbolId;
import com.sinergise.geopedia.core.style.defs.FillStyleDef;
import com.sinergise.geopedia.core.style.defs.LineStyleDef;
import com.sinergise.geopedia.core.style.defs.StyleDef;
import com.sinergise.geopedia.core.style.defs.SymbolStyleDef;
import com.sinergise.geopedia.core.style.defs.TextStyleDef;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;
import com.sinergise.geopedia.core.style.model.FillTypeSpec;
import com.sinergise.geopedia.core.style.model.FontId;
import com.sinergise.geopedia.core.style.model.FontIdSpec;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;
import com.sinergise.geopedia.core.style.model.LineType;
import com.sinergise.geopedia.core.style.model.LineTypeSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.model.SymbolId;
import com.sinergise.geopedia.core.style.model.SymbolIdSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;
import com.sinergise.geopedia.core.style.nulls.NullFillStyle;
import com.sinergise.geopedia.core.style.nulls.NullLineStyle;
import com.sinergise.geopedia.core.style.nulls.NullStyle;
import com.sinergise.geopedia.core.style.nulls.NullSymbolStyle;
import com.sinergise.geopedia.core.style.nulls.NullTextStyle;
import com.sinergise.geopedia.core.style.numbers.NamedConstant;
import com.sinergise.gwt.ui.PNGImage;



public class ShowStyle extends Composite
{
	HorizontalPanel panel = new HorizontalPanel();
	
	public ShowStyle(StyleSpec ss)
	{
		initWidget(panel);
		panel.setStylePrimaryName("Gisopedia-HideBorders"); // hide borders
		
		if (ss==null || ss instanceof NullStyle) {
            panel.add(nullIcon());
        } else if (canHandle(ss)) {
			render(panel, fillIcon(ss));
			render(panel, lineIcon(ss));
			render(panel, symIcon(ss));
			render(panel, textIcon(ss));
		} else {
			panel.add(new Label(ss.toString()));
		}
	}
	
	/**
	 * render only non-null style components.
	 * @param parent panel
	 * @param child widget
	 */
	private void render(Panel parent, Widget child) {
		RootPanel.get().add(child);
		int width = child.getOffsetWidth();
		child.removeFromParent();
		if (width > 1) {
			parent.add(child);
		}
	}
	
	private Widget nullIcon() {
        return img("sicon/fill/0", Icons.Sizes.FILL_ICON_WIDTH, Icons.Sizes.FILL_ICON_HEIGHT);
    }

    private Widget fillIcon(StyleSpec ss)
    {
		String url = "sicon/fill/0";
		
		if (ss instanceof StyleDef) {
			StyleDef sd = (StyleDef) ss;
			FillStyleSpec fss = sd.fill;
            if (fss==null || fss instanceof NullFillStyle) {
                return img("trPix.gif",1,1);
            }
			if (fss instanceof FillStyleDef) {
				FillStyleDef fsd = (FillStyleDef) fss;
				
				int c2 = getVal(fsd.bgColor);
				int c1 = getVal(fsd.fgColor);
				int fid = ((ConstFillType)fsd.fillType).fillTypeId;
				
				url = "sicon/fill/"+fid+"?c1="+c1+"&c2="+c2;
			}
		}
        return img(url, Icons.Sizes.FILL_ICON_WIDTH, Icons.Sizes.FILL_ICON_HEIGHT);
    }
	
	private Widget lineIcon(StyleSpec ss)
    {
		String url = "sicon/line/0";
		
		if (ss instanceof StyleDef) {
			StyleDef sd = (StyleDef) ss;
			LineStyleSpec fss = sd.line;
            if (fss==null || fss instanceof NullLineStyle) {
                return img("trPix.gif",1,1);
            }
			if (fss instanceof LineStyleDef) {
				LineStyleDef fsd = (LineStyleDef) fss;
				
				int c1 = getVal(fsd.color);
				int lid = ((ConstLineType)fsd.lineType).lineTypeId;
				double w = clamp(fsd.lineWidth, LineType.MIN_WIDTH, LineType.MAX_WIDTH);
				
				url = "sicon/line/"+lid+"?c1="+c1+"&ls="+w;
			}
		}

		return img(url, Icons.Sizes.LINE_ICON_WIDTH, Icons.Sizes.LINE_ICON_HEIGHT);
    }
	
	private Widget symIcon(StyleSpec ss)
    {
		String url = "sicon/sym/0";
		
		if (ss instanceof StyleDef) {
			StyleDef sd = (StyleDef) ss;
			SymbolStyleSpec fss = sd.sym;
            if (fss==null || fss instanceof NullSymbolStyle) {
                return img("trPix.gif",1,1);
            }
			if (fss instanceof SymbolStyleDef) {
				SymbolStyleDef fsd = (SymbolStyleDef) fss;
				
				int c1 = getVal(fsd.color);
				int size = clamp(fsd.size, SymbolId.MIN_SIZE, SymbolId.MAX_SIZE);
				int sid = getVal(fsd.symbolId);
				
				url = "sicon/sym/"+sid+"?c1="+c1+"&ss="+size;
			}
		}

		return img(url, Icons.Sizes.SYM_ICON_WIDTH, Icons.Sizes.SYM_ICON_HEIGHT);
    }
	
	private Widget textIcon(StyleSpec ss)
    {
		String url = "sicon/font/0";
		
		if (ss instanceof StyleDef) {
			StyleDef sd = (StyleDef) ss;
			TextStyleSpec fss = sd.text;
            if (fss==null || fss instanceof NullTextStyle) {
                return img("trPix.gif",1,1);
            }            
			if (fss instanceof TextStyleDef) {
				TextStyleDef fsd = (TextStyleDef) fss;
				
				int c1 = getVal(fsd.color);
				boolean bold = getVal(fsd.bold);
				boolean italic = getVal(fsd.italic);
				int fid = getVal(fsd.fontId);
				int height = clamp(fsd.height, FontId.MIN_SIZE, FontId.MAX_SIZE);
				
				url = "sicon/font/"+fid+"?c1="+c1+"&fs="+height+"&bold="+(bold?1:0)+"&italic="+(italic?1:0);
			}
		}

		return img(url, Icons.Sizes.FONT_ICON_WIDTH, Icons.Sizes.FONT_ICON_HEIGHT);
    }
	
	private int getVal(FontIdSpec fontId)
    {
		return ((ConstFontId)fontId).fontId;
    }

	private boolean getVal(BooleanSpec bold)
    {
		return ((ConstBool)bold).value;
    }

	private int getVal(SymbolIdSpec symbolId)
    {
		return ((ConstSymbolId)symbolId).symbolId;
    }

	private int clamp(NumberSpec num, int min, int max)
	{
		double val = getVal(num);
		if (val <= min)
			return min;
		if (val >= max)
			return max;
		return (int)Math.round(val);
	}
	
	private double clamp(NumberSpec num, double min, double max)
	{
		double val = getVal(num);
		
		if (val < min)
			return min;
		if (val > max)
			return max;
		return val;
	}
	
	private double getVal(NumberSpec num)
	{
		if (num instanceof ConstDouble)
			return ((ConstDouble)num).value;
		if (num instanceof ConstLong)
			return ((ConstLong)num).value;
		return Math.PI;
	}
	
	private int getVal(ColorSpec bgColor)
    {
		return ((ConstColor)bgColor).argb;
    }

	static PNGImage img(String url, int w, int h)
	{
		return new PNGImage(url, true, w, h, false);
	}

	boolean canHandle(StyleSpec ss)
	{
		if (ss instanceof StyleDef) {
			StyleDef def = (StyleDef) ss;
			return canHandle(def.fill) && canHandle(def.line) && canHandle(def.sym) && canHandle(def.text);
		} else
		if (ss instanceof NullStyle) {
			return true;
		} else
			return false;
	}

	private boolean canHandle(TextStyleSpec text)
    {
		if (text instanceof TextStyleDef) {
	        TextStyleDef tsd = (TextStyleDef) text;
	        return canHandle(tsd.color) && canHandle(tsd.bold) &&
	               canHandle(tsd.fontId) && canHandle(tsd.height) &&
	               canHandle(tsd.italic);
        } else
        if (text instanceof NullTextStyle) {
        	return true;
        } else
        	return false;
    }

	private boolean canHandle(FontIdSpec fontId)
    {
		return fontId instanceof ConstFontId;
    }

	private boolean canHandle(BooleanSpec bold)
    {
		return bold instanceof ConstBool;
    }

	private boolean canHandle(SymbolStyleSpec sym)
    {
		if (sym instanceof SymbolStyleDef) {
			SymbolStyleDef ssd = (SymbolStyleDef) sym;
			
			return canHandle(ssd.color) && canHandle(ssd.size) && canHandle(ssd.symbolId);
		} else
		if (sym instanceof NullSymbolStyle) {
			return true;
		} else
			return false;
    }

	private boolean canHandle(SymbolIdSpec symbolId)
    {
		return symbolId instanceof ConstSymbolId;
    }

	private boolean canHandle(NumberSpec size)
    {
		if (size instanceof ConstDouble)
			return true;
		if (size instanceof ConstLong)
			return true;
		if (size instanceof NamedConstant)
			return true;
	    return false;
    }

	private boolean canHandle(LineStyleSpec line)
    {
		if (line instanceof LineStyleDef) {
			LineStyleDef lsd = (LineStyleDef) line;
			
			return canHandle(lsd.color) && canHandle(lsd.lineType) && canHandle(lsd.lineWidth);
		} else
		if (line instanceof NullLineStyle) {
			return true;
		} else
			return false;
    }

	private boolean canHandle(LineTypeSpec lineType)
    {
		return lineType instanceof ConstLineType;
    }

	private boolean canHandle(FillStyleSpec fill)
    {
		if (fill instanceof FillStyleDef) {
			FillStyleDef fsd = (FillStyleDef) fill;
			return canHandle(fsd.bgColor) && canHandle(fsd.fgColor) && canHandle(fsd.fillType);
		} else
		if (fill instanceof NullFillStyle) {
			return true;
		} else
			return false;
    }

	private boolean canHandle(FillTypeSpec fillType)
    {
		return fillType instanceof ConstFillType;
    }

	private boolean canHandle(ColorSpec bgColor)
    {
		return bgColor instanceof ConstColor;
    }
}
