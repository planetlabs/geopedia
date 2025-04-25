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
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.style.ConstStyler;
import com.sinergise.geopedia.style.ParseStyleException;
import com.sinergise.geopedia.style.processor.eval.StringEval;

public final class TextRepEvaluator implements Evaluator
{
	public boolean[] boolVals;
	private int[] boolValIdxs;
	public String[] stringVals;
	private int[] stringValIdxs;
	public long[] longVals;
	private int[] longValIdxs;
	public double[] doubleVals;
	private int[] doubleValIdxs;
	public BigDecimal[] bigDecimalVals;
	private int[] bigDecimalIdxs;
	public long[] dateVals;
	private int[] dateIdxs;

	Object2IntOpenHashMap<FieldPath> fieldIdxs;
	ResultSet resultSet;

	ArrayList<FieldPath> boolPaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> stringPaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> longPaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> doublePaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> bigDecimalPaths = new ArrayList<FieldPath>();
	ArrayList<FieldPath> datePaths = new ArrayList<FieldPath>();

	final private StringEval value;

	final Table table;

	public TextRepEvaluator(StringSpec ss, Table table, MetaData meta) throws ParseStyleException, SQLException
	{
		this.table = table;

		TextRepFlattener f = new TextRepFlattener(ss, table, meta);
		f.go();

		value = StringEval.Factory.create(f.textRep, this);
	}

	public void bind(Object2IntOpenHashMap<FieldPath> fieldIdxs, ResultSet rs)
	{
		this.fieldIdxs = fieldIdxs;
		this.resultSet = rs;

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
		for (int a = 0; a < out.length; a++) {
			out[a] = fieldIdxs.getInt(fields.get(a));
		}
		return out;
	}

	public void preprocessRow() throws SQLException
	{
		ResultSet rs = this.resultSet;

		for (int a = 0; a < boolValIdxs.length; a++)
			boolVals[a] = rs.getInt(boolValIdxs[a]) != 0;

		for (int a = 0; a < stringValIdxs.length; a++) {
			stringVals[a] = rs.getString(stringValIdxs[a]);
			if (stringVals[a] == null)
				stringVals[a] = "";
		}

		for (int a = 0; a < doubleValIdxs.length; a++)
			doubleVals[a] = rs.getDouble(doubleValIdxs[a]);

		for (int a = 0; a < bigDecimalIdxs.length; a++) {
			bigDecimalVals[a] = rs.getBigDecimal(bigDecimalIdxs[a]);
			if (bigDecimalVals[a] == null)
				bigDecimalVals[a] = BigDecimal.ZERO;
		}

		for (int a = 0; a < dateVals.length; a++) {
			Timestamp ts = rs.getTimestamp(dateIdxs[a]);
			dateVals[a] = ts == null ? 0 : ts.getTime();
		}

		for (int a = 0; a < longValIdxs.length; a++)
			longVals[a] = rs.getLong(longValIdxs[a]);
	}

	private int find(ArrayList<FieldPath> paths, FieldPath path)
	{
		int s = paths.size();
		for (int a = 0; a < s; a++)
			if (paths.get(a).equals(path))
				return a;
		paths.add(path);
		return s;
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

	public String getText()
	{
		return value.eval();
	}

	public void getNeededFields(final HashSet<FieldPath> needFields)
	{
		needFields.addAll(boolPaths);
		needFields.addAll(stringPaths);
		needFields.addAll(longPaths);
		needFields.addAll(doublePaths);
		needFields.addAll(bigDecimalPaths);
		needFields.addAll(datePaths);
	}

	public boolean isConst()
	{
		return value.isConst();
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
		throw new IllegalStateException();
	}

	public long valueLong(int idx)
	{
		return longVals[idx];
	}

	public String valueString(int idx)
	{
		return stringVals[idx];
	}

	public int getFieldStyleIdx(FieldPath field)
	{
		throw new IllegalStateException();
	}

	public int getScale()
	{
		throw new IllegalStateException();
	}
}
