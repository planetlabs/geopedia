package com.sinergise.common.util.state.gwt;

import static com.sinergise.common.util.Util.isTrue;

import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;
import com.sinergise.common.util.event.selection.ToggleListenerCollection;

public abstract class PropertyChangeListenerAdapter<T> implements PropertyChangeListener<Object> {

	private final DefaultTypeSafeKey<T> propertyKey;
	
	public PropertyChangeListenerAdapter(DefaultTypeSafeKey<T> propertyKey) {
		this.propertyKey = propertyKey;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
		if (propertyKey.getKeyName().equals(propertyName)) {
			propertyChange(sender, (T)oldValue, (T)newValue);
		}
	}
	
	public abstract void propertyChange(Object sender, T oldValue, T newValue);
	
	
	
	public static class PropertyChangeListenerToggleAdapter extends PropertyChangeListenerAdapter<Boolean> 
		implements SourcesToggleEvents 
	{
		
		private final ToggleListenerCollection listeners = new ToggleListenerCollection();

		public PropertyChangeListenerToggleAdapter(DefaultTypeSafeKey<Boolean> propertyKey) {
			super(propertyKey);
		}
		
		public PropertyChangeListenerToggleAdapter(DefaultTypeSafeKey<Boolean> propertyKey, ToggleListener toggleListener) {
			this(propertyKey);
			addToggleListener(toggleListener);
		}
		
		@Override
		public void addToggleListener(ToggleListener l) {
			listeners.add(l);
		}
		
		@Override
		public void removeToggleListener(ToggleListener l) {
			listeners.remove(l);
		}
		
		@Override
		public void propertyChange(Object sender, Boolean oldValue, Boolean newValue) {
			listeners.fireActionPerformed(this, isTrue(oldValue), isTrue(newValue));
		}
		
		public static PropertyChangeListenerToggleAdapter bind(SourcesPropertyChangeEvents<?> source, 
			DefaultTypeSafeKey<Boolean> propertyKey, ToggleListener listener) 
		{
			PropertyChangeListenerToggleAdapter adapter = new PropertyChangeListenerToggleAdapter(propertyKey, listener);
			source.addPropertyChangeListener(adapter);
			return adapter;
		}
	}

}
