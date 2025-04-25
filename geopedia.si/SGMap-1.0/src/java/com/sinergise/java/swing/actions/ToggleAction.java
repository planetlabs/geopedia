/*
 * (c) Infoterra Limited 2004.  The contents of this file are under the
 * copyright of Infoterra.  All rights reserved.  This file must not be
 * copied, reproduced or distributed in wholly or in part or used for
 * purposes other than for that for which it has been supplied without
 * the prior written permission of Infoterra.
 */

package com.sinergise.java.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;


/**
 * An extension of the <code>javax.swing.Action</code> interface which provides
 * <code>selected</code> bound property. It can be used to specify an action
 * which switches between two different states. GUI components will render
 * these actions with check boxes or toggle buttons.
 * 
 * <p>
 * To use this action, either instantiate this class and register as a
 * propertyChangeListener, responding to events that denote a change in
 * "selected" property, or extend this class, overriding actionPerformed()
 * method.
 * </p>
 *
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 * @version $id$
 */
public class ToggleAction extends AbstractAction
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

		private static final String SELECTION_CHANGED = "selection_changed";

    /** Icon used when the action is selected */
    public static final String SMALL_SELECTED_ICON = "SmallSelectedIcon";

    /** Large icon used when the action is selected */
    public static final String LARGE_SELECTED_ICON = "LargeSelectedIcon";
    private boolean internal = false;
    private boolean selected = false;

    /**
     * Constructor with default values.
     */
    public ToggleAction()
    {
        super();
    }


    /**
     * Constructor with name.
     *
     * @param name the name of the action
     */
    public ToggleAction(String name)
    {
        super(name);
    }


    /**
     * Constructor with name and selection flag.
     *
     * @param name the name of the action
     * @param selected the selection flag
     */
    public ToggleAction(String name, boolean selected)
    {
        super(name);
        this.selected = selected;
    }


    /**
     * Contructor with name and icon.
     *
     * @param name
     * @param icon
     */
    public ToggleAction(String name, Icon icon)
    {
        super(name, icon);
    }


    /**
     * Contructor with name, icon and selection flag.
     *
     * @param name the name of action
     * @param icon the icon to be used GUI element
     * @param selected the selection flag
     */
    public ToggleAction(String name, Icon icon, boolean selected)
    {
        super(name, icon);
        this.selected = selected;
    }

    /**
     * Sets the <code>selected</code> property. Fires propertyChange events and
     * calls actionPerformed() method with <code>this</code> as event source.
     *
     * @param selected
     */
    public void setSelected(boolean selected)
    {
        if (this.selected != selected)
        {
            if (!internal)
            {
                actionPerformed(new ActionEvent(this, selected ? 1 : 0,
                        SELECTION_CHANGED));
            }
            else
            {
                internal = false;
                this.selected = selected;
                selectionChanged(selected);
                firePropertyChange("selected", new Boolean(!selected),
                    new Boolean(selected));
            }
        }
    }


    /**
     * Returns the state of this toggle action.
     *
     * @return whether this toggle action is selected.
     */
    public boolean isSelected()
    {
        return selected;
    }


    /**
     * Overriden to modify selection status. Called by the GUI when invoked.
     *
     * @param e event object
     */
    public void actionPerformed(ActionEvent e)
    {
        internal = true;
        setSelected(!selected);
    }


    /**
     * Override this method to provide custom event handling when selection
     * changes.
     *
     * @param sel
     */
    protected void selectionChanged(boolean sel)
    {
//        Debug.out(String.valueOf(sel));
    }
}

/* __oOo__ */
