package com.sinergise.java.util.settings;

import static com.sinergise.java.util.settings.ObjectStorage.ID_ATTR_NAME;
import static com.sinergise.java.util.settings.ObjectStorage.NULL_ELEM_NAME;
import static com.sinergise.java.util.settings.ObjectStorage.TYPE_ATTR_NAME;
import static com.sinergise.java.util.string.StringSerializer.valueOf;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.settings.NamedTypedObject;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.common.util.settings.Settings.NeedsUpdateAfterDeserialization;
import com.sinergise.java.util.string.StringSerializer;
import com.sinergise.java.util.xml.DefaultContentHandler;

class SettingsFromXMLReader extends DefaultContentHandler {
	
	static Logger logger = LoggerFactory.getLogger(SettingsFromXMLReader.class);
	
	private static class PendingObject<T, A extends T> extends NamedTypedObject<T> {
		String id;
		ResolvedType<A> actualType;
		List<PendingObject<?, ?>> childValues = new ArrayList<PendingObject<?, ?>>();
		

		public PendingObject(String name, ResolvedType<T> declaredType) {
			super(name, declaredType, null);
		}

		public PendingObject(String attName, ResolvedType<T> declaredType, T value) {
			super(attName, declaredType, value);
		}

		public boolean isEmpty() {
			return childValues.isEmpty();
		}

		public MapTransformer<T> getExpectedTransformer() {
			return MapTransformer.getFor(expectedType.rawType);
		}

		public MapTransformer<A> getActualTransformer() {
			return MapTransformer.getFor(actualType.rawType);
		}
		
		public <C, D extends C> void addChild(PendingObject<C, D> child) {
			childValues.add(child);
		}

		public <C, D extends C> PendingObject<C, D> addChild(String attributeName, boolean complex) {
			@SuppressWarnings("unchecked")
			ResolvedType<C> childType = (ResolvedType<C>)getActualTransformer().childType(actualType, attributeName, complex);
			if (childType == null) {
				logger.error("Child type could not be determined for {} attribute of {}", attributeName, actualType);
				return null;
			}
			
			PendingObject<C, D> childObj = new PendingObject<C, D>(attributeName, childType);
			if (!complex) {
				childObj.actualType = childObj.expectedType.cast();
			}
			addChild(childObj);
			return childObj;
		}

		public void resolveActualType(String typeString) {
			actualType = getExpectedTransformer().resolveActualType(typeString, expectedType).cast();
		}
		
		public void setId(String id) {
			this.id = id;
		}
	}

	private final Stack<PendingObject<Object, Object>> pendingObjects = new Stack<PendingObject<Object, Object>>();
	PendingObject<?, ?> rootObject;
	
//	private final ArrayList<ArrayList<NamedTypedObject>> pendingValues      = new ArrayList<ArrayList<NamedTypedObject>>();
//	private final ArrayList<ResolvedType>                pendingDeclTypes   = new ArrayList<ResolvedType>();
//	private final ArrayList<ResolvedType>                pendingActualTypes = new ArrayList<ResolvedType>();
//	private final ArrayList<String>                      pendingIDs         = new ArrayList<String>();
	
	private final HashMap<String, Object>                read               = new HashMap<String, Object>();
	
	private boolean                                      doIDs              = false;
	private int                                          curId              = -1;
	
	private final StringBuilder								 pendingChars		= new StringBuilder();
	
	public <T> SettingsFromXMLReader(final T root, final ResolvedType<T> declaredRootClass, final boolean doIDs) {
		this.doIDs = doIDs;
		rootObject = createForRoot(declaredRootClass, root);
	}

	private static <E, A extends E> PendingObject<E, A> createForRoot(ResolvedType<E> declaredRootClass, A root) {
		PendingObject<E, A> po = new PendingObject<E, A>("ROOT", declaredRootClass);
		po.value = root;
		initForRoot(po);
		return po;
	}

	@SuppressWarnings("unchecked")
	private static <E, A extends E> void initForRoot(final PendingObject<E, A> po) {
		if (po.value == null) {
			return;
		}
		A root = (A)po.value;
		ResolvedType<E> declaredType = po.expectedType;
		Class<A> cls = (Class<A>)root.getClass();
		TypeVariable<?>[] tvars = cls.getTypeParameters();
		
		ResolvedType<A> actualType = null;
		if (declaredType.rawType == cls) {
			actualType = (ResolvedType<A>)declaredType;
			
		} else if (tvars == null || tvars.length == 0) {
			actualType = new ResolvedType<A>(cls);
			
		} else if (ArrayUtil.equals(tvars, declaredType.rawType.getTypeParameters())) {
			actualType = new ResolvedType<A>(cls, declaredType.parameterTypes);
			
		}
		po.actualType = actualType;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		pendingChars.append(ch, start, length);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void startElement(final String qName, final Attributes atts) {
		pendingChars.setLength(0);
		PendingObject myObj = createNextObject(qName, atts);
		processAttributes(atts, myObj);
	}

	@SuppressWarnings("unchecked")
	private PendingObject<?, ?> createNextObject(final String qName, final Attributes atts) {
		if (pendingObjects.size() > 0) {
			PendingObject<?, ?> parent = pendingObjects.peek();
			pendingObjects.push(parent.addChild(qName, true));
		} else {
			pendingObjects.push((PendingObject<Object, Object>)rootObject);
		}
		PendingObject<?, ?> myObj = pendingObjects.peek();
		myObj.setId(resolveId(atts.getValue(ID_ATTR_NAME))); 
		myObj.resolveActualType(atts.getValue(TYPE_ATTR_NAME));
		return myObj;
	}

	private <T, A extends T> void processAttributes(final Attributes atts, PendingObject<T, A> myObj) {
		for (int i = 0; i < atts.getLength(); i++) {
			String attName = atts.getQName(i);
			if (ID_ATTR_NAME.equals(attName) || TYPE_ATTR_NAME.equals(attName)) {
				continue;
			}
			processEachAttribute(myObj, attName, atts.getValue(i));
		}
	}

	private <E, A extends E> void processEachAttribute(PendingObject<?, ?> parentObj, String attName, String attValue) {
		PendingObject<E, A> childObj = parentObj.addChild(attName, false);
		if (childObj == null) {
			throw new IllegalArgumentException("Child attribute "+attName+" could not be created for object "+parentObj.actualType);
		}
		try {
			childObj.value = StringSerializer.valueOf(attValue, childObj.actualType.rawType);
			afterObjectRead(childObj);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse attribute "+parentObj.actualType+"."+attName+ "'s value: "+attValue,e);
		}
	}
	
	@Override
	public void endElement(final String qName) {
		PendingObject<Object, Object> myPo = pendingObjects.pop();
		if (myPo.id != null && read.containsKey(myPo.id)) {
			myPo.value = read.get(myPo.id);
			return;
		}
		if (NULL_ELEM_NAME.equals(qName) && myPo.isEmpty()) {
			myPo.value = null;
			afterObjectRead(myPo);
			return;
		}
		if (StringSerializer.canStore(myPo.actualType.rawType) && myPo.isEmpty()) {
			try {
				myPo.value = valueOf(pendingChars.toString(), myPo.actualType.rawType);
				afterObjectRead(myPo);
				return;
			} catch (Exception e) {
				//Will fallback to map
			}
		}
		
		
		final MapTransformer<Object> myTr = MapTransformer.getFor(myPo.expectedType.rawType);
		if (myPo.value == null || !myPo.expectedType.rawType.isInstance(myPo.value)) {
			myPo.value = myTr.createParentOrSuper(myPo.actualType, myPo.expectedType);
		}
		myPo.value = myTr.setChildren(myPo.value, myPo.expectedType, myPo.childValues);
		afterObjectRead(myPo);
	}

	private void afterObjectRead(PendingObject<?, ?> myPo) {
		if (myPo.value instanceof NeedsUpdateAfterDeserialization) {
			((NeedsUpdateAfterDeserialization)myPo.value).updateAfterDeserialization();
		}
		if (doIDs && myPo.id != null) {
			read.put(myPo.id, myPo.value);
		}
	}

	private String resolveId(String idAttVal) {
		return doIDs ? idAttVal : String.valueOf(curId++);
	}

	protected Object getRoot() {
		return rootObject.value;
	}
}