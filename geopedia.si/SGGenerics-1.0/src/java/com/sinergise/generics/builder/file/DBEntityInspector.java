package com.sinergise.generics.builder.file;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sinergise.generics.builder.Inspector;
import com.sinergise.generics.core.XMLTags;
import com.sinergise.generics.datasource.DatabaseDataSource;

public class DBEntityInspector extends XMLEntityInspector{

	
	private Inspector oti;
	public DBEntityInspector(Document file) throws ParserConfigurationException,
			SAXException, IOException {
		super(file, XMLTags.DBEntities);
	}
	public DBEntityInspector setOTI(Inspector oti) {
		this.oti=oti;
		return this;
	}

	
	@Override
	protected Element processElement (Element element) {
		String str = element.getAttribute(DatabaseDataSource.ATTR_DATABASE_TABLE);
		return oti.inspect(str);
	}

}
