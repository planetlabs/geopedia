package com.sinergise.geopedia.style.processor;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;

import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.walk.MetaFieldPath;
import com.sinergise.geopedia.core.entities.walk.TablePath;
import com.sinergise.geopedia.core.entities.walk.UserFieldPath;
import com.sinergise.geopedia.core.style.NamedColors;
import com.sinergise.geopedia.core.style.Sym;
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
import com.sinergise.geopedia.core.style.model.NullSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.model.StyleSpecPart;
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

public class StyleResolver implements ErrorReporter
{
	ArrayList<Pair<String, Token>> errors = null;
	MetaData meta;
	
	public StyleResolver(MetaData meta) {
		this.meta=meta;
	}

	public void error(String msg, Token token)
	{
		if (errors == null)
			errors = new ArrayList<Pair<String,Token>>();

		errors.add(new Pair<String,Token>(msg, token));
	}

	public boolean hadErrors()
	{
		return errors != null;
	}

	StyleSpec result = null;
	Table base;

	public StyleSpec getResult()
	{
		return result;
	}

	public void process(String styleSpec, Table t) throws IOException, SQLException
	{
		process(new StringReader(styleSpec), t);
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
				if (tmp.getType() == StyleSpecPart.T_STYLE) {
					result = (StyleSpec) tmp;
				} else
				if (tmp.getType() == StyleSpecPart.T_NULL) {
					result = new NullStyle();
				} else {
					error("Expression didn't resolve to style spec", null);
				}
			} else {
				error("Expression didn't resolve to style spec", null);
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

		if ("style".equals(name)) {
			if (ssps.length == 4) {
				if (Match.is(name, ssps, "style", StyleSpecPart.T_LINE_STYLE, StyleSpecPart.T_FILL_STYLE,
								StyleSpecPart.T_SYMBOL_STYLE, StyleSpecPart.T_TEXT_STYLE)) {
					return new StyleDef(fixNullLineStyle(ssps[0]), fixNullFillStyle(ssps[1]),
									fixNullSymbolStyle(ssps[2]), fixNullTextStyle(ssps[3]));
				} else {
					error("napačni tipi parametrov pri style(line, fill, symbol, text)", call.pos);
					return null;
				}
			} else {
				error("style(line, fill, symbol, text) sprejme 4 parametre, ne "+ssps.length, call.pos);
				return null;
			}
		}
		if ("fillStyle".equals(name)) {
			if (ssps.length == 3) {
				if (Match.is(name, ssps, "fillStyle", StyleSpecPart.T_COLOR, StyleSpecPart.T_COLOR,
				                StyleSpecPart.T_FILL_TYPE)) {
					return new FillStyleDef(fixNullColor(ssps[0]), fixNullColor(ssps[1]), fixNullFillType(ssps[2]));
				} else {
					error("napačni tipi parametrov pri fillStyle(color, color, fillType)", call.pos);
					return null;
				}
			} else {
				error("fillStyle(color, color, fillType) sprejme 3 parametre, ne "+ssps.length, call.pos);
				return null;
			}
		}
		if ("lineStyle".equals(name)) {
			if (ssps.length == 3) {
				if (Match.is(name, ssps, "lineStyle", StyleSpecPart.T_COLOR, StyleSpecPart.T_NUMBER, StyleSpecPart.T_LINE_TYPE)) {
					return new LineStyleDef(fixNullColor(ssps[0]), fixNullNum(ssps[1]), fixNullLineType(ssps[2]));					
				} else {
					error("napačni tipi parametrov pri lineStyle(color, width, lineType)", call.pos);
				}
			} else {
				error("lineStyle(color, width, lineType) sprejme 3 parametre, ne "+ssps.length, call.pos);
				return null;
			}
		}
		
		if ("textStyle".equals(name)) {
			if (ssps.length == 5) {
				if (Match.is(name, ssps, "textStyle", StyleSpecPart.T_COLOR, StyleSpecPart.T_NUMBER,
				                StyleSpecPart.T_FONT_ID, StyleSpecPart.T_BOOLEAN, StyleSpecPart.T_BOOLEAN)) {
					return new TextStyleDef(fixNullColor(ssps[0]), fixNullNum(ssps[1]), fixNullFontId(ssps[2]),
					                fixNullBool(ssps[3]), fixNullBool(ssps[4]));
				} else {
					error("napačni tipi parametrov pri textStyle(color, size, font, bold, italic)", call.pos);
					return null;
				}
			} else {
				error("textStyle(color, size, font, bold, italic) sprejme 5 parametrov, ne "+ssps.length, call.pos);
				return null;
			}
		}
		
		if ("symbolStyle".equals(name)) {
			if (ssps.length == 4 || ssps.length == 5) { // length 5 is for backward compatibility (last one used to be symbol url)
				int typ0 = ssps[0].getType();
				int typ1 = ssps[1].getType();
				int typ2 = ssps[2].getType();
				int typ3 = ssps[3].getType();
				
				if ((typ0 == StyleSpecPart.T_SYMBOL_ID || typ0 == StyleSpecPart.T_NULL) &&
					(typ1 == StyleSpecPart.T_NUMBER || typ1 == StyleSpecPart.T_NULL) &&
					(typ2 == StyleSpecPart.T_COLOR || typ2 == StyleSpecPart.T_NULL) &&
					(typ3 == StyleSpecPart.T_STRING || typ3 == StyleSpecPart.T_NULL || typ3 == StyleSpecPart.T_BOOLEAN || typ3 == StyleSpecPart.T_NUMBER || typ3 == StyleSpecPart.T_DATE)) {
					return new SymbolStyleDef(fixNullSymbolId(ssps[0]), fixNullNum(ssps[1]), fixNullColor(ssps[2]),
					                toString(ssps[3], call.pos));
					// XXX
				} else {
					error("Napačni tipi parametrov pri symbolStyle(symbolId, size, color, text)", call.pos);
					return null;
				}
			} else {
				error("symbolStyle(symbolId, size, color, text) sprejme 4 parametre, ne "+ssps.length, call.pos);
				return null;
			}
		}

		if ("symbolId".equals(name)) {
			if (ssps.length == 1) {
				if (ssps[0].getType() == StyleSpecPart.T_NULL) {
					return new ConstSymbolId(SymbolId.NONE);
				}
				if (ssps[0].getType() == StyleSpecPart.T_NUMBER) {
					Integer val = intFromNum(ssps[0], call.pos);
					if (val == null) {
						return null;
					} else {
						return new ConstSymbolId(val);
					}
				}
				if (ssps[0].getType() == StyleSpecPart.T_STRING) {
					if (ssps[0] instanceof ConstString) {
						ConstString cs = (ConstString) ssps[0];
						int nall = SymbolId.ids.length;
						for (int a=0; a<nall; a++) {
							if (SymbolId.names[a].equalsIgnoreCase(cs.value)) {
								return new ConstSymbolId(SymbolId.ids[a]);
							}
						}
					}
					error("Imena simbolov so lahko samo znani konstantni nizi", call.pos);
					return null;
				}
			}
			
			error("symbolId() sprejema natanko en številčni parameter", call.pos);
			return null;
		}
		
		if ("fontId".equals(name)) {
			if (ssps.length == 1) {
				if (ssps[0].getType() == StyleSpecPart.T_NULL) {
					return new ConstFontId(FontId.NONE);
				}
				if (ssps[0].getType() == StyleSpecPart.T_NUMBER) {
					Integer val = intFromNum(ssps[0], call.pos);
					if (val == null) {
						return null;
					} else {
						return new ConstFontId(val);
					}
				}
				if (ssps[0].getType() == StyleSpecPart.T_STRING) {
					if (ssps[0] instanceof ConstString) {
						ConstString cs = (ConstString) ssps[0];
						int nall = FontId.ids.length;
						for (int a=0; a<nall; a++) {
							if (FontId.names[a].equalsIgnoreCase(cs.value)) {
								return new ConstFontId(FontId.ids[a]);
							}
						}
					}
					error("Imena fontov so lahko samo znani konstantni nizi", call.pos);
					return null;
				}
			}
			
			error("fontId() sprejema natanko en številčni parameter", call.pos);
			return null;
		}

		if ("lineType".equals(name)) {
			if (ssps.length == 1) {
				if (ssps[0].getType() == StyleSpecPart.T_NULL) {
					return new ConstLineType(LineType.NONE);
				}
				if (ssps[0].getType() == StyleSpecPart.T_NUMBER) {
					Integer val = intFromNum(ssps[0], call.pos);
					if (val == null) {
						return null;
					} else {
						return new ConstLineType(val);
					}
				}
				if (ssps[0].getType() == StyleSpecPart.T_STRING) {
					if (ssps[0] instanceof ConstString) {
						ConstString cs = (ConstString) ssps[0];
						int nall = LineType.ids.length;
						for (int a=0; a<nall; a++) {
							if (LineType.names[a].equalsIgnoreCase(cs.value)) {
								return new ConstLineType(LineType.ids[a]);
							}
						}
					}
					error("Imena tipov črt so lahko samo znani konstantni nizi", call.pos);
					return null;
				}
			}
			
			error("lineType() sprejema natanko en številčni parameter", call.pos);
			return null;
		}

		if ("fillType".equals(name)) {
			if (ssps.length == 1) {
				if (ssps[0].getType() == StyleSpecPart.T_NULL) {
					return new ConstFillType(FillType.NONE);
				}
				if (ssps[0].getType() == StyleSpecPart.T_NUMBER) {
					Integer val = intFromNum(ssps[0], call.pos);
					if (val == null) {
						return null;
					} else {
						return new ConstFillType(val);
					}
				}
				if (ssps[0].getType() == StyleSpecPart.T_STRING) {
					if (ssps[0] instanceof ConstString) {
						ConstString cs = (ConstString) ssps[0];
						int nall = FillType.ids.length;
						for (int a=0; a<nall; a++) {
							if (FillType.names[a].equalsIgnoreCase(cs.value)) {
								return new ConstFillType(FillType.ids[a]);
							}
						}
					}
					error("Imena tipov notranjosti so lahko samo znani konstantni nizi", call.pos);
					return null;
				}
			}
			
			error("fillType() sprejema natanko en številčni parameter", call.pos);
			return null;
		}

		if (Match.is(name, ssps, "rgb", StyleSpecPart.T_NUMBER, StyleSpecPart.T_NUMBER,
		                StyleSpecPart.T_NUMBER)) {
			return new ARGB(new ConstLong(0xFF), fixNullNum(ssps[0]), fixNullNum(ssps[1]), fixNullNum(ssps[2]));
		}
		if (Match.is(name, ssps, "argb", StyleSpecPart.T_NUMBER, StyleSpecPart.T_NUMBER,
		                StyleSpecPart.T_NUMBER, StyleSpecPart.T_NUMBER)) {
			return new ARGB(fixNullNum(ssps[0]), fixNullNum(ssps[1]), fixNullNum(ssps[2]),
			                fixNullNum(ssps[3]));
		}
		if (Match.is(name, ssps, "rgb", StyleSpecPart.T_NUMBER)) {
			Integer rgb = intFromNum(ssps[0], call.pos);
			if (rgb == null)
				return null;
			
			return new ConstColor(0xFF000000 | rgb);
		}
		if (Match.is(name, ssps, "argb", StyleSpecPart.T_NUMBER)) {
			Integer argb = intFromNum(ssps[0], call.pos);
			if (argb == null)
				return null;
			
			return new ConstColor(argb);
		}
		if (Match.is(name, ssps, "color", StyleSpecPart.T_STRING)) {
			if (ssps[0] instanceof ConstString) {
				ConstString cs = (ConstString) ssps[0];
				if (cs.value != null) {
					Integer val = NamedColors.nameToARGB.get(cs.value.toLowerCase());
					if (val != null) {
						return new ConstColor(val.intValue());
					}
				}
			}
			error("Imena barv so lahko samo znani konstantni nizi", call.pos);
			return null;
		}
		if (name.equals("colorMap") && ssps.length > 1) {
			boolean isOk = false;
			if (ssps.length % 2 == 0) {
				isOk = true;
				for (int a = 0; a < ssps.length; a++) {
					if ((a & 1) == 0) {
						isOk &= ssps[a].getType() == StyleSpecPart.T_NUMBER;
					} else {
						isOk &= ssps[a].getType() == StyleSpecPart.T_COLOR;
					}
				}
			}
			if (isOk) {
				NumberSpec val = (NumberSpec) ssps[0];
				ColorSpec[] colors = new ColorSpec[ssps.length / 2];
				for (int a = 0; a < colors.length; a++)
					colors[a] = (ColorSpec) ssps[2 * a + 1];
				NumberSpec[] steps = new NumberSpec[colors.length - 1];
				for (int a = 0; a < steps.length; a++)
					steps[a] = (NumberSpec) ssps[2 + 2 * a];

				return new ColorMap(val, colors, steps);
			}
		}
		if (name.equals("colorBlend") && ssps.length > 1) {
			boolean isOk = false;
			if (ssps.length % 2 == 1) {
				isOk = true;
				for (int a = 0; a < ssps.length; a++) {
					if ((a & 1) == 0) {
						isOk &= ssps[a].getType() == StyleSpecPart.T_NUMBER;
					} else {
						isOk &= ssps[a].getType() == StyleSpecPart.T_COLOR;
					}
				}
			}
			if (isOk) {
				NumberSpec val = (NumberSpec) ssps[0];
				ColorSpec[] colors = new ColorSpec[ssps.length / 2];
				for (int a = 0; a < colors.length; a++)
					colors[a] = (ColorSpec) ssps[2 * a + 1];
				NumberSpec[] steps = new NumberSpec[colors.length];
				for (int a = 0; a < steps.length; a++)
					steps[a] = (NumberSpec) ssps[2 * a + 2];

				return new ColorBlend(val, colors, steps);
			}
		}

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

	Integer intFromNum(StyleSpecPart ssp, Token errPos)
	{
		if (ssp instanceof ConstLong)
			return (int) ((ConstLong)ssp).value;
		if (ssp instanceof ConstDouble)
			return (int) ((ConstDouble)ssp).value;
		if (ssp instanceof NullSpec)
			return 0;
	
		error("Namesto "+ssp+" morate vnesti konstantno število", errPos);
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
			return new LayersStyle(TableUtils.walk(tablePath, field, meta));

		case PLAINTEXT:
			return new StringField(new UserFieldPath(tablePath, field.id));

		case LONGPLAINTEXT:
		case WIKITEXT:
			error("Polje "+field.getName()+" je predolgo, zato ga ni dovoljeno uporabiti v stilu", posForErrs);
			return null;
			
		case STYLE:
			return new StyleField(new UserFieldPath(tablePath, field.id));

		default:
			throw new IllegalArgumentException();
		}
	}

	StyleSpecPart processGlobalProp(Property expr) throws SQLException
	{
		String name = expr.propName;

		if (name.equals("$this")) {
			if (base == null) {
				error("Missing table", expr.pos);
				return null;
			}
			return new LayersStyle(new TablePath(base.id));
		}
		if (name.equals("$theme")) {
			return new ThemeStyle();
		}
		if (name.equals("$textRep")) {
			return new LayersTextRep(new TablePath(base.id));
		}
		if (name.equals("$area")) {
			if (base.geomType.isPolygon()) {
				return new NumberField(new MetaFieldPath(new TablePath(base.id), MetaFieldPath.MF_AREA));
			} else {
				error("Površino imajo samo poligonski sloji", expr.pos);
				return null;
			}
		}
		if (name.equals("$centX") || name.equals("$centY")) {
			if (base.geomType.isPolygon()) {
				return new NumberField(new MetaFieldPath(new TablePath(base.id), name.endsWith("X") ? MetaFieldPath.MF_CENTROID_X : MetaFieldPath.MF_CENTROID_Y));
			} else {
				error("Centroido imajo samo poligonski sloji", expr.pos);
			}
		}
		if (name.equals("$id")) {
			return new NumberField(new MetaFieldPath(new TablePath(base.id), MetaFieldPath.MF_ID));
		}
		if (name.equals("$length")) {
			if (base.geomType.isLine() || base.geomType.isPolygon()) {
				return new NumberField(new MetaFieldPath(new TablePath(base.id), MetaFieldPath.MF_LENGTH));
			} else {
				error("Dolžino geometrije imajo samo linijski in poligonski sloji", expr.pos);
				return null;
			}
		}
		if (name.equals("$time")) {
			return new DateField(new MetaFieldPath(new TablePath(base.id), MetaFieldPath.MF_TIMESTAMP));
		}

		if (name.startsWith("$")) {
			if ("$scale".equals(name))
				return new CurrentScale();
			
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
			error("Neznana spremenljivka "+name, expr.pos);
		
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
		if (obj instanceof StyleSpec) {
			if ("fillStyle".equals(name))
				return new FillStyleOf((StyleSpec) obj);
			if ("lineStyle".equals(name))
				return new LineStyleOf((StyleSpec) obj);
			if ("symbolStyle".equals(name))
				return new SymbolStyleOf((StyleSpec) obj);
			if ("textStyle".equals(name))
				return new TextStyleOf((StyleSpec) obj);
		} else if (obj instanceof LineStyleSpec) {
			if ("color".equals(name))
				return new LineColorOf((LineStyleSpec) obj);
			if ("width".equals(name))
				return new LineWidthOf((LineStyleSpec) obj);
			if ("type".equals(name))
				return new LineWidthOf((LineStyleSpec) obj);
		} else if (obj instanceof FillStyleSpec) {
			if ("bgColor".equals(name))
				return new BgColorOf((FillStyleSpec) obj);
			if ("fgColor".equals(name))
				return new FgColorOf((FillStyleSpec) obj);
			if ("type".equals(name))
				return new FillTypeOf((FillStyleSpec) obj);
		} else if (obj instanceof SymbolStyleSpec) {
			if ("size".equals(name))
				return new SymbolSizeOf((SymbolStyleSpec) obj);
			if ("color".equals(name))
				return new SymbolColorOf((SymbolStyleSpec) obj);
			if ("symbolId".equals(name))
				return new SymbolIdOf((SymbolStyleSpec) obj);
			if ("text".equals(name))
				return new SymbolTextOf((SymbolStyleSpec) obj);
		} else if (obj instanceof TextStyleSpec) {
			if ("bold".equals(name))
				return new BoldOf((TextStyleSpec) obj);
			if ("italic".equals(name))
				return new ItalicOf((TextStyleSpec) obj);
			if ("height".equals(name))
				return new FontHeightOf((TextStyleSpec) obj);
			if ("font".equals(name))
				return new FontIdOf((TextStyleSpec) obj);
			if ("color".equals(name))
				return new FontColorOf((TextStyleSpec) obj);
		} else if (obj instanceof ColorSpec) {
			if ("red".equals(name))
				return new ColorComponent(ColorComponent.RED, (ColorSpec) obj);
			if ("green".equals(name))
				return new ColorComponent(ColorComponent.GREEN, (ColorSpec) obj);
			if ("blue".equals(name))
				return new ColorComponent(ColorComponent.BLUE, (ColorSpec) obj);
			if ("alpha".equals(name))
				return new ColorComponent(ColorComponent.ALPHA, (ColorSpec) obj);
			if ("invert".equals(name))
				return new ColorProcess(ColorProcess.T_INVERT, (ColorSpec) obj);
			if ("grayscale".equals(name))
				return new ColorProcess(ColorProcess.T_GRAYSCALE, (ColorSpec) obj);
			if ("shift".equals(name))
				return new ColorProcess(ColorProcess.T_SHIFT, (ColorSpec) obj);
			if ("brighter".equals(name))
				return new ColorProcess(ColorProcess.T_BRIGHTER, (ColorSpec) obj);
			if ("darker".equals(name))
				return new ColorProcess(ColorProcess.T_DARKER, (ColorSpec) obj);
		} else if (obj instanceof DateSpec) {
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
		} else if (obj instanceof StringSpec) {
			error("Strings have no property " + name, expr.pos);
			return null;
		}
		// no else!
		if (obj instanceof LayersStyle) {
			LayersStyle lpf = (LayersStyle) obj;
			Table t = meta.getTableById(lpf.tablePath.lastTableId());
			
			if (DBNames.isUserFieldName(name)) {
				int fid = DBNames.getUserFieldIdFromName(name);

				for (Field f : t.fields) {
					if (f.id == fid) {
						return createFieldLookup(lpf.tablePath, f, expr.pos);
					}
				}
			} else
			if ("textRep".equals(name)) {
				return new LayersTextRep(lpf.tablePath);
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
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_COLOR)) {
				return new ColorTernary(condb, fixNullColor(ifTrue), fixNullColor(ifFalse));
			} else if (ifTrue.getType() == StyleSpecPart.T_DATE && ifFalse.getType() == StyleSpecPart.T_DATE) {
				return new DateTernary(condb, (DateSpec) ifTrue, (DateSpec) ifFalse);
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_FILL_STYLE)) {
				return new FillStyleTernary(condb, fixNullFillStyle(ifTrue), fixNullFillStyle(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_FILL_TYPE)) {
				return new FillTypeTernary(condb, fixNullFillType(ifTrue), fixNullFillType(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_FONT_ID)) {
				return new FontIdTernary(condb, fixNullFontId(ifTrue), fixNullFontId(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_LINE_STYLE)) {
				return new LineStyleTernary(condb, fixNullLineStyle(ifTrue), fixNullLineStyle(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_LINE_TYPE)) {
				return new LineTypeTernary(condb, fixNullLineType(ifTrue), fixNullLineType(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_SYMBOL_ID)) {
				return new SymbolIdTernary(condb, fixNullSymbolId(ifTrue), fixNullSymbolId(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_SYMBOL_STYLE)) {
				return new SymbolStyleTernary(condb, fixNullSymbolStyle(ifTrue), fixNullSymbolStyle(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_TEXT_STYLE)) {
				return new TextStyleTernary(condb, fixNullTextStyle(ifTrue), fixNullTextStyle(ifFalse));
			} else if (bothOfTypeOneNotNull(ifTrue, ifFalse, StyleSpecPart.T_STYLE)) {
				return new StyleTernary(condb, fixNullStyle(ifTrue), fixNullStyle(ifFalse));
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

		if ((left.getType() == StyleSpecPart.T_STRING || right.getType() == StyleSpecPart.T_STRING) && expr.type == Sym.PLUS) {
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
			return new StringFromNumber((NumberSpec)ssp);
		if (ssp instanceof NullSpec)
			return new ConstString("");
		if (ssp instanceof BooleanSpec)
			return new StringFromBoolean((BooleanSpec)ssp);
		if (ssp instanceof DateSpec)
			return new StringFromDate((DateSpec)ssp);
		
		error(ssp+" can't be converted to string", errPos);
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

	static FillStyleSpec fixNullFillStyle(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new NullFillStyle();
		else
			return (FillStyleSpec) expr;
	}

	static LineStyleSpec fixNullLineStyle(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new NullLineStyle();
		else
			return (LineStyleSpec) expr;
	}

	static SymbolStyleSpec fixNullSymbolStyle(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new NullSymbolStyle();
		else
			return (SymbolStyleSpec) expr;
	}

	static TextStyleSpec fixNullTextStyle(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new NullTextStyle();
		else
			return (TextStyleSpec) expr;
	}

	static StyleSpec fixNullStyle(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new NullStyle();
		else
			return (StyleSpec) expr;
	}

	static FillTypeSpec fixNullFillType(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new ConstFillType(FillType.NONE);
		else
			return (FillTypeSpec) expr;
	}

	static LineTypeSpec fixNullLineType(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new ConstLineType(LineType.NONE);
		else
			return (LineTypeSpec) expr;
	}

	static FontIdSpec fixNullFontId(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new ConstFontId(FontId.NONE);
		else
			return (FontIdSpec) expr;
	}

	static SymbolIdSpec fixNullSymbolId(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new ConstSymbolId(SymbolId.NONE);
		else
			return (SymbolIdSpec) expr;
	}

	static ColorSpec fixNullColor(StyleSpecPart expr)
	{
		if (expr instanceof NullSpec)
			return new ConstColor(0);
		else
			return (ColorSpec) expr;
	}
}