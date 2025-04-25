/*
 * (c) Infoterra Limited 2004.  The contents of this file are under the
 * copyright of Infoterra.  All rights reserved.  This file must not be
 * copied, reproduced or distributed in wholly or in part or used for
 * purposes other than for that for which it has been supplied without
 * the prior written permission of Infoterra.
 */

package com.sinergise.java.swing.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;


/**
 * Convenience class with set of utilities to help work with Action instances.
 * Supports enhencments to Action framework by Abeans.
 *
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 * @author <a href="mailto:igor.kriznar@cosylab.com">Igor Kriznar</a>
 * @version $id$
 */
public class Actions extends AbstractAction
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private static final String VISIBLE = "visible"; // Do not externalize - just key
    private static final String SELECTED = "selected"; // Do not externalize - just key

    /** Property name for large icon property */
    public static final String LARGE_ICON = "LargeIcon"; // Do not externalize - just key

    /**
     * Creates an appropriate button for an action (JToggleButton, JButton,
     * ...)
     *
     * @param action The source action for the target button
     *
     * @return The created button with the proper Action, Icon, Text, ToolTip
     */
    public static AbstractButton createButton(Action action, boolean showText, boolean largeIcon)
    {
        AbstractButton ret=createButton(action);
        return fixButton(ret, showText, largeIcon);
    }
    
    /**
     * Creates an appropriate button for an action (JToggleButton, JButton,
     * ...)
     *
     * @param action The source action for the target button
     *
     * @return The created button with the proper Action, Icon, Text, ToolTip
     */
    public static AbstractButton createButton(Action action)
    {
        if (action instanceof ToggleAction)
        {
            final JToggleButton btn = new JToggleButton(action);
            btn.setSelected(((ToggleAction) action).isSelected());

            if (action.getValue(ToggleAction.SMALL_SELECTED_ICON) != null)
            {
                btn.setSelectedIcon((Icon) action.getValue(
                        ToggleAction.SMALL_SELECTED_ICON));
            }

            action.addPropertyChangeListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        if (SELECTED.equals(evt.getPropertyName()))
                        {
                            btn.setSelected(((Boolean) evt.getNewValue())
                                .booleanValue());
                        }
                        else if (VISIBLE.equals(evt.getPropertyName()))
                        {
                            btn.setVisible(((Boolean) evt.getNewValue())
                                .booleanValue());
                        }
                    }
                });

            return btn;
        }

        return new JButton(action) {
            /**
					 * 
					 */
					private static final long serialVersionUID = 1L;
						private ComponentListener cl=null;
            @Override
			public void addNotify() {
                super.addNotify();
                final JButton but=this;
                cl=new ComponentAdapter() {
                    @Override
					public void componentHidden(ComponentEvent e) {
                        processMouseEvent(new MouseEvent(but,MouseEvent.MOUSE_EXITED,System.currentTimeMillis(),
                                MouseEvent.NOBUTTON,-10,-10,0,false));
                    }
                };
                getParent().addComponentListener(cl);
            }
            @Override
			public void removeNotify() {
                processMouseEvent(new MouseEvent(this,MouseEvent.MOUSE_EXITED,System.currentTimeMillis(),
                        MouseEvent.NOBUTTON,-10,-10,0,false));
                if (cl!=null) {
                    getParent().removeComponentListener(cl);
                    cl=null;
                }
                super.removeNotify();
            }
        };
    }


    /**
     * Creates an appropriate menu item for the provided action,  with icon,
     * text, mnemonic, shortcut, ...
     *
     * @param action The source action
     *
     * @return The created menu item
     */
    public static JMenuItem createMenuItem(Action action)
    {
        return createMenuItem(action, false);
    }

    
    public static final AbstractButton fixButton(AbstractButton but, boolean showText, boolean largeIcon) {
        Action act = but.getAction();

        if (showText && (act.getValue(Action.NAME) != null))
        {
            but.setText((String) act.getValue(Action.NAME));
        }
        else
        {
            but.setText(null);
        }

        if ((act != null) && largeIcon
            && (act.getValue(Actions.LARGE_ICON) != null))
        {
            but.setIcon((Icon) act.getValue(Actions.LARGE_ICON));
        }
        else if ((act != null)
            && (act.getValue(Action.SMALL_ICON) != null))
        {
            but.setIcon((Icon) act.getValue(Action.SMALL_ICON));
        }
        else
        {
            but.setIcon(null);
        }
        return but;
    }

    public static final void fixToolBar(JToolBar bar, boolean showText,
            boolean largeIcon)
        {
            Component[] c = bar.getComponents();
            for (int i = 0; i < c.length; i++)
            {
                if (c[i] instanceof AbstractButton)
                {
                    fixButton((AbstractButton)c[i], showText, largeIcon);
                }
            }
        }

    
    /**
     * Creates a menu item for the specified action, optionally with a
     * radioButton.
     *
     * @param action The source action
     * @param radio Whether to create a radio button
     *
     * @return The created menu item.
     */
    public static JMenuItem createMenuItem(Action action, boolean radio)
    {
        if (action instanceof ToggleAction)
        {
            if (radio)
            {
                final JRadioButtonMenuItem itm = new JRadioButtonMenuItem(action);
                itm.setIcon(null);
                itm.setSelected(((ToggleAction) action).isSelected());
                action.addPropertyChangeListener(new PropertyChangeListener()
                    {
                        public void propertyChange(PropertyChangeEvent evt)
                        {
                            if (SELECTED.equals(evt.getPropertyName()))
                            {
                                itm.setSelected(((Boolean) evt.getNewValue())
                                    .booleanValue());
                            }
                        }
                    });

                return itm;
            }

            final JCheckBoxMenuItem itm = new JCheckBoxMenuItem(action);
            itm.setIcon(null);
            itm.setSelected(((ToggleAction) action).isSelected());
            action.addPropertyChangeListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        if (SELECTED.equals(evt.getPropertyName()))
                        {
                            itm.setSelected(((Boolean) evt.getNewValue())
                                .booleanValue());
                        }
                    }
                });

            return itm;
        }
        else if (radio)
        {
            JRadioButtonMenuItem itm = new JRadioButtonMenuItem(action);

            return itm;
        }

        JMenuItem itm = new JMenuItem(action);

        return itm;
    }


    static void updateMenuBar(Action[] a, JMenuBar bar)
    {
        bar.removeAll();

        for (int i = 0; i < a.length; i++)
        {
            bar.add(createMenuItem(a[i]));
        }
    }
    private Actions() {
    }
    public void actionPerformed(ActionEvent e) {
    }
}

/* __oOo__ */
