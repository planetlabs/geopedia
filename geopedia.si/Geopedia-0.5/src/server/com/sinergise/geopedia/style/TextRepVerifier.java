package com.sinergise.geopedia.style;

import java.sql.SQLException;

import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.style.processor.TextRepEvaluator;

public class TextRepVerifier
{
	public static String isValid(Table t, String tableTextRep, MetaData meta)
	{
		if (t == null || tableTextRep == null)
			throw new IllegalArgumentException();

		StringSpec ss;
		try {
			ss = TextRepCodec.decode(t, tableTextRep, meta);
		} catch (ParseStyleException e) {
			return e.errors[0];
		} catch (SQLException e) {
			return "Napaka pri dostopu do baze: " + e.getMessage();
		} catch (Exception e) {
			return "Neznana napaka: " + e.getMessage();
		} catch (StackOverflowError e) {
			return "Ciklično referenciranje";
			// XXX don't depend on stack overflow :P
			// can it happen here as well?
		}

		try {
			new TextRepEvaluator(ss, t, meta);
		} catch (ParseStyleException e) {
			return e.errors[0];
		} catch (SQLException e) {
			return "Napaka pri dostopu do baze: " + e.getMessage();
		}

		return null;
	}
}
