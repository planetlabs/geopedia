package com.sinergise.geopedia.style.processor;

import java.sql.SQLException;
import java.util.HashMap;

import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.walk.TablePath;
import com.sinergise.geopedia.core.style.bools.AndOrXor;
import com.sinergise.geopedia.core.style.bools.Not;
import com.sinergise.geopedia.core.style.colors.ARGB;
import com.sinergise.geopedia.core.style.colors.ColorBlend;
import com.sinergise.geopedia.core.style.colors.ColorComponent;
import com.sinergise.geopedia.core.style.colors.ColorMap;
import com.sinergise.geopedia.core.style.colors.ColorProcess;
import com.sinergise.geopedia.core.style.consts.ConstBool;
import com.sinergise.geopedia.core.style.consts.ConstColor;
import com.sinergise.geopedia.core.style.consts.ConstDouble;
import com.sinergise.geopedia.core.style.consts.ConstFillType;
import com.sinergise.geopedia.core.style.consts.ConstFontId;
import com.sinergise.geopedia.core.style.consts.ConstLineType;
import com.sinergise.geopedia.core.style.consts.ConstLong;
import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.consts.ConstSymbolId;
import com.sinergise.geopedia.core.style.dates.DateBoolProp;
import com.sinergise.geopedia.core.style.dates.DateNumberProp;
import com.sinergise.geopedia.core.style.defs.FillStyleDef;
import com.sinergise.geopedia.core.style.defs.LineStyleDef;
import com.sinergise.geopedia.core.style.defs.StyleDef;
import com.sinergise.geopedia.core.style.defs.SymbolStyleDef;
import com.sinergise.geopedia.core.style.defs.TextStyleDef;
import com.sinergise.geopedia.core.style.fields.BooleanField;
import com.sinergise.geopedia.core.style.fields.DateField;
import com.sinergise.geopedia.core.style.fields.NumberField;
import com.sinergise.geopedia.core.style.fields.StringField;
import com.sinergise.geopedia.core.style.fields.StyleField;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.DateSpec;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;
import com.sinergise.geopedia.core.style.model.FillType;
import com.sinergise.geopedia.core.style.model.FillTypeSpec;
import com.sinergise.geopedia.core.style.model.FontId;
import com.sinergise.geopedia.core.style.model.FontIdSpec;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;
import com.sinergise.geopedia.core.style.model.LineType;
import com.sinergise.geopedia.core.style.model.LineTypeSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;
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
import com.sinergise.geopedia.core.style.numbers.CompareNum;
import com.sinergise.geopedia.core.style.numbers.CurrentScale;
import com.sinergise.geopedia.core.style.numbers.NParamFunc;
import com.sinergise.geopedia.core.style.numbers.NamedConstant;
import com.sinergise.geopedia.core.style.numbers.NumBinaryOp;
import com.sinergise.geopedia.core.style.numbers.NumUnaryOp;
import com.sinergise.geopedia.core.style.numbers.OneParamFunc;
import com.sinergise.geopedia.core.style.numbers.TwoParamFunc;
import com.sinergise.geopedia.core.style.proxys.BgColorOf;
import com.sinergise.geopedia.core.style.proxys.BoldOf;
import com.sinergise.geopedia.core.style.proxys.FgColorOf;
import com.sinergise.geopedia.core.style.proxys.FillStyleOf;
import com.sinergise.geopedia.core.style.proxys.FillTypeOf;
import com.sinergise.geopedia.core.style.proxys.FontColorOf;
import com.sinergise.geopedia.core.style.proxys.FontHeightOf;
import com.sinergise.geopedia.core.style.proxys.FontIdOf;
import com.sinergise.geopedia.core.style.proxys.ItalicOf;
import com.sinergise.geopedia.core.style.proxys.LayersStyle;
import com.sinergise.geopedia.core.style.proxys.LayersTextRep;
import com.sinergise.geopedia.core.style.proxys.LineColorOf;
import com.sinergise.geopedia.core.style.proxys.LineStyleOf;
import com.sinergise.geopedia.core.style.proxys.LineTypeOf;
import com.sinergise.geopedia.core.style.proxys.LineWidthOf;
import com.sinergise.geopedia.core.style.proxys.SymbolColorOf;
import com.sinergise.geopedia.core.style.proxys.SymbolIdOf;
import com.sinergise.geopedia.core.style.proxys.SymbolSizeOf;
import com.sinergise.geopedia.core.style.proxys.SymbolStyleOf;
import com.sinergise.geopedia.core.style.proxys.SymbolTextOf;
import com.sinergise.geopedia.core.style.proxys.TextStyleOf;
import com.sinergise.geopedia.core.style.proxys.ThemeStyle;
import com.sinergise.geopedia.core.style.strings.CompareString;
import com.sinergise.geopedia.core.style.strings.StringConcat;
import com.sinergise.geopedia.core.style.strings.StringFromBoolean;
import com.sinergise.geopedia.core.style.strings.StringFromDate;
import com.sinergise.geopedia.core.style.strings.StringFromNumber;
import com.sinergise.geopedia.core.style.ternaries.BoolTernary;
import com.sinergise.geopedia.core.style.ternaries.ColorTernary;
import com.sinergise.geopedia.core.style.ternaries.DateTernary;
import com.sinergise.geopedia.core.style.ternaries.FillStyleTernary;
import com.sinergise.geopedia.core.style.ternaries.FillTypeTernary;
import com.sinergise.geopedia.core.style.ternaries.FontIdTernary;
import com.sinergise.geopedia.core.style.ternaries.LineStyleTernary;
import com.sinergise.geopedia.core.style.ternaries.LineTypeTernary;
import com.sinergise.geopedia.core.style.ternaries.NumericTernary;
import com.sinergise.geopedia.core.style.ternaries.StringTernary;
import com.sinergise.geopedia.core.style.ternaries.StyleTernary;
import com.sinergise.geopedia.core.style.ternaries.SymbolIdTernary;
import com.sinergise.geopedia.core.style.ternaries.SymbolStyleTernary;
import com.sinergise.geopedia.core.style.ternaries.TextStyleTernary;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.db.entities.TableUtils;
import com.sinergise.geopedia.style.ParseStyleException;
import com.sinergise.geopedia.style.ReflectiveStyleVisitor;
import com.sinergise.geopedia.style.StyleCodec;
import com.sinergise.geopedia.style.processor.extra.BoldFromField;
import com.sinergise.geopedia.style.processor.extra.FillBgColorFromField;
import com.sinergise.geopedia.style.processor.extra.FillFgColorFromField;
import com.sinergise.geopedia.style.processor.extra.FillTypeFromField;
import com.sinergise.geopedia.style.processor.extra.FontColorFromField;
import com.sinergise.geopedia.style.processor.extra.FontHeightFromField;
import com.sinergise.geopedia.style.processor.extra.FontIdFromField;
import com.sinergise.geopedia.style.processor.extra.ItalicFromField;
import com.sinergise.geopedia.style.processor.extra.LineColorFromField;
import com.sinergise.geopedia.style.processor.extra.LineTypeFromField;
import com.sinergise.geopedia.style.processor.extra.LineWidthFromField;
import com.sinergise.geopedia.style.processor.extra.SymbolColorFromField;
import com.sinergise.geopedia.style.processor.extra.SymbolIdFromField;
import com.sinergise.geopedia.style.processor.extra.SymbolSizeFromField;
import com.sinergise.geopedia.style.processor.extra.SymbolTextFromField;

public class StyleFlattener
{
	StyleSpec first;
	Table firstTable;
	String themeTableStyle;
	TablePath firstTablePath;
	StyleSpec themeStyle;
	
	public ColorSpec lineColor;
	public LineTypeSpec lineType;
	public NumberSpec lineWidth;
	
	public ColorSpec fillBgColor;
	public ColorSpec fillFgColor;
	public FillTypeSpec fillType;
	
	public SymbolIdSpec symbolId;
	public ColorSpec symbolColor;
	public NumberSpec symbolSize;
	public StringSpec symbolText;
	
	public FontIdSpec textFont;
	public ColorSpec textColor;
	public NumberSpec fontSize;
	public BooleanSpec bold;
	public BooleanSpec italic;

	
	HashMap<TablePath, StyleSpec> tableStyles = new HashMap<TablePath, StyleSpec>();
	HashMap<TablePath, StringSpec> tableTextreps = new HashMap<TablePath, StringSpec>();
	
	public int scale;
	private MetaData meta;
	public StyleFlattener(StyleSpec ss, Table table, String themeTableStyle, int scale, MetaData meta)
	{
		this.meta=meta;
		first = ss;
		firstTable = table;
		this.themeTableStyle = themeTableStyle;
		this.firstTablePath = new TablePath(firstTable.id);
		this.scale = scale;
	}
	
	public void go() throws ParseStyleException, SQLException
	{
		StyleSpec first = (StyleSpec) this.first.clone();
		//tableStyles.put(firstTablePath, first);

		lineColor = resolvethLineColor(first);
		lineType = resolvethLineType(first);
		lineWidth = resolvethLineWidth(first);

		fillBgColor = resolvethFillBgColor(first);
		fillFgColor = resolvethFillFgColor(first);
		fillType = resolvethFillType(first);

		symbolId = resolvethSymbolId(first);
		symbolColor = resolvethSymbolColor(first);
		symbolSize = resolvethSymbolSize(first);
		symbolText = resolvethSymbolText(first);

		textFont = resolvethFontId(first);
		textColor = resolvethFontColor(first);
		fontSize = resolvethFontHeight(first);
		bold = resolvethBoldOf(first);
		italic = resolvethItalicOf(first);
	}

	private FontIdSpec resolvethFontId(StyleSpec styleSpec) throws ParseStyleException, SQLException
    {
		if (styleSpec instanceof NullStyle) {
			return new ConstFontId(FontId.NONE);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethFontId(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethFontId(((StyleDef)styleSpec).text);
		} else
		if (styleSpec instanceof StyleField) {
			return new FontIdFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new FontIdTernary(resolvethBoolean(st.condition), resolvethFontId(st.ifTrue), resolvethFontId(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethFontId(getThemeStyle());
		} else {
			throw new UnsupportedOperationException();
		}
    }
	
	private FontIdSpec resolvethFontId(TextStyleSpec text) throws ParseStyleException, SQLException
    {
		if (text instanceof NullTextStyle) {
			return new ConstFontId(FontId.NONE);
		} else
		if (text instanceof TextStyleDef) {
			return resolvethFontId(((TextStyleDef)text).fontId);
		} else
		if (text instanceof TextStyleOf) {
			return resolvethFontId(((TextStyleOf)text).style);
		} else
		if (text instanceof TextStyleTernary) {
			TextStyleTernary tst = (TextStyleTernary)text;
			return new FontIdTernary(resolvethBoolean(tst.condition), resolvethFontId(tst.ifTrue), resolvethFontId(tst.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private FontIdSpec resolvethFontId(FontIdSpec fontId) throws ParseStyleException, SQLException
    {
		if (fontId instanceof FontIdFromField) {
			return fontId;
		} else
		if (fontId instanceof FontIdOf) {
			return resolvethFontId(((FontIdOf)fontId).textStyle);
		} else
		if (fontId instanceof FontIdTernary) {
			FontIdTernary fit = (FontIdTernary) fontId;
			return new FontIdTernary(resolvethBoolean(fit.condition), resolvethFontId(fit.ifTrue), resolvethFontId(fit.ifFalse));
		} else
		if (fontId instanceof ConstFontId) {
			return fontId;
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private SymbolIdSpec resolvethSymbolId(StyleSpec styleSpec) throws ParseStyleException, SQLException
    {
		if (styleSpec instanceof NullStyle) {
			return new ConstSymbolId(SymbolId.NONE);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethSymbolId(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethSymbolId(((StyleDef)styleSpec).sym);
		} else
		if (styleSpec instanceof StyleField) {
			return new SymbolIdFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new SymbolIdTernary(resolvethBoolean(st.condition), resolvethSymbolId(st.ifTrue), resolvethSymbolId(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethSymbolId(getThemeStyle());
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private SymbolIdSpec resolvethSymbolId(SymbolStyleSpec sym) throws ParseStyleException, SQLException
    {
		if (sym instanceof NullSymbolStyle) {
			return new ConstSymbolId(SymbolId.NONE);
		} else
		if (sym instanceof SymbolStyleDef) {
			return resolvethSymbolId(((SymbolStyleDef)sym).symbolId);
		} else
		if (sym instanceof SymbolStyleOf) {
			return resolvethSymbolId(((SymbolStyleOf)sym).style);
		} else
		if (sym instanceof SymbolStyleTernary) {
			SymbolStyleTernary sst = (SymbolStyleTernary) sym;
			return new SymbolIdTernary(resolvethBoolean(sst.condition), resolvethSymbolId(sst.ifTrue), resolvethSymbolId(sst.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private SymbolIdSpec resolvethSymbolId(SymbolIdSpec symbolId) throws ParseStyleException, SQLException
    {
		if (symbolId instanceof ConstSymbolId) {
			return symbolId;
		} else
		if (symbolId instanceof SymbolIdFromField) {
			return symbolId;
		} else
		if (symbolId instanceof SymbolIdOf) {
			return resolvethSymbolId(((SymbolIdOf)symbolId).symbolStyle);
		} else
		if (symbolId instanceof SymbolIdTernary) {
			SymbolIdTernary sit = (SymbolIdTernary) symbolId;
			return new SymbolIdTernary(resolvethBoolean(sit.condition), resolvethSymbolId(sit.ifTrue), resolvethSymbolId(sit.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private FillTypeSpec resolvethFillType(StyleSpec styleSpec) throws ParseStyleException, SQLException
    {
		if (styleSpec instanceof NullStyle) {
			return new ConstFillType(FillType.NONE);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethFillType(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethFillType(((StyleDef)styleSpec).fill);
		} else
		if (styleSpec instanceof StyleField) {
			return new FillTypeFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new FillTypeTernary(resolvethBoolean(st.condition), resolvethFillType(st.ifTrue), resolvethFillType(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethFillType(getThemeStyle());
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private FillTypeSpec resolvethFillType(FillStyleSpec line) throws ParseStyleException, SQLException
    {
		if (line instanceof FillStyleDef) {
			return resolvethFillType(((FillStyleDef)line).fillType);
		} else
		if (line instanceof FillStyleOf) {
			return resolvethFillType(((FillStyleOf)line).style);
		} else
		if (line instanceof FillStyleTernary) {
			FillStyleTernary lst = (FillStyleTernary) line;
			return new FillTypeTernary(resolvethBoolean(lst.condition), resolvethFillType(lst.ifTrue), resolvethFillType(lst.ifFalse));
		} else
		if (line instanceof NullFillStyle) {
			return new ConstFillType(FillType.NONE);
		} else {
			throw new UnsupportedOperationException();
		}
    }
	
	private FillTypeSpec resolvethFillType(FillTypeSpec fillType) throws ParseStyleException, SQLException
    {
		if (fillType instanceof FillTypeFromField) {
			return fillType;
		} else
		if (fillType instanceof FillTypeOf) {
			return resolvethFillType(((FillTypeOf)fillType).fillStyle);
		} else
		if (fillType instanceof FillTypeTernary) {
			FillTypeTernary ftt = (FillTypeTernary) fillType;
			return new FillTypeTernary(resolvethBoolean(ftt.condition), resolvethFillType(ftt.ifTrue), resolvethFillType(ftt.ifFalse));
		} else
		if (fillType instanceof ConstFillType) {
			return fillType;
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private LineTypeSpec resolvethLineType(StyleSpec styleSpec) throws ParseStyleException, SQLException
    {
		if (styleSpec instanceof NullStyle) {
			return new ConstLineType(LineType.NONE);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethLineType(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethLineType(((StyleDef)styleSpec).line);
		} else
		if (styleSpec instanceof StyleField) {
			return new LineTypeFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new LineTypeTernary(resolvethBoolean(st.condition), resolvethLineType(st.ifTrue), resolvethLineType(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethLineType(getThemeStyle());
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private LineTypeSpec resolvethLineType(LineStyleSpec line) throws ParseStyleException, SQLException
    {
		if (line instanceof LineStyleDef) {
			return resolvethLineType(((LineStyleDef)line).lineType);
		} else
		if (line instanceof LineStyleOf) {
			return resolvethLineType(((LineStyleOf)line).style);
		} else
		if (line instanceof LineStyleTernary) {
			LineStyleTernary lst = (LineStyleTernary) line;
			return new LineTypeTernary(resolvethBoolean(lst.condition), resolvethLineType(lst.ifTrue), resolvethLineType(lst.ifFalse));
		} else
		if (line instanceof NullLineStyle) {
			return new ConstLineType(LineType.NONE);
		} else {
			throw new UnsupportedOperationException();
		}
    }
	
	private LineTypeSpec resolvethLineType(LineTypeSpec lineType) throws ParseStyleException, SQLException
    {
		if (lineType instanceof ConstLineType) {
			return lineType;
		} else
		if (lineType instanceof LineTypeFromField) {
			return lineType;
		} else
		if (lineType instanceof LineTypeOf) {
			return resolvethLineType(((LineTypeOf)lineType).lineStyle);
		} else
		if (lineType instanceof LineTypeTernary) {
			LineTypeTernary ltt = (LineTypeTernary) lineType;
			return new LineTypeTernary(resolvethBoolean(ltt.condition), resolvethLineType(ltt.ifTrue), resolvethLineType(ltt.ifFalse));
		} else
			throw new UnsupportedOperationException();
    }

	private StyleSpec getInner(TablePath tp) throws ParseStyleException, SQLException
	{
		StyleSpec res = tableStyles.get(tp);
		if (res != null)
			return res;
		
		res = (StyleSpec) TableUtils.getStyleSpecForTable(tp.lastTableId(), meta).clone();
		res.accept(new PathPrepender(tp));
		
		tableStyles.put(tp, res);
		return res;
	}
	
	private StringSpec getInnerTextRep(TablePath tp) throws ParseStyleException, SQLException
	{
		StringSpec res = tableTextreps.get(tp);
		if (res != null)
			return res;
		
		res = (StringSpec) TableUtils.getTextRepSpecForTable(tp.lastTableId(), meta).clone();
		res.accept(new PathPrepender(tp));
		
		tableTextreps.put(tp, res);
		return res;
	}
	
	private ColorSpec resolvethLineColor(StyleSpec styleSpec) throws ParseStyleException, SQLException
	{
		if (styleSpec instanceof NullStyle) {
			return new ConstColor(0);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethLineColor(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethLineColor(((StyleDef)styleSpec).line);
		} else
		if (styleSpec instanceof StyleField) {
			return new LineColorFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new ColorTernary(resolvethBoolean(st.condition), resolvethLineColor(st.ifTrue), resolvethLineColor(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethLineColor(getThemeStyle());
		} else {
			throw new IllegalStateException("Unknown style spec class: "+styleSpec.getClass().getName());
		}
	}
	
	private ColorSpec resolvethLineColor(LineStyleSpec lss) throws ParseStyleException, SQLException
	{
		if (lss instanceof LineStyleDef) {
			LineStyleDef lsd = (LineStyleDef) lss;
			return resolvethColor(lsd.color);
		} else
		if (lss instanceof LineStyleOf) {
			LineStyleOf lso = (LineStyleOf) lss;
			return resolvethLineColor(lso.style);
		} else
		if (lss instanceof LineStyleTernary) {
			LineStyleTernary lst = (LineStyleTernary) lss;
			return new ColorTernary(resolvethBoolean(lst.condition), resolvethLineColor(lst.ifTrue), resolvethLineColor(lst.ifFalse));
		} else
		if (lss instanceof NullLineStyle) {
			return new ConstColor(0);
		} else {
			throw new IllegalStateException("Unknown line style spec class: "+lss.getClass().getName());
		}
	}
	
	private ColorSpec resolvethColor(ColorSpec cs) throws ParseStyleException, SQLException
	{
		if (cs instanceof ARGB) {
			ARGB argb = (ARGB) cs;
			return new ARGB(resolvethNumber(argb.alpha), resolvethNumber(argb.red), resolvethNumber(argb.green), resolvethNumber(argb.blue));
		} else
		if (cs instanceof BgColorOf) {
			BgColorOf bco = (BgColorOf) cs;
			return resolvethFillBgColor(bco.fillStyle);
		} else
		if (cs instanceof ColorBlend) {
			ColorBlend cb = (ColorBlend) cs;
			NumberSpec num = resolvethNumber(cb.number);
			ColorSpec[] colors = cb.colors.clone();
			for (int a=0; a<colors.length; a++)
                colors[a] = resolvethColor(colors[a]);
			NumberSpec[] limits = cb.limits.clone();
			for (int a=0; a<limits.length; a++)
                limits[a] = resolvethNumber(limits[a]);
			return new ColorBlend(num, colors, limits);
		} else
		if (cs instanceof ColorMap) {
			ColorMap cm = (ColorMap) cs;
			NumberSpec num = resolvethNumber(cm.number);
			ColorSpec[] colors = cm.colors.clone();
			for (int a=0; a<colors.length; a++)
                colors[a] = resolvethColor(colors[a]);
			NumberSpec[] limits = cm.limits.clone();
			for (int a=0; a<limits.length; a++)
                limits[a] = resolvethNumber(limits[a]);
			return new ColorMap(num, colors, limits);
		} else
		if (cs instanceof ColorProcess) {
			ColorProcess cp = (ColorProcess) cs;
			return new ColorProcess(cp.type, resolvethColor(cp.base));
		} else
		if (cs instanceof ColorTernary) {
			ColorTernary ct = (ColorTernary) cs;
			return new ColorTernary(resolvethBoolean(ct.condition), resolvethColor(ct.ifTrue), resolvethColor(ct.ifFalse));
		} else
		if (cs instanceof FgColorOf) {
			FgColorOf fco = (FgColorOf) cs;
			return resolvethFillFgColor(fco.fillStyle);
		} else
		if (cs instanceof FontColorOf) {
			FontColorOf fco = (FontColorOf) cs;
			return resolvethFontColor(fco.textStyle);
		} else
		if (cs instanceof LineColorFromField) {
			return cs;
		} else
		if (cs instanceof LineColorOf) {
			LineColorOf lco = (LineColorOf) cs;
			return resolvethLineColor(lco.lineStyle);
		} else
		if (cs instanceof ConstColor) {
			return cs;
		} else
		if (cs instanceof SymbolColorOf) {
			SymbolColorOf sco = (SymbolColorOf) cs;
			return resolvethSymbolColor(sco.symbolStyle);
		} else {
			throw new IllegalStateException("Unknown color spec class: "+cs.getClass().getName());
		}
	}
	
	private ColorSpec resolvethFontColor(TextStyleSpec textStyle) throws ParseStyleException, SQLException
    {
		if (textStyle instanceof NullTextStyle) {
			return new ConstColor(0);
		} else
		if (textStyle instanceof TextStyleDef) {
			return resolvethColor(((TextStyleDef)textStyle).color);
		} else
		if (textStyle instanceof TextStyleOf) {
			return resolvethFontColor(((TextStyleOf)textStyle).style);
		} else
		if (textStyle instanceof TextStyleTernary) {
			TextStyleTernary tst = (TextStyleTernary) textStyle;
			return new ColorTernary(resolvethBoolean(tst.condition), resolvethFontColor(tst.ifTrue), resolvethFontColor(tst.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
    }
	
	private ColorSpec resolvethFontColor(StyleSpec styleSpec) throws ParseStyleException, SQLException
    {
		if (styleSpec instanceof NullStyle) {
			return new ConstColor(0);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethFontColor(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethFontColor(((StyleDef)styleSpec).text);
		} else
		if (styleSpec instanceof StyleField) {
			return new FontColorFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new ColorTernary(resolvethBoolean(st.condition), resolvethFontColor(st.ifTrue), resolvethFontColor(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethFontColor(getThemeStyle());
		} else {
			throw new IllegalStateException("Unknown style spec class: "+styleSpec.getClass().getName());
		}
    }

	NumberSpec resolvethNumber(NumberSpec ns) throws ParseStyleException, SQLException 
	{
		if (ns instanceof ColorComponent) {
			ColorComponent cc = (ColorComponent) ns;
			return new ColorComponent(cc.getType(), resolvethColor(cc.color));
		} else
		if (ns instanceof ConstDouble) {
			return ns;
		} else
		if (ns instanceof ConstLong) {
			return ns;
		} else
		if (ns instanceof CurrentScale) {
			return new ConstLong(scale);
		} else
		if (ns instanceof DateNumberProp) {
			DateNumberProp dnp = (DateNumberProp) ns;
			return new DateNumberProp(dnp.field, resolvethDate(dnp.date));
		} else
		if (ns instanceof FontHeightOf) {
			FontHeightOf fho = (FontHeightOf) ns;
			return resolvethFontHeight(fho.textStyle);
		} else
		if (ns instanceof LineWidthOf) {
			LineWidthOf lwo = (LineWidthOf) ns;
			return resolvethLineWidth(lwo.lineStyle);
		} else
		if (ns instanceof NamedConstant) {
			return ns;
		} else
		if (ns instanceof NParamFunc) {
			NParamFunc npf = (NParamFunc) ns;
			NumberSpec[] args = npf.args.clone();
			for (int a=0; a<args.length; a++)
                args[a] = resolvethNumber(args[a]);
			return new NParamFunc(npf.func, args);
		} else
		if (ns instanceof NumberField) {
			return ns;
		} else
		if (ns instanceof NumBinaryOp) {
			NumBinaryOp nbo = (NumBinaryOp) ns;
			return new NumBinaryOp(resolvethNumber(nbo.left), nbo.op, resolvethNumber(nbo.right));
		} else
		if (ns instanceof NumericTernary) {
			NumericTernary nt = (NumericTernary) ns;
			return new NumericTernary(resolvethBoolean(nt.condition), resolvethNumber(nt.ifTrue), resolvethNumber(nt.ifFalse));
		} else
		if (ns instanceof NumUnaryOp) {
			NumUnaryOp nuo = (NumUnaryOp) ns;
			return new NumUnaryOp(nuo.op, resolvethNumber(nuo.base));
		} else
		if (ns instanceof OneParamFunc) {
			OneParamFunc opf = (OneParamFunc) ns;
			return new OneParamFunc(opf.func, resolvethNumber(opf.arg));
		} else
		if (ns instanceof SymbolSizeOf) {
			SymbolSizeOf sso = (SymbolSizeOf) ns;
			return resolvethSymbolSize(sso.symbolStyle);
		} else
		if (ns instanceof TwoParamFunc) {
			TwoParamFunc tpf = (TwoParamFunc) ns;
			return new TwoParamFunc(tpf.func, resolvethNumber(tpf.arg0), resolvethNumber(tpf.arg1));
		} else {
			throw new UnsupportedOperationException("Unknown number spec class: "+ns.getClass().getName());
		}
	}

	private NumberSpec resolvethFontHeight(TextStyleSpec textStyle) throws ParseStyleException, SQLException
    {
		if (textStyle instanceof NullTextStyle) {
			return new ConstLong(0);
		} else
		if (textStyle instanceof TextStyleDef) {
			return resolvethNumber(((TextStyleDef)textStyle).height);
		} else
		if (textStyle instanceof TextStyleOf) {
			return resolvethFontHeight(((TextStyleOf)textStyle).style);
		} else
		if (textStyle instanceof TextStyleTernary) {
			TextStyleTernary tst = (TextStyleTernary) textStyle;
			return new NumericTernary(resolvethBoolean(tst.condition), resolvethFontHeight(tst.ifTrue), resolvethFontHeight(tst.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private NumberSpec resolvethFontHeight(StyleSpec styleSpec) throws ParseStyleException, SQLException
    {
		if (styleSpec instanceof NullStyle) {
			return new ConstLong(0);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethFontHeight(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethFontHeight(((StyleDef)styleSpec).text);
		} else
		if (styleSpec instanceof StyleField) {
			return new FontHeightFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new NumericTernary(resolvethBoolean(st.condition), resolvethFontHeight(st.ifTrue), resolvethFontHeight(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethFontHeight(getThemeStyle());
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private NumberSpec resolvethLineWidth(LineStyleSpec lineStyle) throws ParseStyleException, SQLException
    {
		if (lineStyle instanceof LineStyleDef) {
			return resolvethNumber(((LineStyleDef)lineStyle).lineWidth);
		} else
		if (lineStyle instanceof LineStyleOf) {
			return resolvethLineWidth(((LineStyleOf)lineStyle).style);
		} else
		if (lineStyle instanceof LineStyleTernary) {
			LineStyleTernary lst = (LineStyleTernary) lineStyle;
			return new NumericTernary(resolvethBoolean(lst.condition), resolvethLineWidth(lst.ifTrue), resolvethLineWidth(lst.ifFalse));
		} else
		if (lineStyle instanceof NullLineStyle) {
			return new ConstLong(0);
		} else {
			throw new UnsupportedOperationException();
		}
    }

	private NumberSpec resolvethLineWidth(StyleSpec styleSpec) throws ParseStyleException, SQLException
    {
		if (styleSpec instanceof NullStyle) {
			return new ConstLong(0);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethLineWidth(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethLineWidth(((StyleDef)styleSpec).line);
		} else
		if (styleSpec instanceof StyleField) {
			return new LineWidthFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new NumericTernary(resolvethBoolean(st.condition), resolvethLineWidth(st.ifTrue), resolvethLineWidth(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethLineWidth(getThemeStyle());
		} else {
			throw new UnsupportedOperationException();
		}
    }

	public NumberSpec resolvethSymbolSize(SymbolStyleSpec sss) throws ParseStyleException, SQLException
	{
		if (sss instanceof NullSymbolStyle) {
			return new ConstLong(0);
		} else
		if (sss instanceof SymbolStyleDef) {
			return resolvethNumber(((SymbolStyleDef)sss).size);
		} else
		if (sss instanceof SymbolStyleOf) {
			SymbolStyleOf sso = (SymbolStyleOf) sss;
			return resolvethSymbolSize(sso.style);
		} else
		if (sss instanceof SymbolStyleTernary) {
			SymbolStyleTernary sst = (SymbolStyleTernary) sss;
			return new NumericTernary(resolvethBoolean(sst.condition), resolvethSymbolSize(sst.ifTrue), resolvethSymbolSize(sst.ifFalse));
		} else {
			throw new UnsupportedOperationException("Unknown symbol size spec: "+sss.getClass().getName());
		}
	}
	
	public NumberSpec resolvethSymbolSize(StyleSpec ss) throws ParseStyleException, SQLException
    {
    	if (ss instanceof NullStyle) {
    		return new ConstLong(0);
    	} else
    	if (ss instanceof LayersStyle) {
    		LayersStyle ls = (LayersStyle) ss;
    		StyleSpec inner = getInner(ls.tablePath);
    		return resolvethSymbolSize(inner);
    	} else
    	if (ss instanceof StyleDef) {
    		return resolvethSymbolSize(((StyleDef)ss).sym);
    	} else
    	if (ss instanceof StyleField) {
    		return new SymbolSizeFromField(((StyleField)ss).fieldPath);
    	} else
    	if (ss instanceof StyleTernary) {
    		StyleTernary st = (StyleTernary) ss;
    		return new NumericTernary(resolvethBoolean(st.condition), resolvethSymbolSize(st.ifTrue), resolvethSymbolSize(st.ifFalse));
    	} else
    	if (ss instanceof ThemeStyle) {
    		return resolvethSymbolSize(getThemeStyle());
    	} else {
    		throw new IllegalStateException("Unknown style spec class: "+ss.getClass().getName());
    	}
    }

	public StringSpec resolvethSymbolText(StyleSpec ss) throws ParseStyleException, SQLException
	{
		if (ss instanceof NullStyle) {
			return new ConstString("");
		} else
		if (ss instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) ss;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethSymbolText(inner);
		} else
		if (ss instanceof StyleDef) {
			return resolvethSymbolText(((StyleDef)ss).sym);
		} else
		if (ss instanceof StyleField) {
			return new SymbolTextFromField(((StyleField)ss).fieldPath);
		} else
		if (ss instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) ss;
			return new StringTernary(resolvethBoolean(st.condition), resolvethSymbolText(st.ifTrue), resolvethSymbolText(st.ifFalse));
		} else
		if (ss instanceof ThemeStyle) {
			return resolvethSymbolText(getThemeStyle());
		} else {
			throw new IllegalStateException("Unknown style spec class: "+ss.getClass().getName());
		}
	}
	
	public StringSpec resolvethSymbolText(SymbolStyleSpec sss) throws ParseStyleException, SQLException
	{
		if (sss instanceof NullSymbolStyle) {
			return new ConstString("");
		} else
		if (sss instanceof SymbolStyleDef) {
			return resolvethString(((SymbolStyleDef)sss).text);
		} else
		if (sss instanceof SymbolStyleOf) {
			return resolvethSymbolText(((SymbolStyleOf)sss).style);
		} else
		if (sss instanceof SymbolStyleTernary) {
			SymbolStyleTernary sst = (SymbolStyleTernary) sss;
			return new StringTernary(resolvethBoolean(sst.condition), resolvethSymbolText(sst.ifTrue), resolvethSymbolText(sst.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	public BooleanSpec resolvethBoldOf(TextStyleSpec tss) throws ParseStyleException, SQLException
	{
		if (tss instanceof NullTextStyle) {
			return new ConstBool(false);
		} else
		if (tss instanceof TextStyleDef) {
			return resolvethBoolean(((TextStyleDef)tss).bold);
		} else
		if (tss instanceof TextStyleOf) {
			return resolvethBoldOf(((TextStyleOf)tss).style);
		} else
		if (tss instanceof TextStyleTernary) {
			TextStyleTernary tst = (TextStyleTernary) tss;
			return new BoolTernary(resolvethBoolean(tst.condition), resolvethBoldOf(tst.ifTrue), resolvethBoldOf(tst.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	public BooleanSpec resolvethBoldOf(StyleSpec styleSpec) throws ParseStyleException, SQLException
	{
		if (styleSpec instanceof NullStyle) {
			return new ConstBool(false);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethBoldOf(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethBoldOf(((StyleDef)styleSpec).text);
		} else
		if (styleSpec instanceof StyleField) {
			return new BoldFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new BoolTernary(resolvethBoolean(st.condition), resolvethBoldOf(st.ifTrue), resolvethBoldOf(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethBoldOf(getThemeStyle());
		} else {
			throw new IllegalStateException("Unknown style spec class: "+styleSpec.getClass().getName());
		}
	}
	
	public BooleanSpec resolvethItalicOf(StyleSpec styleSpec) throws ParseStyleException, SQLException
	{
		if (styleSpec instanceof NullStyle) {
			return new ConstBool(false);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethItalicOf(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethItalicOf(((StyleDef)styleSpec).text);
		} else
		if (styleSpec instanceof StyleField) {
			return new ItalicFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new BoolTernary(resolvethBoolean(st.condition), resolvethItalicOf(st.ifTrue), resolvethItalicOf(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethItalicOf(getThemeStyle());
		} else {
			throw new IllegalStateException("Unknown style spec class: "+styleSpec.getClass().getName());
		}
	}
	
	public BooleanSpec resolvethItalicOf(TextStyleSpec tss) throws ParseStyleException, SQLException
	{
		if (tss instanceof NullTextStyle) {
			return new ConstBool(false);
		} else
		if (tss instanceof TextStyleDef) {
			return resolvethBoolean(((TextStyleDef)tss).italic);
		} else
		if (tss instanceof TextStyleOf) {
			return resolvethItalicOf(((TextStyleOf)tss).style);
		} else
		if (tss instanceof TextStyleTernary) {
			TextStyleTernary tst = (TextStyleTernary) tss;
			return new BoolTernary(resolvethBoolean(tst.condition), resolvethItalicOf(tst.ifTrue), resolvethItalicOf(tst.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public BooleanSpec resolvethBoolean(BooleanSpec bs) throws ParseStyleException, SQLException
	{
		if (bs instanceof AndOrXor) {
			AndOrXor aox = (AndOrXor) bs;
			return new AndOrXor(resolvethBoolean(aox.left), aox.type, resolvethBoolean(aox.right));
		} else
		if (bs instanceof BoldOf) {
			return resolvethBoldOf(((BoldOf)bs).textStyle);
		} else
		if (bs instanceof BooleanField) {
			return bs;
		} else
		if (bs instanceof BoolTernary) {
			BoolTernary bt = (BoolTernary) bs;
			return new BoolTernary(resolvethBoolean(bt.condition), resolvethBoolean(bt.ifTrue), resolvethBoolean(bt.ifFalse));
		} else
		if (bs instanceof CompareNum) {
			CompareNum cn = (CompareNum) bs;
			return new CompareNum(resolvethNumber(cn.left), cn.op, resolvethNumber(cn.right));
		} else
		if (bs instanceof CompareString) {
			CompareString cn = (CompareString) bs;
			return new CompareString(resolvethString(cn.left), cn.op, resolvethString(cn.right));
		} else
		if (bs instanceof ConstBool) {
			return bs;
		} else
		if (bs instanceof DateBoolProp) {
			DateBoolProp dbp = (DateBoolProp) bs;
			return new DateBoolProp(dbp.field, resolvethDate(dbp.date));
		} else
		if (bs instanceof ItalicOf) {
			return resolvethItalicOf(((ItalicOf)bs).textStyle);
		} else
		if (bs instanceof Not) {
			return new Not(resolvethBoolean(((Not)bs).base));
		} else {
			throw new UnsupportedOperationException("Unknown bool spec class: "+bs.getClass().getName());
		}
	}
	
	public StringSpec resolvethString(StringSpec ss) throws ParseStyleException, SQLException
	{
		if (ss instanceof ConstString) {
			return ss;
		} else
		if (ss instanceof StringConcat) {
			StringConcat sc = (StringConcat) ss;
			return new StringConcat(resolvethString(sc.left), resolvethString(sc.right));
		} else
		if (ss instanceof StringField) {
			return ss;
		} else
		if (ss instanceof LayersTextRep) {
			LayersTextRep ls = (LayersTextRep) ss;
			StringSpec inner = getInnerTextRep(ls.tablePath);
			return resolvethString(inner);
		} else
		if (ss instanceof StringFromBoolean) {
			return new StringFromBoolean(resolvethBoolean(((StringFromBoolean)ss).val));
		} else
		if (ss instanceof StringFromDate) {
			return new StringFromDate(resolvethDate(((StringFromDate)ss).val));
		} else
		if (ss instanceof StringFromNumber) {
			return new StringFromNumber(resolvethNumber(((StringFromNumber)ss).val));
		} else
		if (ss instanceof StringTernary) {
			StringTernary st = (StringTernary) ss;
			return new StringTernary(resolvethBoolean(st.condition), resolvethString(st.ifTrue), resolvethString(st.ifFalse));
		} else
		if (ss instanceof SymbolTextOf) {
			return resolvethSymbolText(((SymbolTextOf)ss).symbolStyle);
		} else {
			throw new UnsupportedOperationException("Unknown string spec class: "+ss.getClass().getName());
		}
	}
	
	public DateSpec resolvethDate(DateSpec ds) throws ParseStyleException, SQLException
	{
		if (ds instanceof DateField) {
			return ds;
		} else
		if (ds instanceof DateTernary) {
			DateTernary dt = (DateTernary) ds;
			return new DateTernary(resolvethBoolean(dt.condition), resolvethDate(dt.ifTrue), resolvethDate(dt.ifFalse));
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	public ColorSpec resolvethSymbolColor(SymbolStyleSpec fss) throws ParseStyleException, SQLException
	{
		if (fss instanceof SymbolStyleDef) {
			return resolvethColor(((SymbolStyleDef)fss).color);
		} else
		if (fss instanceof SymbolStyleOf) {
			return resolvethSymbolColor(((SymbolStyleOf)fss).style);
		} else
		if (fss instanceof SymbolStyleTernary) {
			SymbolStyleTernary fst = (SymbolStyleTernary) fss;
			return new ColorTernary(resolvethBoolean(fst.condition), resolvethSymbolColor(fst.ifTrue), resolvethSymbolColor(fst.ifFalse));
		} else
		if (fss instanceof NullSymbolStyle) {
			return new ConstColor(0);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public ColorSpec resolvethSymbolColor(StyleSpec styleSpec) throws ParseStyleException, SQLException
	{
		if (styleSpec instanceof NullStyle) {
			return new ConstColor(0);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethSymbolColor(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethSymbolColor(((StyleDef)styleSpec).sym);
		} else
		if (styleSpec instanceof StyleField) {
			return new SymbolColorFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new ColorTernary(resolvethBoolean(st.condition), resolvethSymbolColor(st.ifTrue), resolvethSymbolColor(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethSymbolColor(getThemeStyle());
		} else {
			throw new IllegalStateException("Unknown style spec class: "+styleSpec.getClass().getName());
		}
	}
	
	public ColorSpec resolvethFillBgColor(FillStyleSpec fss) throws ParseStyleException, SQLException
	{
		if (fss instanceof FillStyleDef) {
			return resolvethColor(((FillStyleDef)fss).bgColor);
		} else
		if (fss instanceof FillStyleOf) {
			return resolvethFillBgColor(((FillStyleOf)fss).style);
		} else
		if (fss instanceof FillStyleTernary) {
			FillStyleTernary fst = (FillStyleTernary) fss;
			return new ColorTernary(resolvethBoolean(fst.condition), resolvethFillBgColor(fst.ifTrue), resolvethFillBgColor(fst.ifFalse));
		} else
		if (fss instanceof NullFillStyle) {
			return new ConstColor(0);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	private ColorSpec resolvethFillBgColor(StyleSpec styleSpec) throws ParseStyleException, SQLException
	{
		if (styleSpec instanceof NullStyle) {
			return new ConstColor(0);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethFillBgColor(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethFillBgColor(((StyleDef)styleSpec).fill);
		} else
		if (styleSpec instanceof StyleField) {
			return new FillBgColorFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new ColorTernary(resolvethBoolean(st.condition), resolvethFillBgColor(st.ifTrue), resolvethFillBgColor(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethFillBgColor(getThemeStyle());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public ColorSpec resolvethFillFgColor(FillStyleSpec fss) throws ParseStyleException, SQLException
	{
		if (fss instanceof FillStyleDef) {
			return resolvethColor(((FillStyleDef)fss).fgColor);
		} else
		if (fss instanceof FillStyleOf) {
			return resolvethFillFgColor(((FillStyleOf)fss).style);
		} else
		if (fss instanceof FillStyleTernary) {
			FillStyleTernary fst = (FillStyleTernary) fss;
			return new ColorTernary(resolvethBoolean(fst.condition), resolvethFillFgColor(fst.ifTrue), resolvethFillFgColor(fst.ifFalse));
		} else
		if (fss instanceof NullFillStyle) {
			return new ConstColor(0);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	private ColorSpec resolvethFillFgColor(StyleSpec styleSpec) throws ParseStyleException, SQLException
	{
		if (styleSpec instanceof NullStyle) {
			return new ConstColor(0);
		} else
		if (styleSpec instanceof LayersStyle) {
			LayersStyle ls = (LayersStyle) styleSpec;
			StyleSpec inner = getInner(ls.tablePath);
			return resolvethFillFgColor(inner);
		} else
		if (styleSpec instanceof StyleDef) {
			return resolvethFillFgColor(((StyleDef)styleSpec).fill);
		} else
		if (styleSpec instanceof StyleField) {
			return new FillFgColorFromField(((StyleField)styleSpec).fieldPath);
		} else
		if (styleSpec instanceof StyleTernary) {
			StyleTernary st = (StyleTernary) styleSpec;
			return new ColorTernary(resolvethBoolean(st.condition), resolvethFillFgColor(st.ifTrue), resolvethFillFgColor(st.ifFalse));
		} else
		if (styleSpec instanceof ThemeStyle) {
			return resolvethFillFgColor(getThemeStyle());
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	StyleSpec getThemeStyle() throws ParseStyleException
	{
		if (themeStyle != null)
			return themeStyle;
		
		if (themeTableStyle == null)
			throw new ParseStyleException(new String[] { "Ni teme" });
		
		String ss = themeTableStyle.trim();

		if (ss.length() < 1 || ss.equals("$this") || ss.equals("null"))
			return tableStyles.get(firstTablePath);
		try {
			StyleSpec themeStyle = StyleCodec.decode(firstTable, ss, meta);
			return this.themeStyle = themeStyle;
		} catch (Exception e) {
		}
				
		return tableStyles.get(firstTablePath);
	}
	
	public static class PathPrepender extends ReflectiveStyleVisitor
	{
		TablePath toPrepend;
		public PathPrepender(TablePath tp)
		{
			toPrepend = tp;
		}
		
		public boolean visit(StyleField sf, boolean entering)
		{
			if (entering)
				sf.fieldPath = TableUtils.combine(toPrepend, sf.fieldPath);

			return true;
		}
		
		public boolean visit(BooleanField bf, boolean entering)
		{
			if (entering)
				bf.fieldPath = TableUtils.combine(toPrepend, bf.fieldPath);
			
			return true;
		}
		
		public boolean visit(DateField bf, boolean entering)
		{
			if (entering)
				bf.fieldPath = TableUtils.combine(toPrepend, bf.fieldPath);
			
			return true;
		}

		public boolean visit(NumberField bf, boolean entering)
		{
			if (entering)
				bf.fieldPath = TableUtils.combine(toPrepend, bf.fieldPath);
			
			return true;
		}

		public boolean visit(StringField bf, boolean entering)
		{
			if (entering)
				bf.fieldPath = TableUtils.combine(toPrepend, bf.fieldPath);
			
			return true;
		}

		public boolean visit(LayersStyle ls, boolean entering)
		{
			if (entering)
				ls.tablePath = TableUtils.combine(toPrepend, ls.tablePath);
			
			return true;
		}
		
		public boolean visit(LayersTextRep ls, boolean entering)
		{
			if (entering)
				ls.tablePath = TableUtils.combine(toPrepend, ls.tablePath);
			
			return true;
		}
		
		public boolean visit(ThemeStyle ts, boolean entering)
		{
			throw new IllegalStateException();
		}
	}
}
