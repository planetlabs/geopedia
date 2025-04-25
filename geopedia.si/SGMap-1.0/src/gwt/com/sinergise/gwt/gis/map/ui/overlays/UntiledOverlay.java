package com.sinergise.gwt.gis.map.ui.overlays;


import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.ui.ImageUtilGWT.ImageLoadListener;


public abstract class UntiledOverlay extends OverlayComponent<UntiledRenderInfo> implements
		ImageLoadListener {
	public static int TIMEOUT = 1000;

	public abstract static class RenderedImage {
		public final UntiledRenderInfo info;
		private transient EnvelopeI curRect;
		protected final ImageLoadListener loadListener;

		public RenderedImage(UntiledRenderInfo info, ImageLoadListener l) {
			this.info = info;
			this.loadListener = l;
		}

		public void updateImagePosition(DisplayCoordinateAdapter curDCA) {
			EnvelopeI tempRect = curDCA.pixFromWorld.rectInt(info.worldBounds);
			if (tempRect.equals(curRect)) {
				return;
			}
			curRect = tempRect;
			double scale = (double) curRect.getWidth() / info.imageNaturalSize.w();
			if (scale > 0.1 && scale < 10) {
				positionImage(curRect);
			} else {
				hideImage();
			}
		}

		public abstract void initialize();

		public abstract void positionImage(EnvelopeI rect);

		public abstract void hideImage();

		public boolean isTransparent(DisplayCoordinateAdapter dca) {
			if (curRect == null || info.isTransparent) {
				return true;
			}
			return !(curRect.contains(0, 0) && curRect.contains(dca.getDisplayWidth() - 1, dca.getDisplayHeight() - 1));
		}

		public abstract void destroy();
	}

	UntiledRenderInfo myInfo;

	RenderedImage lastLoaded;
	RenderedImage curPending;

	Timer loadTimer = new Timer() {
		@Override
		public void run() {
			onLoad(curPending);
		}
	};

	@Override
	public UntiledRenderInfo prepareToRender(DisplayCoordinateAdapter dca, boolean trans, boolean quick) {
		if (myInfo == null) myInfo = new UntiledRenderInfo();
		myInfo.quick = quick;
		myInfo.dca = dca;
		myInfo.worldBounds = dca.worldRect;
		myInfo.isTransparent = trans;
		prepareInfo(myInfo);
		if (!quick) {
			if (lastLoaded != null && lastLoaded.info.equals(myInfo)) {
				// System.out.println("SAME AS LAST");
				myInfo.quick = true;
			} else if (curPending != null && curPending.info.equals(myInfo)) {
				// System.out.println("SAME AS PENDING");
				myInfo.quick = true;
			}
		}
		if (myInfo.quick && myInfo.hasAnything) {
			if (lastLoaded == null && curPending == null)
				myInfo.hasAnything = false;
			else {
				myInfo.hasAnything = (lastLoaded != null && lastLoaded.info.worldBounds.intersects(dca.worldRect))
					|| (curPending != null && curPending.info.worldBounds.intersects(dca.worldRect));
				myInfo.isTransparent = (lastLoaded != null && lastLoaded.isTransparent(dca))
					|| (curPending != null && curPending.isTransparent(dca));
			}
		}
		return myInfo;
	}

	public abstract void addImageToDOM(RenderedImage image);

	public abstract void removeImageFromDOM(RenderedImage image);

	public abstract void prepareInfo(UntiledRenderInfo ret);

	@Override
	public void reposition(UntiledRenderInfo info) {
		if (!info.quick) {
			redraw(info);
			myInfo = null;
		} else {
			refresh(info);
		}
	}

	protected abstract RenderedImage createImage(UntiledRenderInfo info,
			ImageLoadListener l);

	protected void redraw(UntiledRenderInfo info) {
		if (curPending != null) {
			destroy(curPending, true);
		}
		refresh(info);
		UntiledRenderInfo copy = info.createCopy();
		curPending = createImage(copy, this);
		if (curPending != null) {
			loadTimer.schedule(TIMEOUT);
			curPending.initialize();
			if (curPending != null) {// Must have been swapped already
				addImageToDOM(curPending);
				curPending.positionImage(new EnvelopeI(-10, -10, -1, -1));
			}
		}
	}

	@Override
	public void onLoad(Object image) {
		loadTimer.cancel();
		swapImages();
	}

	@Override
	public void onError(Object image) {
		loadTimer.cancel();
		System.out.println("ERROR");
		destroy(curPending, true);
		curPending = null;
	}

	protected final void destroy(final RenderedImage img, boolean async) {
		if (img == null) return;
		Scheduler.ScheduledCommand cmd = new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				removeImageFromDOM(img);
				img.destroy();
			}
		};
		if (async) Scheduler.get().scheduleDeferred(cmd);
		else cmd.execute();
	}

	public void swapImages() {
		if (curPending == null)
			return;
//		System.out.println("LOADED");
		final RenderedImage toDestroy = lastLoaded;
		lastLoaded = curPending;
		curPending = null;
		if (lastLoaded != null) {
			final RenderedImage toAdd = lastLoaded;
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					addImageToDOM(toAdd);
					toAdd.updateImagePosition(toAdd.info.dca);
					if (toDestroy != null && toDestroy != lastLoaded) {
						destroy(toDestroy, false);
					}
				}
			});
		}
	}

	protected void refresh(UntiledRenderInfo rInfo) {
		if (lastLoaded == null)
			return;

		if (rInfo.hasAnything) {
			if (lastLoaded.info.themeEquals(rInfo)) {
				lastLoaded.updateImagePosition(rInfo.dca);
			} else {
				lastLoaded.hideImage();
			}
		} else {
			lastLoaded.hideImage();
		}
	}
}
