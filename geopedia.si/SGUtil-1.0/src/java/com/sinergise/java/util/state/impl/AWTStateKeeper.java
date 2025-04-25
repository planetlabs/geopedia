/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package com.sinergise.java.util.state.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.sinergise.java.util.state.State;
import com.sinergise.java.util.state.StateFactory;
import com.sinergise.java.util.state.StateKeeper;
import com.sinergise.java.util.state.StateOriginator;
import com.sinergise.java.util.state.StateStorage;

/**
 * This class is an implementation of the <code>StateKeeper</code> interface and stores application state by traversing through its AWT
 * tree. Each component in the tree which implements <code>StateOriginator</code> is called to create a <code>State</code> object in the
 * process of the aplication state saving. The created <code>State</code> will be marked with the full path to the component so later it can
 * be delivered to it. <br/>
 * If the AWT hierarchy of the components at the time of restoring the state is different than at the time of saving, the state keeper might
 * not be able to deliver data to the <code>StateOriginator</code> these cases should be handled separately inside the application.
 * 
 * @author dvitas
 */
public class AWTStateKeeper implements StateKeeper {
	protected static final char   pathSeparator      = '/';
	protected static final char   indexSeparator     = '#';
	protected static final String tokenizerDelimiter = "" + pathSeparator + indexSeparator;
	protected Container           rootContainer      = null;
	
	/**
		 *
		 */
	public AWTStateKeeper(final Container rootContainer) {
		super();
		this.rootContainer = rootContainer;
	}
	
	protected void fillState(final Container container, String path, final HashMap<String, Integer> paths, final HashMap<String, Integer> indexes,
	                         final List<State> states) {
		path = path + pathSeparator + container.getClass().getName();
		
		Integer index = paths.get(path);
		
		if (index == null) {
			index = new Integer(0);
		} else {
			index = new Integer(index.intValue() + 1);
		}
		
		paths.put(path, index);
		path = path + indexSeparator + index.intValue();
		
		// System.out.println(">>> Querying '"+container.getName()+"' '"+path+"' '"+container+"'");
		final Component[] components = container.getComponents();
		// System.out.println(">>> Component returned "+components.length);
		
		for (final Component component : components) {
			if (component instanceof StateOriginator) {
				final StateOriginator so = (StateOriginator)component;
				final State s = so.getState();
				
				if (s != null) {
					String id = path + pathSeparator + component.getClass().getName();
					index = indexes.get(id);
					
					if (index == null) {
						index = new Integer(0);
					} else {
						index = new Integer(index.intValue() + 1);
					}
					
					indexes.put(id, index);
					id = id + indexSeparator + index.intValue();
					s.setID(id);
					states.add(s);
				}
			}
			
			if (component instanceof Container) {
				fillState((Container)component, path, paths, indexes, states);
			}
		}
	}
	
	protected Object getObject(final Container container, final String name, final int index) {
		int hitCount = -1;
		final Component[] components = container.getComponents();
		
		for (final Component component : components) {
			if (component.getClass().getName().equals(name)) {
				hitCount++;
			}
			
			if (hitCount == index) {
				return component;
			}
		}
		
		return null;
	}
	
	protected StateOriginator getOriginator(final Container root, final String ID) {
		int index;
		
		final StringTokenizer st = new StringTokenizer(ID, tokenizerDelimiter);
		Container container = root;
		
		// check if this tree belongs to us
		if (st.hasMoreTokens()) {
			if (!root.getClass().getName().equals(st.nextToken())) {
				return null;
			}
			
			st.nextToken(); // skip index
		}
		
		Object object = null;
		
		// go through the path to the component
		while (st.hasMoreTokens()) {
			final String name = st.nextToken();
			index = Integer.parseInt(st.nextToken());
			object = getObject(container, name, index);
			
			if (object == null) {
				return null;
			}
			
			if (object instanceof Container) {
				container = (Container)object;
			}
		}
		
		if (!(object instanceof StateOriginator)) {
			return null;
		}
		
		return (StateOriginator)object;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateKeeper#restore()
	 */
	@Override
	public void restore(final String filePath, final String applicationName) throws IOException {
		if (rootContainer == null) {
			throw new IOException("Unable to restore state. The rootContainer is null!");
		}
		
		final StateStorage ss = StateFactory.createStateStorage(filePath, applicationName);
		ss.load(filePath, applicationName);
		final List<State> states = ss.getStates();
		setStates(states);
	}
	
	protected void setStates(final List<State> states) {
		// Do root first so that others get the right treatment
		int extSt = Frame.NORMAL;
		
		State curState;
		String ID;
		for (int i = 0; i < states.size(); i++) {
			curState = states.get(i);
			ID = curState.getID();
			
			if (ID.equals("RootContainer")) {
				final Rectangle bounds = curState.getRectangle("Bounds");
				rootContainer.setBounds(bounds);
				
				// Handle maximized state
				if (rootContainer instanceof Frame) {
					extSt = curState.getInt("extendedState", Frame.NORMAL);
					((Frame)rootContainer).setExtendedState(extSt);
					((Frame)rootContainer).validate();
					((Frame)rootContainer).doLayout();
				}
				states.remove(i);
				break;
			}
		}
		
		for (int i = 0; i < states.size(); i++) {
			curState = states.get(i);
			ID = curState.getID();
			
			final StateOriginator originator = getOriginator(rootContainer, ID);
			
			if (originator != null) {
				originator.setState(curState);
			}
		}
	}
	
	protected List<State> getStates() {
		final List<State> states = new ArrayList<State>();
		// add a state for the rootContainer no matter if it is StateOriginator or not
		final State state = StateFactory.createState();
		state.setID("RootContainer");
		state.putRectangle("Bounds", rootContainer.getBounds());
		
		// in these maps we will keep track of components of the same class in a container
		final HashMap<String, Integer> indexes = new HashMap<String, Integer>();
		final HashMap<String, Integer> paths = new HashMap<String, Integer>();
		fillState(rootContainer, "", paths, indexes, states);
		
		// Check extended state
		// MUST COME IN THE END TO HANDLE LAYOUT PROPERLY!!!
		if (rootContainer instanceof Frame) {
			final int extSt = ((Frame)rootContainer).getExtendedState();
			state.putInt("extendedState", extSt);
			if (extSt != Frame.NORMAL) { // Get normal bounds
				((Frame)rootContainer).setExtendedState(Frame.NORMAL);
				((Frame)rootContainer).validate();
				((Frame)rootContainer).doLayout();
				state.putRectangle("Bounds", rootContainer.getBounds());
			}
		}
		
		states.add(state);
		return states;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cosylab.application.state.StateKeeper#save()
	 */
	@Override
	public void save(final String filePath, final String applicationName) throws IOException {
		if (rootContainer == null) {
			throw new IOException("Unable to save state. The rootContainer is null!");
		}
		
		final StateStorage ss = StateFactory.createStateStorage();
		ss.getStates().addAll(getStates());
		ss.store(filePath, applicationName);
	}
	
}

/* __oOo__ */
