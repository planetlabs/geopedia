package com.sinergise.geopedia.style.processor;

import java.sql.SQLException;
import java.util.HashMap;

import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.walk.TablePath;
import com.sinergise.geopedia.core.style.bools.AndOrXor;
import com.sinergise.geopedia.core.style.bools.Not;
import com.sinergise.geopedia.core.style.consts.ConstBool;
import com.sinergise.geopedia.core.style.consts.ConstDouble;
import com.sinergise.geopedia.core.style.consts.ConstLong;
import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.dates.DateBoolProp;
import com.sinergise.geopedia.core.style.dates.DateNumberProp;
import com.sinergise.geopedia.core.style.fields.BooleanField;
import com.sinergise.geopedia.core.style.fields.DateField;
import com.sinergise.geopedia.core.style.fields.NumberField;
import com.sinergise.geopedia.core.style.fields.StringField;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.DateSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.numbers.CompareNum;
import com.sinergise.geopedia.core.style.numbers.NParamFunc;
import com.sinergise.geopedia.core.style.numbers.NamedConstant;
import com.sinergise.geopedia.core.style.numbers.NumBinaryOp;
import com.sinergise.geopedia.core.style.numbers.NumUnaryOp;
import com.sinergise.geopedia.core.style.numbers.OneParamFunc;
import com.sinergise.geopedia.core.style.numbers.TwoParamFunc;
import com.sinergise.geopedia.core.style.proxys.LayersTextRep;
import com.sinergise.geopedia.core.style.strings.CompareString;
import com.sinergise.geopedia.core.style.strings.StringConcat;
import com.sinergise.geopedia.core.style.strings.StringFromBoolean;
import com.sinergise.geopedia.core.style.strings.StringFromDate;
import com.sinergise.geopedia.core.style.strings.StringFromNumber;
import com.sinergise.geopedia.core.style.ternaries.BoolTernary;
import com.sinergise.geopedia.core.style.ternaries.DateTernary;
import com.sinergise.geopedia.core.style.ternaries.NumericTernary;
import com.sinergise.geopedia.core.style.ternaries.StringTernary;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.db.entities.TableUtils;
import com.sinergise.geopedia.style.ParseStyleException;
import com.sinergise.geopedia.style.ReflectiveStyleVisitor;

public class TextRepFlattener
{
	StringSpec first;
	Table firstTable;
	
	public StringSpec textRep;
	private MetaData meta;
	HashMap<TablePath, StringSpec> tableTextReps = new HashMap<TablePath, StringSpec>();
	
	public TextRepFlattener(StringSpec ss, Table table, MetaData meta) {
		this.meta=meta;
		first = ss;
		firstTable = table;
	}
	
	public void go() throws ParseStyleException, SQLException
	{
		StringSpec first = (StringSpec) this.first.clone();

		textRep = resolvethString(first);
	}

	private StringSpec getInner(TablePath tp) throws ParseStyleException, SQLException
	{
		StringSpec res = tableTextReps.get(tp);
		if (res != null)
			return res;
		
		res = (StringSpec) TableUtils.getTextRepSpecForTable(tp.lastTableId(), meta).clone();
		res.accept(new PathPrepender(tp));
		
		tableTextReps.put(tp, res);
		return res;
	}
	
	NumberSpec resolvethNumber(NumberSpec ns) throws ParseStyleException, SQLException 
	{
		if (ns instanceof ConstDouble) {
			return ns;
		} else
		if (ns instanceof ConstLong) {
			return ns;
		} else
		if (ns instanceof DateNumberProp) {
			DateNumberProp dnp = (DateNumberProp) ns;
			return new DateNumberProp(dnp.field, resolvethDate(dnp.date));
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
		if (ns instanceof TwoParamFunc) {
			TwoParamFunc tpf = (TwoParamFunc) ns;
			return new TwoParamFunc(tpf.func, resolvethNumber(tpf.arg0), resolvethNumber(tpf.arg1));
		} else {
			throw new UnsupportedOperationException("Unknown number spec class: "+ns.getClass().getName());
		}
	}

	public BooleanSpec resolvethBoolean(BooleanSpec bs) throws ParseStyleException, SQLException
	{
		if (bs instanceof AndOrXor) {
			AndOrXor aox = (AndOrXor) bs;
			return new AndOrXor(resolvethBoolean(aox.left), aox.type, resolvethBoolean(aox.right));
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
		if (bs instanceof Not) {
			return new Not(resolvethBoolean(((Not)bs).base));
		} else {
			throw new UnsupportedOperationException("Unknown bool spec class: "+bs.getClass().getName());
		}
	}
	
	public StringSpec resolvethString(StringSpec ss) throws ParseStyleException, SQLException
	{
		if (ss instanceof LayersTextRep) {
			LayersTextRep ltr = (LayersTextRep) ss;
			StringSpec inner = getInner(ltr.tablePath);
			return resolvethString(inner);
		} else
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
	
	public static class PathPrepender extends ReflectiveStyleVisitor
	{
		TablePath toPrepend;
		public PathPrepender(TablePath tp)
		{
			toPrepend = tp;
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

		public boolean visit(LayersTextRep ls, boolean entering)
		{
			if (entering)
				ls.tablePath = TableUtils.combine(toPrepend, ls.tablePath);
			
			return true;
		}
	}
}
