package com.sinergise.generics.gwt.core;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.gwt.widgets.NoticeableWidgetWrapper;

/**
 * Creation resolver is a special creation type of creation listener/provider. 
 * It is used to respond to all registered creation listeners,
 * only when all registered creation providers have responded
 * with creation completed call.
 * 
 * To prevent race conditions and to ensure, that creation completed is actually 
 * called at the end. Call begin() method of this class before adding any IsCreationProvider
 * interfaces and done(), after the last interface is added.
 *  
 * @author pkolaric
 *
 */
public class CreationResolver implements IsCreationProvider, CreationListener{

	IsCreationProvider creationToResolve;
	private List<IsCreationProvider> providers = new ArrayList<IsCreationProvider>();
	private List<CreationListener> creationListeners = new ArrayList<CreationListener>();
	private InternalCreationProvider internalProvider = null;
	
	
	private class InternalCreationProvider implements IsCreationProvider {
		private boolean done = false;
		private CreationListener listener;
		
		@Override
		public boolean isCreated() {
			return done;
		}
		
		public void done() {
			if (done)
				return;
			done=true;
			if (listener!=null) {
				listener.creationCompleted(InternalCreationProvider.this);
			}
			
		}
		
		@Override
		public void addCreationListener(CreationListener l) {
			listener=l;
		}
	};
	

	public CreationResolver (IsCreationProvider cProvider) {
		creationToResolve = cProvider;
	}
	public CreationResolver() {
		creationToResolve = this;
	}
	
	public void addCreationProvider(IsCreationProvider provider) {
		if (provider.isCreated()) // might be created already, skip it
			return;
		providers.add(provider);
		provider.addCreationListener(this);
	}
	
	public void begin() {
		internalProvider = new InternalCreationProvider();
		addCreationProvider(internalProvider);
	}
	
	public void done() {
		if (internalProvider!=null)
			internalProvider.done();
	}
	
	
	@Override
	public void addCreationListener(CreationListener l) { 
		if (l!=null)
			creationListeners.add(l);		
	}
	
	private void fireCreationCompleted() {
		for (CreationListener l:creationListeners) {
			l.creationCompleted(creationToResolve);
		}
	}

	
	@Override
	public boolean isCreated() {
		if (providers.size()==0)
			return true;
		return false;
	}
	
	
	@Override
	public void creationCompleted(IsCreationProvider cp) {
		if (providers.contains(cp)) {
			providers.remove(cp);
			if (providers.size()==0) {
				fireCreationCompleted();
			}
		
		}
		// silently ignore if creation completed is called twice (widgets shouldn't advertise creation twice anyway)
		// TODO: enable exception at development time only (for debugging)
	}
	
	public boolean addIfCreationProvider(Widget w) {
		Widget possibleCreationProvider = w;
		if (w instanceof NoticeableWidgetWrapper<?>) {
			possibleCreationProvider = ((NoticeableWidgetWrapper<?>)w).getWrappedWidget();
		}
		if (possibleCreationProvider instanceof IsCreationProvider) {
			addCreationProvider((IsCreationProvider)possibleCreationProvider);
			return true;
		}
		return false;
	}
	
}
