package com.sinergise.geopedia.style;

import java.io.IOException;
import java.sql.SQLException;

import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.nulls.NullStyle;
import com.sinergise.geopedia.db.entities.MetaData;
import com.sinergise.geopedia.style.processor.StyleResolver;

public class StyleCodec
{
	public static StyleSpec decode(Table table, String spec, MetaData meta) throws ParseStyleException,
	                SQLException
	{
		if (spec != null)
			spec = spec.trim();
		
		if (spec == null || spec.length() < 1 || spec.equals("null"))
			return NullStyle.instance;

		StyleResolver resolver = new StyleResolver(meta);
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

	public static String encode(StyleSpec spec)
	{
		StringBuffer sb = new StringBuffer();
		spec.toString(sb);
		return sb.toString();
	}
}
