package com.sinergise.geopedia.style;

import java.sql.SQLException;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.numbers.CurrentScale;
import com.sinergise.geopedia.core.style.proxys.ThemeStyle;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.style.processor.StyleEvaluator;

public class StyleVerifier
{
	public static String isValid(String constStyleSpec, TiledCRS tiledCRS, MetaData meta)
	{
		if (constStyleSpec == null)
			throw new IllegalArgumentException();

		StyleSpec ss;
		try {
			ss = StyleCodec.decode(null, constStyleSpec, meta);
		} catch (ParseStyleException e) {
			return e.errors[0];
		} catch (SQLException e) {
			// TODO: any other reason to go to database?
			return "Stil ni konstanten";
		} catch (Exception e) {
			return "Neznana napaka: " + e.getMessage();
		} catch (StackOverflowError e) {
			return "Ciklično referenciranje";
			// XXX don't depend on stack overflow :P
		}

		if (!ss.isConst())
			return "Stil ni konstanten";

		CheckForThemeScale cfts = new CheckForThemeScale();
		ss.accept(cfts);
		int min = tiledCRS.getMinLevelId();
		int max = cfts.hasScale ? tiledCRS.getMaxLevelId() : tiledCRS.getMinLevelId();
		try {
			for (int level = min; level <= max; level++)
				new ConstStyler(level, ss);
		} catch (UnsupportedOperationException e) {
			return "Stil je konstanten, vendar še ni podprt: " + e.getMessage();
		}

		return null;
	}

	public static String isValid(Table t, String tableStyleSpec, TiledCRS tiledCRS, MetaData meta)
	{
		if (t == null || tableStyleSpec == null)
			throw new IllegalArgumentException();

		StyleSpec ss;
		try {
			ss = StyleCodec.decode(t, tableStyleSpec, meta);
		} catch (ParseStyleException e) {
			return e.errors[0];
		} catch (SQLException e) {
			return "Napaka pri dostopu do baze: " + e.getMessage();
		} catch (Exception e) {
			return "Neznana napaka: " + e.getMessage();
		} catch (StackOverflowError e) {
			return "Ciklično referenciranje";
			// XXX don't depend on stack overflow :P
		}

		CheckForThemeScale cfts = new CheckForThemeScale();
		ss.accept(cfts);
		if (cfts.hasTheme)
			return "V stilu tabele ni dovoljeno referencirati teme";

		int min = tiledCRS.getMinLevelId();
		int max = cfts.hasScale ? tiledCRS.getMaxLevelId() : tiledCRS.getMinLevelId();
		for (int level = min; level <= max; level++) {
			try {
				new StyleEvaluator(ss, t, null, level, meta);
			} catch (ParseStyleException e) {
				return e.errors[0];
			} catch (SQLException e) {
				return "Napaka pri dostopu do baze: " + e.getMessage();
			}
		}

		return null;
	}

	public static String isValid(String themeTableStyle, Table t, String themeStyleSpec, TiledCRS tiledCRS, MetaData meta)
	{
		if (themeTableStyle == null || t == null || themeStyleSpec == null)
			throw new IllegalArgumentException();

		StyleSpec ss;
		try {
			ss = StyleCodec.decode(t, themeStyleSpec, meta);
		} catch (ParseStyleException e) {
			return e.errors[0];
		} catch (SQLException e) {
			return "Napaka pri dostopu do baze: " + e.getMessage();
		} catch (Exception e) {
			return "Neznana napaka: " + e.getMessage();
		} catch (StackOverflowError e) {
			return "Ciklično referenciranje";
			// XXX don't depend on stack overflow :P
		}

		CheckForThemeScale cfts = new CheckForThemeScale();
		ss.accept(cfts);

		int min = tiledCRS.getMinLevelId();
		int max = cfts.hasScale ? tiledCRS.getMaxLevelId() : tiledCRS.getMinLevelId();
		for (int level = min; level <= max; level++) {
			try {
				new StyleEvaluator(ss, t, themeTableStyle, level, meta);
			} catch (ParseStyleException e) {
				return e.errors[0];
			} catch (SQLException e) {
				return "Napaka pri dostopu do baze: " + e.getMessage();
			}
		}

		return null;
	}

	static class CheckForThemeScale extends ReflectiveStyleVisitor
	{
		boolean hasTheme = false;
		boolean hasScale = false;

		public boolean visit(ThemeStyle ts, boolean entering)
		{
			hasTheme = true;
			return true;
		}

		public boolean visit(CurrentScale cs, boolean entering)
		{
			hasScale = true;
			return true;
		}
	}
}
