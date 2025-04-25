package com.sinergise.geopedia.style;

import java.io.IOException;
import java.sql.SQLException;

import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.style.consts.ConstString;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.style.processor.TextRepResolver;

public class TextRepCodec
{
	public static StringSpec decode(Table table, String spec, MetaData meta) throws ParseStyleException,
	                SQLException
	{
		if (spec != null)
			spec = spec.trim();
		
		if (spec == null || spec.length() < 1 || spec.equals("null"))
			return new ConstString("");

		TextRepResolver resolver = new TextRepResolver(meta);
		try {
			resolver.process(spec, table);
		} catch (IOException e) {
			throw new ParseStyleException(new String[] { e.getMessage() });
		}

		if (resolver.hadErrors()) {
			String[] errs = resolver.getErrors();
			for (String s : errs) {
				System.err.println(s);
			}
			throw new ParseStyleException(errs);
		}

		return resolver.getResult();
	}

	public static String encode(StringSpec spec)
	{
		StringBuffer sb = new StringBuffer();
		spec.toString(sb);
		return sb.toString();
	}
}
