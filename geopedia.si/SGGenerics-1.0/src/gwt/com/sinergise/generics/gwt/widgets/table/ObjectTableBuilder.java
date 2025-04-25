package com.sinergise.generics.gwt.widgets.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.gwt.GwtEntityTypesStorage;
import com.sinergise.generics.gwt.core.CreationListener;
import com.sinergise.generics.gwt.core.GWTWidgetInspector;
import com.sinergise.generics.gwt.core.IsCreationProvider;
import com.sinergise.generics.gwt.widgetbuilders.GenericWidgetFactory;
import com.sinergise.generics.gwt.widgetbuilders.TableWidgetBuilder;
import com.sinergise.generics.gwt.widgetprocessors.GWTTableDataProvider;
import com.sinergise.generics.gwt.widgetprocessors.TableValueBinderWidgetProcessor;

public abstract class ObjectTableBuilder<T> {
	public boolean ignoreCaseInFilter = false;
	public static class AttributeWidgetInfo {
		private String name;
		private int genericsType;
		private Map<String, String> widgetMetadata;
		private boolean primaryKey = false;
		List<AttributeWidgetInfo> extraAttributes = new ArrayList<AttributeWidgetInfo>();
		
		
		public AttributeWidgetInfo(String att, int type, Map<String, String> metadata) {
			this.name = att;
			this.genericsType = type;
			this.widgetMetadata = metadata;
		}
		
		public void addExtraAttribute(AttributeWidgetInfo info) {
			extraAttributes.add(info);
		}

		public void setPrimaryKey(boolean primaryKey) {
			this.primaryKey = primaryKey;
		}
		
		public String getName() {
			return name;
		}
		public int getGenericsType() {
			return genericsType;
		}
		
		public void setGenericsType(int genericsType) {
			this.genericsType = genericsType;
		}
		
		public List<AttributeWidgetInfo> getExtraAttributes() {
			return extraAttributes;
		}
		public Map<String, String> getWidgetMetadata() {
			return widgetMetadata;
		}

		public boolean isPrimaryKey() {
			return primaryKey;
		}
	}
	
	protected final ArrayList<T> objects;
	private final GWTWidgetInspector wi;
	private final ArrayValueHolder avh;
	private final String[] attributes;
	private volatile boolean inited = false;
	
	public ObjectTableBuilder(String typeName, String[] attributes) {
		objects = new ArrayList<T>();
		EntityType type = GwtEntityTypesStorage.getInstance().createNewEntityType(typeName);
		wi = new GWTWidgetInspector(type, new HashMap<String, String>());
		avh = new ArrayValueHolder(wi.getEntityType());
		this.attributes = attributes;
	}
	
	private void initWidgetInspector() {
		synchronized(wi) {
			if (inited) return;
			inited = true;
			int len = attributes.length;
			
			for (int i = 0; i < len; i++) {
				AttributeWidgetInfo attInfo = getAttributeMetadata(attributes[i], i);
				if (attInfo == null) {
					continue;
				}
				addAttribute(wi, attInfo);
			}
		}
	}


	public static void addAttribute(GWTWidgetInspector wi, AttributeWidgetInfo attInfo) {
		int attId = wi.getEntityType().addTypeAttribute(new TypeAttribute(Integer.MIN_VALUE, attInfo.getName(), attInfo.getGenericsType()));
		if (attInfo.isPrimaryKey()) {
			wi.getEntityType().setPrimaryKeyId(attId);
		}
		wi.getAttributeMetadata(attInfo.getName()).putAll(attInfo.getWidgetMetadata());
		for (AttributeWidgetInfo xtra : attInfo.getExtraAttributes()) {
			addAttribute(wi, xtra);
		}
	}
	
	protected Map<String, String> addExtraAttribute(String name, int attType) {
		EntityType type = wi.getEntityType();
		type.addTypeAttribute(new TypeAttribute(type.getAttributeCount(), name, attType));
		wi.setAttributeMetaAttribute(name, MetaAttributes.IGNORE, MetaAttributes.BOOLEAN_TRUE);
		return wi.getAttributeMetadata(name);
	}

	public void addData(T row) {
		initWidgetInspector();
		objects.add(row);
		avh.add(convertToEntity(row));
	}
	
	public void removeData(int rowIndex) {
		initWidgetInspector();
		objects.remove(rowIndex);
		avh.remove(rowIndex);
	}
	
	public T getData(int rowIndex) {
		return objects.get(rowIndex);
	}
	
	public ArrayValueHolder getValues() {
		return avh;
	}

	protected GenercsTable createTable() {
		return new GenercsTable();
	}
	
	protected TableValueBinderWidgetProcessor createValueWidgetProcessor() {
		return new TableValueBinderWidgetProcessor();
	}
	

	protected TableDataProvider createTableDataProvider(TableValueBinderWidgetProcessor twp) {
		GWTTableDataProvider tdp = new GWTTableDataProvider(twp);
		tdp.setTableData(avh);
		tdp.setIgnoreCaseInFilter(ignoreCaseInFilter);
		return tdp;
	}
	
	public Widget generateTable() {
		initWidgetInspector();
		
		final GenercsTable gt = createTable();
		final TableValueBinderWidgetProcessor twp = createValueWidgetProcessor();
		gt.addWidgetProcessor(twp);
		gt.setDataProvider(createTableDataProvider(twp));
		gt.setWidgetBuilder(new TableWidgetBuilder());
		gt.addCreationListener(new CreationListener() {
			@Override
			public void creationCompleted(IsCreationProvider w) {
				gt.repaint();
			}
		});
		return GenericWidgetFactory.buildWidget(gt, wi);
	}

	/**
	 * 
	 * @param att Attribute name
	 * @param i Attribute index
	 * @param meta_out meta info
	 * @return attribute generics type
	 */
	protected abstract AttributeWidgetInfo getAttributeMetadata(String att, int i);
	
	protected EntityObject convertToEntity(T row) {
		EntityObject eo = GwtEntityTypesStorage.getInstance().createEntityObject(wi.getEntityType());
		populateEntity(eo, row);
		afterEntityCreated(row, eo);
		return eo;
	}

	public void populateEntity(EntityObject eo, T row) {
		int cnt = eo.getType().getAttributeCount();
		for (int i = 0; i < cnt; i++) {
			setAttributeValue(row, eo, eo.getType().getAttribute(i));
		}
	}

	public void setAttributeValue(T row, EntityObject eo, TypeAttribute att) {
		eo.setPrimitiveValue(att.getId(), getValue(row, att, wi.getAttributeMetadata(att.getName())));
	}

	protected void afterEntityCreated(T row, EntityObject eo) {
	}
	
	protected abstract String getValue(T row, TypeAttribute typeAttribute, Map<String, String> typeMeta);
}
