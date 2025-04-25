package com.sinergise.geopedia.style.processor;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;

import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.core.style.model.FontId;
import com.sinergise.geopedia.core.style.model.LineType;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.model.SymbolId;
import com.sinergise.geopedia.core.style.nulls.NullStyle;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.style.ConstStyler;
import com.sinergise.geopedia.style.ParseStyleException;
import com.sinergise.geopedia.style.StyleCodec;
import com.sinergise.geopedia.style.Styler;
import com.sinergise.geopedia.style.processor.eval.BooleanEval;
import com.sinergise.geopedia.style.processor.eval.ColorEval;
import com.sinergise.geopedia.style.processor.eval.DoubleEval;
import com.sinergise.geopedia.style.processor.eval.FillTypeEval;
import com.sinergise.geopedia.style.processor.eval.FontIdEval;
import com.sinergise.geopedia.style.processor.eval.LineTypeEval;
import com.sinergise.geopedia.style.processor.eval.LongEval;
import com.sinergise.geopedia.style.processor.eval.StringEval;
import com.sinergise.geopedia.style.processor.eval.SymbolIdEval;

public final class StyleEvaluator extends Styler implements Evaluator
{
	public ConstStyler[] fieldStyles; private int[] fieldStyleIdxs;
	public boolean[] boolVals; private int[] boolValIdxs;
	public String[] stringVals; private int[] stringValIdxs;
	public long[] longVals; private int[] longValIdxs;
	public double[] doubleVals; private int[] doubleValIdxs;
	public BigDecimal[] bigDecimalVals; private int[] bigDecimalIdxs;
	public long[] dateVals; private int[] dateIdxs;

	Object2IntOpenHashMap<FieldPath> fieldIdxs;
	ResultSet resultSet;
	
	ArrayList<FieldPath> stylePaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> boolPaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> stringPaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> longPaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> doublePaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> bigDecimalPaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> datePaths = new ArrayList<FieldPath>();
	
	final private ColorEval backFillColor;
	final private ColorEval foreFillColor;
	final private FillTypeEval fillType;

	final private ColorEval lineColor;
	final private LineTypeEval lineType;
	final private DoubleEval lineWidth;

	final private ColorEval symbolColor;
	final private SymbolIdEval symbolId;
	final private LongEval symbolSize;
	final private StringEval symbolText;

	final private ColorEval textColor;
	final private BooleanEval bold;
	final private BooleanEval italic;
	final private FontIdEval fontId;
	final private LongEval fontSize;
	
	public final int scale;
	
	final Table table;
	private MetaData meta;
	public StyleEvaluator(StyleSpec ss, Table table, String themeTableStyle, int scale, MetaData meta) throws ParseStyleException, SQLException
	{
		this.meta=meta;
		this.table = table;
		this.scale = scale;
		
		StyleFlattener f = new StyleFlattener(ss, table, themeTableStyle, scale, meta);
		f.go();
		
		backFillColor = ColorEval.   Factory.create(f.fillBgColor, this);
		foreFillColor = ColorEval.   Factory.create(f.fillFgColor, this);
		fillType      = FillTypeEval.Factory.create(f.fillType, this);
		lineColor     = ColorEval.   Factory.create(f.lineColor, this);
		lineType      = LineTypeEval.Factory.create(f.lineType, this);
		lineWidth     = DoubleEval.  Factory.create(f.lineWidth, this);
		symbolColor   = ColorEval.   Factory.create(f.symbolColor, this);
		symbolId      = SymbolIdEval.Factory.create(f.symbolId, this);
		symbolSize    = LongEval.    Factory.create(f.symbolSize, this);
		symbolText    = StringEval.  Factory.create(f.symbolText, this);
		textColor     = ColorEval.   Factory.create(f.textColor, this);
		bold          = BooleanEval. Factory.create(f.bold, this);
		italic        = BooleanEval. Factory.create(f.italic, this);
		fontId        = FontIdEval.  Factory.create(f.textFont, this);
		fontSize      = LongEval.    Factory.create(f.fontSize, this);
	}
	
	public void bind(Object2IntOpenHashMap<FieldPath> fieldIdxs, ResultSet rs)
	{
		this.fieldIdxs = fieldIdxs;
		this.resultSet = rs;
		
		fieldStyleIdxs = getEm(stylePaths); 
		fieldStyles = new ConstStyler[stylePaths.size()];
		for (int i = 0; i < fieldStyles.length; i++) {
			fieldStyles[i] = new ConstStyler(scale);
		}
		
		boolValIdxs = getEm(boolPaths); 
		boolVals = new boolean[boolPaths.size()];
		
		stringValIdxs = getEm(stringPaths); 
		stringVals = new String[stringPaths.size()];
		
		longValIdxs = getEm(longPaths); 
		longVals = new long[longPaths.size()];
		
		doubleValIdxs = getEm(doublePaths); 
		doubleVals = new double[doublePaths.size()];
		
		bigDecimalIdxs = getEm(bigDecimalPaths); 
		bigDecimalVals = new BigDecimal[bigDecimalPaths.size()];
		
		dateIdxs = getEm(datePaths); 
		dateVals = new long[datePaths.size()];
	}
	
	private int[] getEm(ArrayList<FieldPath> fields)
	{
		int[] out = new int[fields.size()];
		for (int a=0; a<out.length; a++) {
			out[a] = fieldIdxs.getInt(fields.get(a));
		}
		return out;
	}
	
	public void preprocessRow() throws SQLException
    {
		ResultSet rs = this.resultSet;
		
		for (int a=0; a < fieldStyleIdxs.length; a++) {
			StyleSpec ss;
            try {
	            ss = StyleCodec.decode(table, rs.getString(fieldStyleIdxs[a]), meta);
            } catch (ParseStyleException e) {
            	ss = NullStyle.instance;
            }
			
            try {
            	fieldStyles[a].processStyle(ss);
            } catch (UnsupportedOperationException e) {
            	System.out.println(rs.getString(fieldStyleIdxs[a]));
            	throw e;
            }
		}
		
		for (int a=0; a < boolValIdxs.length; a++)
			boolVals[a] = rs.getInt(boolValIdxs[a]) != 0;
		
		for (int a=0; a < stringValIdxs.length; a++) {
			stringVals[a] = rs.getString(stringValIdxs[a]);
			if (stringVals[a] == null)
				stringVals[a] = "";
		}
		
		for (int a=0; a < doubleValIdxs.length; a++)
			doubleVals[a] = rs.getDouble(doubleValIdxs[a]);
		
		for (int a=0; a < bigDecimalIdxs.length; a++) {
			bigDecimalVals[a] = rs.getBigDecimal(bigDecimalIdxs[a]);
			if (bigDecimalVals[a] == null)
				bigDecimalVals[a] = BigDecimal.ZERO;
		}
		
		for (int a=0; a < dateVals.length; a++) {
			Timestamp ts = rs.getTimestamp(dateIdxs[a]);
			dateVals[a] = ts == null ? 0 : ts.getTime();
		}
		
		for (int a = 0; a < longValIdxs.length; a++)
			longVals[a] = rs.getLong(longValIdxs[a]);
    }
	
	private int find(ArrayList<FieldPath> paths, FieldPath path)
	{
		int s = paths.size();
		for (int a=0; a<s; a++)
			if (paths.get(a).equals(path))
				return a;
		paths.add(path);
		return s;
	}
	
	public int getFieldStyleIdx(FieldPath field)
    {
		return find(stylePaths, field);
    }
	
	public int getBooleanFieldIdx(FieldPath field)
	{
		return find(boolPaths, field);
	}

	public int getStringFieldIdx(FieldPath fieldPath)
    {
		return find(stringPaths, fieldPath);
    }

	public int getLongFieldIdx(FieldPath fieldPath)
    {
		return find(longPaths, fieldPath);
    }

	public int getDoubleFieldIdx(FieldPath fieldPath)
    {
		return find(doublePaths, fieldPath);
    }

	public int getBigDecimalFieldIdx(FieldPath fieldPath)
    {
		return find(bigDecimalPaths, fieldPath);
    }

	public int getDateFieldIdx(FieldPath fieldPath)
    {
		return find(datePaths, fieldPath);
    }

	public int getBackFillColor()
    {
		return backFillColor.eval();
    }

	public boolean getBold()
    {
		return bold.eval();
    }

	public int getFillType()
    {
		return fillType.eval();
    }

	public int getFontID()
    {
		return fontId.eval();
    }

	public int getFontSize()
    {
		long l = fontSize.eval();
		if (l < FontId.MIN_SIZE)
			return FontId.MIN_SIZE;
		if (l > FontId.MAX_SIZE)
			return FontId.MAX_SIZE;
		return (int)l;
    }

	public int getForeFillColor()
    {
		return foreFillColor.eval();
    }

	public boolean getItalic()
    {
		return italic.eval();
    }

	public int getLineColor()
    {
		return lineColor.eval();
    }

	public int getLineType()
    {
		return lineType.eval();
    }

	public double getLineWidth()
    {
		double l = lineWidth.eval();
		if (l < LineType.MIN_WIDTH)
			return LineType.MIN_WIDTH;
		if (l > LineType.MAX_WIDTH)
			return LineType.MAX_WIDTH;
		return l;
    }

	public int getSymbolColor()
    {
		return symbolColor.eval();
    }

	public int getSymbolId()
    {
		return symbolId.eval();
    }

	public int getSymbolSize()
    {
		long l = symbolSize.eval();
		if (l < SymbolId.MIN_SIZE)
			return SymbolId.MIN_SIZE;
		if (l > SymbolId.MAX_SIZE)
			return SymbolId.MAX_SIZE;
		return (int)l;
    }

	public String getSymbolText()
    {
		return symbolText.eval();
    }

	public int getTextColor()
    {
		return textColor.eval();
    }

	public void getNeededFields(final HashSet<FieldPath> needFields)
    {
		needFields.addAll(stylePaths);
		needFields.addAll(boolPaths);
		needFields.addAll(stringPaths);
		needFields.addAll(longPaths);
		needFields.addAll(doublePaths);
		needFields.addAll(bigDecimalPaths);
		needFields.addAll(datePaths);
    }
	
	public int getConstParts()
	{
		int result = 0;

		if (backFillColor.isConst()) result |= CONST_FILL_BGCOLOR;
		if (foreFillColor.isConst()) result |= CONST_FILL_FGCOLOR;
		if (fillType.isConst()) result |= CONST_FILL_TYPE;
		if (lineColor.isConst()) result |= CONST_LINE_COLOR;
		if (lineType.isConst()) result |= CONST_LINE_TYPE;
		if (lineWidth.isConst()) result |= CONST_LINE_WIDTH;
		if (symbolColor.isConst()) result |= CONST_SYM_COLOR;
		if (symbolId.isConst()) result |= CONST_SYM_ID;
		if (symbolSize.isConst()) result |= CONST_SYM_SIZE;
		if (symbolText.isConst()) result |= CONST_SYM_TEXT;
		if (textColor.isConst()) result |= CONST_FONT_COLOR;
		if (bold.isConst()) result |= CONST_FONT_BOLD;
		if (italic.isConst()) result |= CONST_FONT_ITALIC;
		if (fontId.isConst()) result |= CONST_FONT_ID;
		if (fontSize.isConst()) result |= CONST_FONT_HEIGHT;
		
		return result;
	}
	
	public int getScale()
	{
		return scale;
	}
	
	public BigDecimal valueBigDecimal(int idx)
	{
		return bigDecimalVals[idx];
	}
	
	public boolean valueBool(int idx)
	{
		return boolVals[idx];
	}
	
	public long valueDate(int idx)
	{
		return dateVals[idx];
	}
	
	public double valueDouble(int idx)
	{
		return doubleVals[idx];
	}
	
	public ConstStyler valueFieldStyles(int fieldIdx)
	{
		return fieldStyles[fieldIdx];
	}
	
	public long valueLong(int idx)
	{
		return longVals[idx];
	}
	
	public String valueString(int idx)
	{
		return stringVals[idx];
	}
}
