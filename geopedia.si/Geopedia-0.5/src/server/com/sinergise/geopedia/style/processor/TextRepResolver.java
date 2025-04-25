package com.sinergise.geopedia.style.processor;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;

import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldFlags;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.walk.MetaFieldPath;
import com.sinergise.geopedia.core.entities.walk.TablePath;
import com.sinergise.geopedia.core.entities.walk.UserFieldPath;
import com.sinergise.geopedia.core.style.Sym;
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
import com.sinergise.geopedia.core.style.model.NullSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.model.StyleSpecPart;
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
import com.sinergise.geopedia.db.DBNames;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.db.entities.TableUtils;
import com.sinergise.geopedia.expr.ErrorReporter;
import com.sinergise.geopedia.expr.SymUtil;
import com.sinergise.geopedia.expr.dom.ArrayAccess;
import com.sinergise.geopedia.expr.dom.BasicNumericOp;
import com.sinergise.geopedia.expr.dom.BitwiseOp;
import com.sinergise.geopedia.expr.dom.CompareOp;
import com.sinergise.geopedia.expr.dom.ConditionalOp;
import com.sinergise.geopedia.expr.dom.EqualsOp;
import com.sinergise.geopedia.expr.dom.Expression;
import com.sinergise.geopedia.expr.dom.Expressions;
import com.sinergise.geopedia.expr.dom.Literal;
import com.sinergise.geopedia.expr.dom.MethodCall;
import com.sinergise.geopedia.expr.dom.Null;
import com.sinergise.geopedia.expr.dom.Property;
import com.sinergise.geopedia.expr.dom.ShiftOp;
import com.sinergise.geopedia.expr.dom.Ternary;
import com.sinergise.geopedia.expr.dom.UnaryOp;
import com.sinergise.geopedia.expr.lexer.Lexer;
import com.sinergise.geopedia.expr.lexer.Token;
import com.sinergise.geopedia.expr.lexer.Tokens;
import com.sinergise.geopedia.expr.parser.Parser;

public class TextRepResolver implements ErrorReporter
{
	ArrayList<Pair<String, Token>> errors = null;
	MetaData meta;
	public TextRepResolver(MetaData meta)
	{
		this.meta=meta;
	}

	public void error(String msg, Token token)
	{
		if (errors == null)
			errors = new ArrayList<Pair<String, Token>>();

		errors.add(new Pair<String, Token>(msg, token));
	}

	public boolean hadErrors()
	{
		return errors != null;
	}

	StringSpec result = null;
	Table base;

	public StringSpec getResult()
	{
		return result;
	}

	public void process(String textRepSpec, Table t) throws IOException, SQLException
	{
		process(new StringReader(textRepSpec), t);
	}

	public void process(StringReader reader, Table t) throws IOException, SQLException
	{
		result = null;

		Lexer lexer = new Lexer(reader, this);
		Parser parser = new Parser(new Tokens(lexer), this);
		Expression expr = parser.parseExpression();

		if (!hadErrors()) {
			base = t;

			StyleSpecPart tmp = process(expr);
			if (tmp != null) {
				if (tmp.getType() == StyleSpecPart.T_STRING) {
					result = (StringSpec) tmp;
				} else if (tmp.getType() == StyleSpecPart.T_BOOLEAN) {
					result = new StringFromBoolean((BooleanSpec) tmp);
				} else if (tmp.getType() == StyleSpecPart.T_DATE) {
					result = new StringFromDate((DateSpec) tmp);
				} else if (tmp.getType() == StyleSpecPart.T_NUMBER) {
					result = new StringFromNumber((NumberSpec) tmp);
				} else if (tmp.getType() == StyleSpecPart.T_NULL) {
					result = new ConstString("");
				} else {
					error("Expression didn't resolve to text rep", null);
				}
			} else {
				error("Expression didn't resolve to text rep", null);
			}
		}

		if (hadErrors())
			result = null;
	}

	StyleSpecPart process(Expression expr) throws SQLException
	{
		if (expr instanceof ArrayAccess) {
			error("Arrays not supported in style specs", expr.pos);
			return null;
		} else if (expr instanceof BasicNumericOp) {
			return process((BasicNumericOp) expr);
		} else if (expr instanceof BitwiseOp) {
			return process((BitwiseOp) expr);
		} else if (expr instanceof CompareOp) {
			return process((CompareOp) expr);
		} else if (expr instanceof ConditionalOp) {
			return process((ConditionalOp) expr);
		} else if (expr instanceof EqualsOp) {
			return process((EqualsOp) expr);
		} else if (expr instanceof ShiftOp) {
			return process((ShiftOp) expr);
		} else if (expr instanceof Literal) {
			return process((Literal) expr);
		} else if (expr instanceof MethodCall) {
			return process((MethodCall) expr);
		} else if (expr instanceof Property) {
			return process((Property) expr);
		} else if (expr instanceof Ternary) {
			return process((Ternary) expr);
		} else if (expr instanceof UnaryOp) {
			return process((UnaryOp) expr);
		} else {
			throw new UnsupportedOperationException("Unknown type of " + expr.getClass().getName());
		}
	}

	StyleSpecPart process(MethodCall call) throws SQLException
	{
		if (call.object == null) {
			return processGlobalMethod(call);
		}

		error("No method " + call.methodName + " found", call.pos);
		return null;
	}

	StyleSpecPart[] process(Expressions exprs) throws SQLException
	{
		int size = exprs.size();
		StyleSpecPart[] out = new StyleSpecPart[size];
		for (int a = 0; a < size; a++) {
			if ((out[a] = process(exprs.get(a))) == null)
				return null;
		}
		return out;
	}

	static boolean allOfType(StyleSpecPart[] ssps, int type)
	{
		for (StyleSpecPart part : ssps) {
			if (part.getType() != type)
				return false;
		}

		return true;
	}

	StyleSpecPart processGlobalMethod(MethodCall call) throws SQLException
	{
		Expressions params = call.params;
		String name = call.methodName;
		int nParams = params.size();

		StyleSpecPart[] ssps = process(params);
		if (ssps == null)
			return null;

		if (allOfType(ssps, StyleSpecPart.T_NUMBER)) {
			if (nParams == 1) {
				OneParamFunc opf = OneParamFunc.create(name, (NumberSpec) ssps[0]);
				if (opf != null)
					return opf;
			}
			if (nParams == 2) {
				TwoParamFunc tpf = TwoParamFunc.create(name, (NumberSpec) ssps[0], (NumberSpec) ssps[1]);
				if (tpf != null)
					return tpf;
			}
			NParamFunc npf = NParamFunc.create(name, ssps);
			if (npf != null)
				return npf;
		}

		error("Unknown global method " + name, call.pos);
		return null;
	}

	StyleSpecPart createFieldLookup(TablePath tablePath, Field field, Token posForErrs) throws SQLException
	{
		switch (field.type) {
		case BLOB:
			error("Can't reference blobs", posForErrs);
			return null;

		case BOOLEAN:
			return new BooleanField(new UserFieldPath(tablePath, field.id));

		case DATE:
		case DATETIME:
			return new DateField(new UserFieldPath(tablePath, field.id));

		case INTEGER:
		case DECIMAL:
			return new NumberField(new UserFieldPath(tablePath, field.id));

		case FOREIGN_ID:
			if (field.hasFlag(FieldFlags.MANDATORY))
				return new LayersTextRep(TableUtils.walk(tablePath, field, meta));
			else {
				error("Polje " + field.getName() + " ni obvezno, zato ga ni dovoljeno uporabiti v izrazu",
				                posForErrs);
				return null;
			}

		case PLAINTEXT:
			return new StringField(new UserFieldPath(tablePath, field.id));

		case LONGPLAINTEXT:
		case WIKITEXT:
			error("Polje " + field.getName() + " je potencialno predolgo, zato ga ni dovoljeno uporabiti v izrazu",
			                posForErrs);
			return null;

		case STYLE:
			error("Polje " + field.getName() + " vsebuje stil, zato ga ni dovoljeno uporabiti v izrazu",
			                posForErrs);
			return null;

		default:
			throw new IllegalArgumentException();
		}
	}

	StyleSpecPart processGlobalProp(Property expr) throws SQLException
	{
		String name = expr.propName;

		if (name.equals("$area")) {
			if (base.geomType.isPolygon()) {
				return new NumberField(new MetaFieldPath(new TablePath(base.id), MetaFieldPath.MF_AREA));
			} else {
				error("Površino imajo samo poligonski sloji", expr.pos);
				return null;
			}
		} else
		if (name.equals("$centX") || name.equals("$centY")) {
			if (base.geomType.isPolygon()) {
				return new NumberField(new MetaFieldPath(new TablePath(base.id), name.endsWith("X") ? MetaFieldPath.MF_CENTROID_X : MetaFieldPath.MF_CENTROID_Y));
			} else {
				error("Centroido imajo samo poligonski sloji", expr.pos);
			}
		} else
		if (name.equals("$id")) {
			return new NumberField(new MetaFieldPath(new TablePath(base.id), MetaFieldPath.MF_ID));
		} else
		if (name.equals("$length")) {
			if (base.geomType.isLine() || base.geomType.isPolygon()) {
				return new NumberField(new MetaFieldPath(new TablePath(base.id), MetaFieldPath.MF_LENGTH));
			} else {
				error("Dolžino geometrije imajo samo linijski in poligonski sloji", expr.pos);
				return null;
			}
		} else
		if (name.equals("$time")) {
			return new DateField(new MetaFieldPath(new TablePath(base.id), MetaFieldPath.MF_TIMESTAMP));
		}
		if (name.startsWith("$")) {
			String tmpname = name.substring(1);

			if (DBNames.isUserFieldName(tmpname)) {
				if (base == null) {
					error("Missing table", expr.pos);
					return null;
				}

				int fid = DBNames.getUserFieldIdFromName(tmpname);

				for (Field f : base.fields) {
					if (f.id == fid) {
						return createFieldLookup(new TablePath(base.id), f, expr.pos);
					}
				}
			}

			error("Field " + tmpname + " not found", expr.pos);
			return null;
		}

		NamedConstant nc = NamedConstant.get(name);
		if (nc == null)
			error("Neznana spremenljivka " + name, expr.pos);

		return nc;
	}

	StyleSpecPart process(Property expr) throws SQLException
	{
		if (expr.object == null)
			return processGlobalProp(expr);

		StyleSpecPart obj = process(expr.object);
		if (obj == null)
			return null;

		String name = expr.propName;
		if (obj instanceof DateSpec) {
			if ("day".equals(name))
				return new DateNumberProp(DateNumberProp.F_DAY_OF_MONTH, (DateSpec) obj);
			if ("hour".equals(name))
				return new DateNumberProp(DateNumberProp.F_HOUR_OF_DAY, (DateSpec) obj);
			if ("minute".equals(name))
				return new DateNumberProp(DateNumberProp.F_MINUTES, (DateSpec) obj);
			if ("month".equals(name))
				return new DateNumberProp(DateNumberProp.F_MONTH, (DateSpec) obj);
			if ("second".equals(name))
				return new DateNumberProp(DateNumberProp.F_SECONDS, (DateSpec) obj);
			if ("weekday".equals(name))
				return new DateNumberProp(DateNumberProp.F_WEEKDAY, (DateSpec) obj);
			if ("year".equals(name))
				return new DateNumberProp(DateNumberProp.F_YEAR, (DateSpec) obj);
			if ("isWeekday".equals(name))
				return new DateBoolProp(DateBoolProp.F_ISWEEKDAY, (DateSpec) obj);
			if ("isWeekend".equals(name))
				return new DateBoolProp(DateBoolProp.F_ISWEEKEND, (DateSpec) obj);
		} else if (obj instanceof NumberSpec) {
			error("Numbers have no property " + name, expr.pos);
			return null;
		}
		// no else!
		if (obj instanceof LayersTextRep) {
			LayersTextRep lpf = (LayersTextRep) obj;
			Table t = meta.getTableById(lpf.tablePath.lastTableId());

			if (name.equals("textRep"))
				return lpf;

			if (DBNames.isUserFieldName(name)) {
				int fid = DBNames.getUserFieldIdFromName(name);


				for (Field f : t.fields) {
					if (f.id == fid) {
						if (f.hasFlag(FieldFlags.MANDATORY))
							return createFieldLookup(lpf.tablePath, f, expr.pos);
						error("Polje " + f.getName() + " ni obvezno, zato ga ni dovoljeno uporabiti v izrazu",
						                expr.pos);
						return null;
					}
				}
			} else
			if (name.equals("area")) {
				if (t.geomType.isPolygon()) {
					return new NumberField(new MetaFieldPath(lpf.tablePath, MetaFieldPath.MF_AREA));
				} else {
					error("Površino imajo samo poligonski sloji", expr.pos);
					return null;
				}
			} else
			if (name.equals("centX") || name.equals("centY")) {
				if (t.geomType.isPolygon()) {
					return new NumberField(new MetaFieldPath(lpf.tablePath, name.endsWith("X") ? MetaFieldPath.MF_CENTROID_X : MetaFieldPath.MF_CENTROID_Y));
				} else {
					error("Centroido imajo samo poligonski sloji", expr.pos);
				}
			} else
			if (name.equals("id")) {
				return new NumberField(new MetaFieldPath(lpf.tablePath, MetaFieldPath.MF_ID));
			} else
			if (name.equals("length")) {
				if (t.geomType.isLine() || t.geomType.isPolygon()) {
					return new NumberField(new MetaFieldPath(lpf.tablePath, MetaFieldPath.MF_LENGTH));
				} else {
					error("Dolžino geometrije imajo samo linijski in poligonski sloji", expr.pos);
					return null;
				}
			} else
			if (name.equals("time")) {
				return new DateField(new MetaFieldPath(lpf.tablePath, MetaFieldPath.MF_TIMESTAMP));
			}
		}

		error("Unknown property " + name, expr.pos);
		return null;
	}

	StyleSpecPart process(UnaryOp expr) throws SQLException
	{
		StyleSpecPart base = process(expr.base);
		if (base == null)
			return null;

		int nt = expr.type;

		if (nt == Sym.PLUS || nt == Sym.MINUS || nt == Sym.TILDE) {
			if (base.getType() == StyleSpecPart.T_NUMBER) {
				return new NumUnaryOp(nt, (NumberSpec) base);
			} else {
				error("+, - and ~ only supported on numbers", expr.pos);
				return null;
			}
		} else if (nt == Sym.EXCL) {
			if (base.getType() == StyleSpecPart.T_BOOLEAN) {
				return new Not((BooleanSpec) base);
			} else {
				error("! only supported on booleans", expr.pos);
				return null;
			}
		} else {
			throw new UnsupportedOperationException("Unknown type of unaryop: " + nt);
		}
	}

	StyleSpecPart process(Ternary expr) throws SQLException
	{
		StyleSpecPart cond = process(expr.cond);
		if (cond == null)
			return null;

		if (cond.isType(StyleSpecPart.T_BOOLEAN, true)) {
			BooleanSpec condb = fixNullBool(cond);

			StyleSpecPart ifTrue = process(expr.ifTrue);
			StyleSpecPart ifFalse = process(expr.ifFalse);
			if (ifTrue == null || ifFalse == null)
				return null;

			if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_NUMBER)) {
				return new NumericTernary(condb, fixNullNum(ifTrue), fixNullNum(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_STRING)) {
				return new StringTernary(condb, toString(ifTrue, expr.pos), toString(ifFalse, expr.pos));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_BOOLEAN)) {
				return new BoolTernary(condb, fixNullBool(ifTrue), fixNullBool(ifFalse));
			} else if (ifTrue.getType() == StyleSpecPart.T_DATE && ifFalse.getType() == StyleSpecPart.T_DATE) {
				return new DateTernary(condb, (DateSpec) ifTrue, (DateSpec) ifFalse);
			} else {
				error("Uncompatible types for ?:", expr.pos);
				return null;
			}
		} else {
			error("Conditions in ?: can only be boolean", expr.pos);
			return null;
		}
	}

	StyleSpecPart process(Literal expr)
	{
		Object value = expr.value;

		if (value == null) {
			return null;
		} else if (value == Null.instance) {
			return NullSpec._instance;
		} else if (value instanceof Boolean) {
			return new ConstBool(((Boolean) value).booleanValue());
		} else if (value instanceof String) {
			return new ConstString((String) value);
		} else if (value instanceof Integer || value instanceof Long) {
			return new ConstLong(((Number) value).longValue());
		} else if (value instanceof Float || value instanceof Double) {
			return new ConstDouble(((Double) value).doubleValue());
		} else if (value instanceof Character) {
			return new ConstString(String.valueOf(((Character) value).charValue()));
		} else {
			error("Unrecognized literal (value = " + value + ", class = " + value.getClass().getName() + ")",
			                expr.pos);
			return null;
		}
	}

	static boolean bothOfTypeOneNotNull(StyleSpecPart a, StyleSpecPart b, int type)
	{
		int at = a.getType();
		int bt = b.getType();

		if (at == StyleSpecPart.T_NULL)
			return bt == type;
		if (bt == StyleSpecPart.T_NULL)
			return at == type;
		return at == type && bt == type;
	}

	StyleSpecPart process(EqualsOp expr) throws SQLException
	{
		StyleSpecPart left = process(expr.left);
		if (left == null)
			return null;

		StyleSpecPart right = process(expr.right);
		if (right == null)
			return null;

		if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_STRING)) {
			return new CompareString(toString(left, expr.pos), expr.type, toString(right, expr.pos));
		} else if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_NUMBER)) {
			return new CompareNum(fixNullNum(left), expr.type, fixNullNum(right));
		} else {
			error(SymUtil.toString(expr.type, null) + " only supported on strings and numbers", expr.pos);
			return null;
		}
	}

	StyleSpecPart process(CompareOp expr) throws SQLException
	{
		StyleSpecPart left = process(expr.left);
		if (left == null)
			return null;

		StyleSpecPart right = process(expr.right);
		if (right == null)
			return null;

		if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_STRING)) {
			return new CompareString(toString(left, expr.pos), expr.type, toString(right, expr.pos));
		} else if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_NUMBER)) {
			return new CompareNum(fixNullNum(left), expr.type, fixNullNum(right));
		} else {
			error(SymUtil.toString(expr.type, null) + " only supported on strings and numbers", expr.pos);
			return null;
		}
	}

	StyleSpecPart process(ShiftOp expr) throws SQLException
	{
		StyleSpecPart left = process(expr.left);
		if (left == null)
			return null;

		StyleSpecPart right = process(expr.right);
		if (right == null)
			return null;

		if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_NUMBER)) {
			return new NumBinaryOp(fixNullNum(left), expr.type, fixNullNum(right));
		} else {
			error(SymUtil.toString(expr.type, null) + " only supported on numbers", expr.pos);
			return null;
		}
	}

	StyleSpecPart process(BitwiseOp expr) throws SQLException
	{
		StyleSpecPart left = process(expr.left);
		if (left == null)
			return null;

		StyleSpecPart right = process(expr.right);
		if (right == null)
			return null;

		if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_BOOLEAN)) {
			return new AndOrXor(fixNullBool(left), expr.type, fixNullBool(right));
		} else if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_NUMBER)) {
			return new NumBinaryOp(fixNullNum(left), expr.type, fixNullNum(right));
		} else {
			error(SymUtil.toString(expr.type, null) + " only supported on booleans and numbers", expr.pos);
			return null;
		}
	}

	StyleSpecPart process(ConditionalOp expr) throws SQLException
	{
		StyleSpecPart left = process(expr.left);
		if (left == null)
			return null;

		StyleSpecPart right = process(expr.right);
		if (right == null)
			return null;

		if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_BOOLEAN)) {
			return new AndOrXor(fixNullBool(left), expr.type, fixNullBool(right));
		} else {
			error(SymUtil.toString(expr.type, null) + " only supported on booleans", expr.pos);
			return null;
		}
	}

	StyleSpecPart process(BasicNumericOp expr) throws SQLException
	{
		StyleSpecPart left = process(expr.left);
		if (left == null)
			return null;

		StyleSpecPart right = process(expr.right);
		if (right == null)
			return null;

		if ((left.getType() == StyleSpecPart.T_STRING || right.getType() == StyleSpecPart.T_STRING)
		                && expr.type == Sym.PLUS) {
			return new StringConcat(toString(left, expr.pos), toString(right, expr.pos));
		} else if (bothOfTypeOneNotNull(left, right, StyleSpecPart.T_NUMBER)) {
			return new NumBinaryOp(fixNullNum(left), expr.type, fixNullNum(right));
		} else {
			error(SymUtil.toString(expr.type, null) + " only supported on numeric types (and + on strings)",
			                expr.pos);
			return null;
		}
	}

	StringSpec toString(StyleSpecPart ssp, Token errPos)
	{
		if (ssp instanceof StringSpec)
			return (StringSpec) ssp;
		if (ssp instanceof NumberSpec)
			return new StringFromNumber((NumberSpec) ssp);
		if (ssp instanceof NullSpec)
			return new ConstString("");
		if (ssp instanceof BooleanSpec)
			return new StringFromBoolean((BooleanSpec) ssp);
		if (ssp instanceof DateSpec)
			return new StringFromDate((DateSpec) ssp);

		error(ssp + " can't be converted to string", errPos);
		return null;
	}

	public String[] getErrors()
	{
		if (errors == null)
			return null;

		String[] out = new String[errors.size()];
		for (int a = 0; a < out.length; a++)
			out[a] = (String) ((Pair) errors.get(a)).first;

		return out;
	}

	static NumberSpec fixNullNum(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new ConstLong(0);
		else
			return (NumberSpec) expr;
	}

	static BooleanSpec fixNullBool(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new ConstBool(false);
		else
			return (BooleanSpec) expr;
	}
}