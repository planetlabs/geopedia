package com.sinergise.common.gis.map.shapes.snap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SelectableImpl;
import com.sinergise.common.util.geom.HasCoordinate;

public class ChainedSnapProvider implements SnapProvider {
	
	private List<SnapProvider> providers = new ArrayList<SnapProvider>();
	
	private final Selectable enabled = new SelectableImpl(true);
	
	@Override
	public Selectable getEnabled() {
		return enabled;
	}
	
	public void registerSnapProvider(SnapProvider provider) {
		providers.add(provider);
	}
	
	public void registerSnapProvider(int index, SnapProvider provider) {
		providers.add(index, provider);
	}

	@Override
	public void snapPoint(HasCoordinate point, final SnapProviderCallback callback) {
		snapWithNextProvider(providers.iterator(), point, callback);
	}
	
	private void snapWithNextProvider(final Iterator<SnapProvider> providerIter, HasCoordinate point, final SnapProviderCallback callback) {
		if (!providerIter.hasNext()) {
			return;
		}
		
		SnapProvider provider = providerIter.next();
		if (!provider.getEnabled().isSelected()) {
			snapWithNextProvider(providerIter, point, callback);
			return;
		}
		
		provider.snapPoint(point, new SnapProviderCallback() {
			
			@Override
			public void onPointSnapped(HasCoordinate p, HasCoordinate snapLocation) {
				callback.onPointSnapped(p, snapLocation);
			}
			
			@Override
			public void onPointNotSnapped(HasCoordinate p) {
				snapWithNextProvider(providerIter, p, callback);
			}
		});
		
	}

}
