package com.sinergise.geopedia.style;

import java.util.ArrayList;

import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.bools.AndOrXor;
import com.sinergise.geopedia.core.style.bools.Not;
import com.sinergise.geopedia.core.style.consts.ConstBool;
import com.sinergise.geopedia.core.style.consts.ConstDouble;
import com.sinergise.geopedia.core.style.consts.ConstLong;
import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.fields.BooleanField;
import com.sinergise.geopedia.core.style.fields.NumberField;
import com.sinergise.geopedia.core.style.fields.StringField;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.nulls.NullStyle;
import com.sinergise.geopedia.core.style.numbers.CompareNum;
import com.sinergise.geopedia.core.style.numbers.CurrentScale;
import com.sinergise.geopedia.core.style.numbers.NamedConstant;
import com.sinergise.geopedia.core.style.numbers.NumBinaryOp;
import com.sinergise.geopedia.core.style.numbers.NumUnaryOp;
import com.sinergise.geopedia.core.style.numbers.OneParamFunc;
import com.sinergise.geopedia.core.style.numbers.TwoParamFunc;
import com.sinergise.geopedia.core.style.strings.CompareString;
import com.sinergise.geopedia.core.style.strings.StringConcat;
import com.sinergise.geopedia.core.style.strings.StringFromBoolean;
import com.sinergise.geopedia.core.style.strings.StringFromNumber;
import com.sinergise.geopedia.core.style.ternaries.BoolTernary;
import com.sinergise.geopedia.core.style.ternaries.NumericTernary;
import com.sinergise.geopedia.core.style.ternaries.StringTernary;
import com.sinergise.geopedia.core.style.ternaries.StyleTernary;
import com.sinergise.geopedia.db.QueryPreResult;
import com.sinergise.geopedia.expr.SymUtil;

public class SqlIzer
{
	public static StyleSpec sqlIze(StyleSpec ss, StringBuilder sql, boolean firstWhere, QueryPreResult qpr, ArrayList<Object> sqlValues)
	{
		if (!(ss instanceof StyleTernary))
			return ss;
		
		StyleTernary st = (StyleTernary) ss;
		
		BooleanSpec bs;
		StyleSpec out;
		
		if (st.ifTrue instanceof NullStyle) {
			if (able(st.condition)) {
				bs = new Not(st.condition);
				out = st.ifFalse;
			} else
				return ss;
		} else
		if (st.ifFalse instanceof NullStyle) {
			if (able(st.condition)) {
				bs = st.condition;
				out = st.ifTrue;
			} else
				return ss;
		} else
			return ss;
		
		if (firstWhere) {
			sql.append(" WHERE (");
		} else {
			sql.append(" AND (");
		}
		
		toSql(bs, sql, qpr, sqlValues);
		
		sql.append(')');
		
		return sqlIze(out, sql, false, qpr, sqlValues);
	}
	
	private static void toSql(BooleanSpec bs, StringBuilder sql, QueryPreResult qpr, ArrayList<Object> sqlValues)
    {
		if (bs instanceof AndOrXor) {
			AndOrXor aox = (AndOrXor) bs;
			
			sql.append('(');
			toSql(aox.left, sql, qpr, sqlValues);
			if (aox.type == Sym.CAR) {
				sql.append(") XOR (");
			} else
			if (aox.type == Sym.BAR || aox.type == Sym.BARBAR) {
				sql.append(") OR (");
			} else {
				sql.append(") AND (");
			}
			toSql(aox.right, sql, qpr, sqlValues);
			sql.append(')');
		} else
		if (bs instanceof BooleanField) {
			sql.append(qpr.getFieldName(((BooleanField)bs).fieldPath));
		} else
		if (bs instanceof BoolTernary) {
			BoolTernary bt = (BoolTernary) bs;
			
			sql.append("IF(");
			toSql(bt.condition, sql, qpr, sqlValues);
			sql.append(", ");
			toSql(bt.ifTrue, sql, qpr, sqlValues);
			sql.append(", ");
			toSql(bt.ifFalse, sql, qpr, sqlValues);
			sql.append(')');
		} else
		if (bs instanceof CompareNum) {
			CompareNum cn = (CompareNum) bs;

			sql.append('(');
			toSql(cn.left, sql, qpr, sqlValues);
			sql.append(") ");
			sql.append(SymUtil.toSqlString(cn.op));
			sql.append(" (");
			toSql(cn.right, sql, qpr, sqlValues);
			sql.append(')');
		} else
		if (bs instanceof CompareString) {
			CompareString cs = (CompareString) bs;

			sql.append('(');
			toSql(cs.left, sql, qpr, sqlValues);
			sql.append(") ");
			sql.append(SymUtil.toSqlString(cs.op));
			sql.append(" (");
			toSql(cs.right, sql, qpr, sqlValues);
			sql.append(')');		
		} else
		if (bs instanceof ConstBool) {
			throw new IllegalStateException();
		} else
		if (bs instanceof Not) {
			sql.append("NOT (");
			toSql(((Not)bs).base, sql, qpr, sqlValues);
			sql.append(')');
		} else
			throw new IllegalStateException();
    }

	static boolean able(BooleanSpec bs)
	{
		if (bs instanceof AndOrXor) {
			AndOrXor aox = (AndOrXor) bs;
			return able(aox.left) && able(aox.right);
		} else
		if (bs instanceof BooleanField) {
			return true;
		} else
		if (bs instanceof BoolTernary) {
			BoolTernary bt = (BoolTernary) bs;
			return able(bt.condition) && able(bt.ifTrue) && able(bt.ifFalse);
		} else
		if (bs instanceof CompareNum) {
			CompareNum cn = (CompareNum) bs;
			return able(cn.left) && able(cn.right);
		} else
		if (bs instanceof CompareString) {
			CompareString cs = (CompareString) bs;
			return able(cs.left) && able(cs.right);
		} else
		if (bs instanceof ConstBool) {
			throw new IllegalStateException();
		} else
		if (bs instanceof Not) {
			return able(((Not)bs).base);
		}
		
		// TODO: check datespecs
		
		return false;
	}
	
	private static void toSql(NumberSpec ns, StringBuilder sql, QueryPreResult qpr, ArrayList<Object> sqlValues)
	{
		if (ns instanceof ConstDouble) {
			sql.append('?');
			sqlValues.add(new Double(((ConstDouble)ns).value));
		} else
		if (ns instanceof ConstLong) {
			sql.append('?');
			sqlValues.add(new Long(((ConstLong)ns).value));
		} else
		if (ns instanceof CurrentScale) {
			throw new IllegalStateException();
		} else
		if (ns instanceof NamedConstant) {
			sql.append("PI()");
		} else
		if (ns instanceof NumberField) {
			sql.append(qpr.getFieldName(((NumberField)ns).fieldPath));
		} else
		if (ns instanceof NumBinaryOp) {
			NumBinaryOp nbo = (NumBinaryOp) ns;
			
			switch(nbo.op) {
			case Sym.PERCENT:
			case Sym.MINUS:
			case Sym.PLUS:
			case Sym.SLASH:
			case Sym.STAR:
				break;
			default:
				throw new IllegalStateException();
			}
			
			sql.append('(');
			toSql(nbo.left, sql, qpr, sqlValues);
			sql.append(") ");
			sql.append(SymUtil.toSqlString(nbo.op));
			sql.append(" (");
			toSql(nbo.right, sql, qpr, sqlValues);
			sql.append(')');	
		} else
		if (ns instanceof NumericTernary) {
			NumericTernary nt = (NumericTernary) ns;
			
			sql.append("IF(");
			toSql(nt.condition, sql, qpr, sqlValues);
			sql.append(", ");
			toSql(nt.ifTrue, sql, qpr, sqlValues);
			sql.append(", ");
			toSql(nt.ifFalse, sql, qpr, sqlValues);
			sql.append(')');
		} else
		if (ns instanceof NumUnaryOp) {
			NumUnaryOp nuo = (NumUnaryOp) ns;
			if (nuo.op == Sym.MINUS) {
				sql.append("-(");
				toSql(nuo.base, sql, qpr, sqlValues);
				sql.append(')');
			} else
				throw new IllegalStateException();
		} else
		if (ns instanceof OneParamFunc) {
			OneParamFunc opf = (OneParamFunc) ns;
			switch(opf.func) {
			case OneParamFunc.FUNC_ABS: sql.append("ABS("); break;
			case OneParamFunc.FUNC_ACOS: sql.append("ACOS("); break;
			case OneParamFunc.FUNC_ASIN: sql.append("ASIN("); break;
			case OneParamFunc.FUNC_ATAN: sql.append("ATAN("); break;
			case OneParamFunc.FUNC_CEIL: sql.append("CEIL("); break;
			case OneParamFunc.FUNC_COS: sql.append("COS("); break;
			case OneParamFunc.FUNC_EXP: sql.append("EXP("); break;
			case OneParamFunc.FUNC_FLOOR: sql.append("FLOOR("); break;
			case OneParamFunc.FUNC_LOG: sql.append("LOG("); break;
			case OneParamFunc.FUNC_LOG10: sql.append("LOG10("); break;
			case OneParamFunc.FUNC_LOG2: sql.append("LOG2("); break;
			case OneParamFunc.FUNC_ROUND: sql.append("ROUND("); break;
			case OneParamFunc.FUNC_SIN: sql.append("SIN("); break;
			case OneParamFunc.FUNC_SQRT: sql.append("SQRT("); break;
			case OneParamFunc.FUNC_TAN: sql.append("TAN("); break;
			case OneParamFunc.FUNC_SIGNUM: sql.append("SIGN("); break;
			case OneParamFunc.FUNC_EXP10: sql.append("POW(10, "); break;
			
			case OneParamFunc.FUNC_RAD2DEG: sql.append((180 / Math.PI)+" * ("); break;
			case OneParamFunc.FUNC_RAD2GRAD: sql.append((200 / Math.PI)+" * ("); break;
			case OneParamFunc.FUNC_GRAD2DEG: sql.append((360.0 / 400)+" * ("); break;
			case OneParamFunc.FUNC_GRAD2RAD: sql.append((Math.PI / 200)+" * ("); break;
			case OneParamFunc.FUNC_DEG2RAD: sql.append((Math.PI / 180)+" * ("); break;
			case OneParamFunc.FUNC_DEG2GRAD: sql.append((400.0 / 360)+" * ("); break;
			default:
				throw new IllegalStateException();
			}
			
			toSql(opf.arg, sql, qpr, sqlValues);
			
			sql.append(')');
		} else
		if (ns instanceof TwoParamFunc) {
			TwoParamFunc tpf = (TwoParamFunc) ns;
			switch(tpf.func) {
			case TwoParamFunc.FUNC_ATAN:
				sql.append("ATAN2("); break;
			case TwoParamFunc.FUNC_POW:
				sql.append("POW("); break;
			default:
				throw new IllegalStateException();
			}
			
			toSql(tpf.arg0, sql, qpr, sqlValues);
			
			sql.append(", ");
			
			toSql(tpf.arg1, sql, qpr, sqlValues);
			
			sql.append(')');
		} else
			throw new IllegalStateException();
	}
	
	static boolean able(NumberSpec ns)
	{
		if (ns instanceof ConstDouble) {
			return true;
		} else
		if (ns instanceof ConstLong) {
			return true;
		} else
		if (ns instanceof CurrentScale) {
			throw new IllegalStateException();
		} else
		if (ns instanceof NamedConstant) {
			return true;
		} else
		if (ns instanceof NumberField) {
			return true;
		} else
		if (ns instanceof NumBinaryOp) {
			NumBinaryOp nbo = (NumBinaryOp) ns;
			
			switch(nbo.op) {
			case Sym.PERCENT:
			case Sym.MINUS:
			case Sym.PLUS:
			case Sym.SLASH:
			case Sym.STAR:
				break;
			default:
				return false;
			}
			
			return able(nbo.left) && able(nbo.right);
		} else
		if (ns instanceof NumericTernary) {
			NumericTernary nt = (NumericTernary) ns;
			return able(nt.condition) && able(nt.ifTrue) && able(nt.ifFalse);
		} else
		if (ns instanceof NumUnaryOp) {
			NumUnaryOp nuo = (NumUnaryOp) ns;
			return nuo.op == Sym.MINUS && able(nuo.base);
		} else
		if (ns instanceof OneParamFunc) {
			OneParamFunc opf = (OneParamFunc) ns;
			switch(opf.func) {
			case OneParamFunc.FUNC_ABS:
			case OneParamFunc.FUNC_ACOS:
			case OneParamFunc.FUNC_ASIN:
			case OneParamFunc.FUNC_ATAN:
			case OneParamFunc.FUNC_CEIL:
			case OneParamFunc.FUNC_COS:
			case OneParamFunc.FUNC_DEG2GRAD:
			case OneParamFunc.FUNC_DEG2RAD:
			case OneParamFunc.FUNC_EXP:
			case OneParamFunc.FUNC_EXP10:
			case OneParamFunc.FUNC_FLOOR:
			case OneParamFunc.FUNC_GRAD2DEG:
			case OneParamFunc.FUNC_GRAD2RAD:
			case OneParamFunc.FUNC_LOG:
			case OneParamFunc.FUNC_LOG10:
			case OneParamFunc.FUNC_LOG2:
			case OneParamFunc.FUNC_RAD2DEG:
			case OneParamFunc.FUNC_RAD2GRAD:
			case OneParamFunc.FUNC_ROUND:
			case OneParamFunc.FUNC_SIGNUM:
			case OneParamFunc.FUNC_SIN:
			case OneParamFunc.FUNC_SQRT:
			case OneParamFunc.FUNC_TAN:
				return able(opf.arg);
			default:
				throw new IllegalStateException();
			}
		} else
		if (ns instanceof TwoParamFunc) {
			TwoParamFunc tpf = (TwoParamFunc) ns;
			switch(tpf.func) {
			case TwoParamFunc.FUNC_ATAN:
			case TwoParamFunc.FUNC_POW:
				return able(tpf.arg0) && able(tpf.arg1);
			default:
				throw new IllegalStateException();
			}
		}
		
		return false;
	}
	
	private static void toSql(StringSpec ss, StringBuilder sql, QueryPreResult qpr, ArrayList<Object> sqlValues)
	{
		if (ss instanceof ConstString) {
			sql.append('?');
			sqlValues.add(((ConstString)ss).value);
		} else
		if (ss instanceof StringConcat) {
			StringConcat sc = (StringConcat) ss;
			
			sql.append("CONCAT(");
			toSql(sc.left, sql, qpr, sqlValues);
			sql.append(", ");
			toSql(sc.right, sql, qpr, sqlValues);
			sql.append(')');
		} else
		if (ss instanceof StringField) {
			sql.append(qpr.getFieldName(((StringField)ss).fieldPath));
		} else
		if (ss instanceof StringFromBoolean) {
			sql.append("IF(");
			toSql(((StringFromBoolean)ss).val, sql, qpr, sqlValues);
			sql.append(", \"true\", \"false\")");
		} else
		if (ss instanceof StringFromNumber) {
			sql.append("CAST(");
			toSql(((StringFromNumber)ss).val, sql, qpr, sqlValues);
			sql.append(" AS CHAR)");
		} else
		if (ss instanceof StringTernary) {
			StringTernary st = (StringTernary) ss;
			
			sql.append("IF(");
			toSql(st.condition, sql, qpr, sqlValues);
			sql.append(", ");
			toSql(st.ifTrue, sql, qpr, sqlValues);
			sql.append(", ");
			toSql(st.ifFalse, sql, qpr, sqlValues);
			sql.append(')');
		} else
			throw new IllegalStateException();
	}
	
	static boolean able(StringSpec ss)
	{
		if (ss instanceof ConstString) {
			return true;
		} else
		if (ss instanceof StringConcat) {
			StringConcat sc = (StringConcat) ss;
			return able(sc.left) && able(sc.right);
		} else
		if (ss instanceof StringField) {
			return true;
		} else
		if (ss instanceof StringFromBoolean) {
			return able(((StringFromBoolean)ss).val);
		} else
		if (ss instanceof StringFromNumber) {
			return able(((StringFromNumber)ss).val);
		} else
		if (ss instanceof StringTernary) {
			StringTernary st = (StringTernary) ss;
			return able(st.condition) && able(st.ifTrue) && able(st.ifFalse);
		}
		
		return false;
	}
}
