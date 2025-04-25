package com.sinergise.geopedia.core.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sinergise.common.util.state.gwt.StateGWT;





/**
 * TODO: recheck!!!
 * 
 * @author amarolt
 * 
 * LookupField represents the opposite side of a many-2-one or many-2-many relation between tables in database .
 * LookupField can be used for displaying manyLinks from some feature.
 * Example:
 * Display a list of banks by zipcode;
 */
public class LookupField implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	public String name;
    public static final String PREFIX = "L";
    private static final int NULL_VALUE = 0;
    
    public static final String QUERY_TABLE = "queryTable";
    
    public static final String SOURCE_REFERENCE = "sourceReference";
    public static final String TARGET_REFERENCE = "targetReference";
    
    public static final String LABEL = "label";
    public static final String DEFAULT_LABEL = "";
    
    public int sourceTableId;
    public Integer queryTableId;
    public int targetTableId;
    
    public Integer sourceReference;
    public Integer targetReference;
    
    private LookupField() {}
    
    /**
     * Read table properties to find if it has many-2-one or many-to-many relation with lookupTable
     * @param sourceTable
     * @param targetTableId - some layer that has one-2-many or many-2-many relation with table
     */
    public LookupField(Table sourceTable, int targetTableId) {
        this.targetTableId = targetTableId;
        
        if (sourceTable==null || sourceTable.properties==null) { return; }
        this.sourceTableId = sourceTable.id;
        
        StateGWT manyLinks = sourceTable.properties.getState(Table.PROP_MANY_LINKS);
        if (manyLinks==null) { return; }
        
        StateGWT lookup = manyLinks.getState(key(targetTableId));
        if (lookup == null) { return; }
        {
            int i = lookup.getInt(QUERY_TABLE, NULL_VALUE);
            if (i == NULL_VALUE) { return; } else {
                this.queryTableId = i;
            }
        }
        {
            int i = lookup.getInt(SOURCE_REFERENCE, NULL_VALUE);
            if (i == NULL_VALUE) { return; } else {
                this.sourceReference = i;
            }
        }
        {
            int i = lookup.getInt(TARGET_REFERENCE, NULL_VALUE);
            if (i == NULL_VALUE) { return; } else {
                this.targetReference = i;
            }
        }
    }
    
    public LookupField(int sourceTableId, int queryTableId, int targetTableId, int sourceReference, int targetReference) {
        this.sourceTableId = sourceTableId;
        this.queryTableId = queryTableId;
        this.targetTableId = targetTableId;
        this.sourceReference = sourceReference;
        this.targetReference = targetReference;
    }
    
    public LookupField(int sourceTableId, int queryTableId, int targetTableId, int sourceReference) {
        this(sourceTableId, queryTableId, targetTableId, sourceReference, NULL_VALUE);
    }
    
    public boolean isValid() {
        return name!=null && sourceTableId!=NULL_VALUE && queryTableId!=null && targetTableId!=NULL_VALUE && sourceReference!=null;
        // targetReference can be null
    }
    
    public static String key(int targetTableId) {
        return PREFIX + targetTableId;
    }
    public static int targetTableId(String targetKey) {
        if (targetKey != null && targetKey.startsWith(PREFIX)) {
            String targetTableIdString = targetKey.substring(PREFIX.length());
            try {
                return new Integer(targetTableIdString).intValue();
            } catch (NumberFormatException e) {}
        }
        return NULL_VALUE;
    }
    
    /**
     * @param sourceTable - some table which properties might contain lookup spec.
     * @return - zero or more "LookupField"s
     */
    public static LookupField[] get(Table sourceTable) {
        
        if (sourceTable==null || sourceTable.properties==null) { return null; }
        
        StateGWT manyLinks = sourceTable.properties.getState(Table.PROP_MANY_LINKS);
//        State[] manyLinks = sourceTable.properties.getStates(Table.PROP_MANY_LINKS);
        if (manyLinks==null) { return null; }
        {
            Iterator<String> iterator = manyLinks.childKeyIterator();
            if (iterator == null) { return null; }
            
            List<LookupField> list = new ArrayList<LookupField>();
            
            while (iterator.hasNext()) {
                String key = iterator.next();
                StateGWT lookup = manyLinks.getState(key);
                if (lookup == null) { continue; }
                
                LookupField instance = new LookupField();
                instance.name = lookup.getString(LABEL, DEFAULT_LABEL);
                instance.sourceTableId = sourceTable.id;
                {
                    int i = lookup.getInt(QUERY_TABLE, NULL_VALUE);
                    if (i == NULL_VALUE) { continue; } else {
                        instance.queryTableId = i;
                    }
                }
                instance.targetTableId = targetTableId(key);
                {
                    int i = lookup.getInt(SOURCE_REFERENCE, NULL_VALUE);
                    if (i == NULL_VALUE) { continue; } else {
                        instance.sourceReference = i;
                    }
                }
                {
                    int i = lookup.getInt(TARGET_REFERENCE, NULL_VALUE);
                    if (i == NULL_VALUE) { instance.targetReference = null; } else {
                        instance.targetReference = i;
                    }
                }
                list.add(instance);
            }
            return list.toArray(new LookupField[]{});
        }
    }
    
    public static StateGWT manyLinks(LookupField[] list) {
        
        if (list==null || list.length==0) { return null; }
        
        StateGWT manyLinks = new StateGWT();
        
        for (LookupField instance : list) {
            if (instance==null || (! instance.isValid())) { continue; }
            StateGWT lookup = new StateGWT();
            lookup.putString(LABEL, instance.name);
            lookup.putInt(QUERY_TABLE, instance.queryTableId);
            lookup.putInt(SOURCE_REFERENCE, instance.sourceReference);
            if (instance.targetReference!=NULL_VALUE) {
                lookup.putInt(TARGET_REFERENCE, instance.targetReference);
            }
            manyLinks.putState(key(instance.targetTableId), lookup);
        }
        return manyLinks;
    }
    
    /**
     * 
<?xml version="1.0" encoding="UTF-8"?><states><state><manyLinks><L2988 sourceReference="7229" label="Vrste" targetReference="7230" queryTable="3222"/></manyLinks></state></states>

<?xml version="1.0" encoding="UTF-8"?>
<states>
    <state>
        <manyLinks>
            <L2988 label="Vrste" sourceReference="7229" queryTable="3222" targetReference="7230"/>
        </manyLinks>
    </state>
</states>

     * 
     * @param list
     * @return
     */
    public static StateGWT properties(LookupField[] list) {
        
    	StateGWT manyLinks = manyLinks(list);
        if (manyLinks==null) { return null; }
        
        StateGWT properties = new StateGWT();
        properties.putState(Table.PROP_MANY_LINKS, manyLinks);
        return properties;
    }
}
