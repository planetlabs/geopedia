/*
 *
 */
package com.sinergise.common.ui.action;

import com.google.gwt.resources.client.ImageResource;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;
import com.sinergise.common.util.event.selection.ToggleListenerCollection;


public abstract class ToggleAction extends Action implements Selectable {
    public static class Inverse extends ToggleAction implements ToggleListener {
    	final ToggleAction src;
    	public Inverse(String name, ToggleAction source) {
    		super(name);
    		this.src = source;
    		setSelected(!src.isSelected());
    		src.addToggleListener(this);
		}
    	
    	public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
    		if (source == src) {
    			setSelected(!newOn);
    		}
    	}
    	
    	@Override
    	protected void selectionChanged(boolean newSelected) {
    		src.setSelected(!newSelected);
    	}
	}
    
    public static ToggleAction createInverse(ToggleAction source) {
    	if (source instanceof Inverse) {
    		return ((Inverse)source).src;
    	}
    	return new Inverse("!"+source.getName(), source);
    }

	public static final String PROP_SELECTABLE="selectableActionType";
    public static final String PROP_SELECTED="selected";
    /**
     * @deprecated Use {@link #PROP_SELECTED_ICON_RES}
     */
    @Deprecated
	public static final String PROP_SELECTED_ICON="selectedIcon";
    
    public static final String PROP_SELECTED_ICON_RES="selectedIconResource";
    
    private ToggleListenerCollection tlc;
    
    public ToggleAction(String name) {
        super(name);
        setProperty(PROP_SELECTABLE, Boolean.TRUE);
    }
    
    public void addToggleListener(ToggleListener l) {
        if (tlc==null) tlc=new ToggleListenerCollection();
        tlc.add(l);
    }
    public void removeToggleListener(ToggleListener l) {
        if (tlc==null) return;
        tlc.add(l);
    }
    
    private int block=0; 
    
    public void setSelected(boolean sel) {
        boolean oldSel = isSelected();
		if (sel != oldSel) {
            if (block==0) {
                actionPerformed();
            } else {
            	//Set all the internals first
                final Object newValue = sel ? "true" : "false";
				final Object oldValue = silentSetProperty(PROP_SELECTED, newValue);
                selectionChanged(sel);
                //Maybe internal reversed the decision
                if (sel != isSelected()) return;

                //Now notify all listeners
               	firePropertyChange(PROP_SELECTED, oldValue, newValue);
               	//Maybe property change reversed the decision
               	if (sel != isSelected()) return;

               	if (tlc!=null) tlc.fireActionPerformed(this, oldSel, sel);
            }
        }
    }
    
    @Override
	public ToggleAction setProperty(String name, Object value) {
        if (PROP_SELECTED.equals(name)) {
        	setSelected(toBoolean(value));
        } else {
            super.setProperty(name, value);
        }
        return this;
    }

	private static boolean toBoolean(Object oldValue) {
		if (oldValue == null) {
			return false;
		}
		return "true".equalsIgnoreCase(oldValue.toString());
	}
    
    public boolean isSelected() {
        return isSelected(this);
    }
    
    @Override
	protected final void actionPerformed() {
        block++;
        try {
            setSelected(!isSelected());
        } finally {
            block--;
        }
    }

    /**
     * Override this method to provide custom event handling
     * when selection changes.
     *
     * @param newSelected
     */
    protected abstract void selectionChanged(boolean newSelected);

    public static boolean isSelected(Action act) {
        return toBoolean(act.getProperty(PROP_SELECTED));
    }

    public static String getSelectedIcon(Action act) {
        Object selIco=act.getProperty(PROP_SELECTED_ICON);
        if (selIco==null) selIco=act.getIcon();
        if (selIco==null) return null;
        return selIco.toString();
    }

	public static void setSelected(Action act, boolean sel) {
		act.setProperty(PROP_SELECTED, sel?"true":"false");
	}

	public static ImageResource getSelectedIconResource(Action act) {
		ImageResource ret = (ImageResource) act.getProperty(PROP_SELECTED_ICON_RES);
		if (ret == null) ret = act.getIconResource();
		return ret;
	}
}
