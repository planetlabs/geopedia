package com.sinergise.geopedia.style;

import java.math.BigDecimal;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.style.processor.Evaluator;

public class DummyEvaluator implements Evaluator
{
	int scale;
	
	public static DummyEvaluator[] forScale = {
		new DummyEvaluator(0),
		new DummyEvaluator(1),
		new DummyEvaluator(2),
		new DummyEvaluator(3),
		new DummyEvaluator(4),
		new DummyEvaluator(5),
		new DummyEvaluator(6),
		new DummyEvaluator(7),
		new DummyEvaluator(8),
		new DummyEvaluator(9),
		new DummyEvaluator(10),
		new DummyEvaluator(11),
		new DummyEvaluator(12),
		new DummyEvaluator(13),
		new DummyEvaluator(14),
		new DummyEvaluator(15),
		new DummyEvaluator(16),
		new DummyEvaluator(17),
		new DummyEvaluator(18),
		new DummyEvaluator(19),
        new DummyEvaluator(20),
        new DummyEvaluator(21)
	};
	
	public DummyEvaluator(int scale)
	{
		this.scale = scale;
	}
	
	public int getScale()
    {
		return scale;
    }

	public int getBigDecimalFieldIdx(FieldPath fieldPath)
	{
		throw new IllegalStateException();
    }

	public int getBooleanFieldIdx(FieldPath fieldPath)
    {
		throw new IllegalStateException();
    }

	public int getDateFieldIdx(FieldPath fieldPath)
    {
		throw new IllegalStateException();
    }

	public int getDoubleFieldIdx(FieldPath fieldPath)
    {
		throw new IllegalStateException();
    }

	public int getFieldStyleIdx(FieldPath field)
    {
		throw new IllegalStateException();
    }

	public int getLongFieldIdx(FieldPath fieldPath)
    {
		throw new IllegalStateException();
    }

	public int getStringFieldIdx(FieldPath fieldPath)
    {
		throw new IllegalStateException();
    }

	public BigDecimal valueBigDecimal(int idx)
    {
		throw new IllegalStateException();
    }

	public boolean valueBool(int idx)
    {
		throw new IllegalStateException();
    }

	public long valueDate(int fidx)
    {
		throw new IllegalStateException();
    }

	public double valueDouble(int idx)
    {
		throw new IllegalStateException();
    }

	public ConstStyler valueFieldStyles(int fieldIdx)
    {
		throw new IllegalStateException();
    }

	public long valueLong(int idx)
    {
		throw new IllegalStateException();
    }

	public String valueString(int idx)
    {
		throw new IllegalStateException();
    }
}