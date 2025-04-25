package com.sinergise.gwt.ui.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.core.KeyCodes;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.util.html.CSS;
import com.sinergise.gwt.util.html.ExtDOM;

public final class MouseHandler implements MouseDownHandler,
	MouseUpHandler, MouseMoveHandler, MouseOutHandler, MouseOverHandler,
	MouseWheelHandler, ClickHandler, DoubleClickHandler
{ 
	private static final int NOT_SET_I = Integer.MIN_VALUE;
	
	private static final int ALL_EVENTS = Event.ONMOUSEWHEEL | Event.MOUSEEVENTS | Event.ONCLICK
		| Event.ONDBLCLICK | Event.ONKEYUP | Event.ONKEYDOWN | Event.ONKEYPRESS | Event.FOCUSEVENTS
		| Event.ONLOSECAPTURE;
	
	public static final int MOD_NONE = 16;
	
	public static final int MOD_SHIFT = 1;
	
	public static final int MOD_CTRL = 2;
	
	public static final int MOD_ALT = 4;
	
	public static final int MOD_SPACE = 8;
	
	public static final int MOD_ANY = -1;
	
	public static final int BUTTON_NONE = 0;
	public static final int BUTTON_LEFT = NativeEvent.BUTTON_LEFT;
	public static final int BUTTON_MIDDLE = NativeEvent.BUTTON_MIDDLE;
	public static final int BUTTON_RIGHT = NativeEvent.BUTTON_RIGHT;
	public static final int BUTTON_NA = Integer.MIN_VALUE;
	
	public static final int DRAG_DIST_SQ = 25;
	
	public static final int DBL_CLICK_MILLIS = 250;
	public static final int WHEEL_MILLIS = 250;
	
	private static final Logger logger = LoggerFactory.getLogger(MouseHandler.class);
	
	public static class MouseActionBinding<T extends MouseAction> {
		protected final T act;
		protected final int button;
		protected final int modifier;
		protected final int clickCount;

		protected MouseActionBinding(T act, int btn, int mod, int clickCount) {
			this.act = act;
			this.button = btn;
			this.modifier = mod;
			this.clickCount = clickCount;
		}
		
		
		/**
		 * Order: button, mods, clickCnt
		 * 
		 * @param but
		 * @param mods
		 * @param clickCnt
		 * @return
		 */
		public boolean isMoreSpecificThan(int but, int mods, int clickCnt, int currentClickCnt) {
			if (this.button != BUTTON_NA && this.button != but) return true;
			if (mods == 0) mods = MOD_NONE;
			if (this.modifier != MOD_ANY) {
				if (mods == MOD_ANY) return true;
				else if (bitCnt(this.modifier) > bitCnt(mods)) return true;
			}
			
			if (this.clickCount != NOT_SET_I
				&& Math.abs(this.clickCount - currentClickCnt) < Math.abs(clickCnt - currentClickCnt)) return true;
			return false;
		}
		
		private static int bitCnt(int bits) {
			int ret = 0;
			if ((bits & MOD_ALT) > 0) ret++;
			if ((bits & MOD_CTRL) > 0) ret++;
			if ((bits & MOD_SHIFT) > 0) ret++;
			return ret;
		}
		
		public boolean canHandle(int but, int mods, int clickCnt) {
			if (this.button != BUTTON_NA && this.button != but) return false;
			if (mods == 0) mods = MOD_NONE;
			if (modifier != MOD_ANY) {
				if ((modifier & mods) == 0) return false;
			}
			if (this.clickCount != NOT_SET_I && clickCnt < this.clickCount) return false;
			return true;
		}

		public T getAction() {
			return act;
		}

		@SuppressWarnings("unchecked")
		public static <T extends MouseDragAction> MouseActionBinding<T>[] createArr(T act, int buttonLeft, int mods) {
			return new MouseActionBinding[]{create(act, buttonLeft, mods)};
		}
		@SuppressWarnings("unchecked")
		public static <T extends MouseClickAction> MouseActionBinding<T>[] createArr(T act, int buttonLeft, int mods, int clickCount) {
			return new MouseActionBinding[]{create(act, buttonLeft, mods, clickCount)};
		}
		@SuppressWarnings("unchecked")
		public static <T extends MouseAction> MouseActionBinding<T>[] createArr(T act, int mods) {
			return new MouseActionBinding[]{create(act, mods)};
		}

		public static <T extends MouseDragAction> MouseActionBinding<T> create(T act, int button, int mods) {
			return new MouseActionBinding<T>(act, button, mods, NOT_SET_I);
		}

		public static <T extends MouseClickAction> MouseActionBinding<T> create(T act, int button, int mods, int clickCount) {
			return new MouseActionBinding<T>(act, button, mods, clickCount);
		}

		public static <T extends MouseAction> MouseActionBinding<T> create(T act, int mods) {
			return new MouseActionBinding<T>(act, BUTTON_NA, mods, NOT_SET_I);
		}
	}
	
	private static class ModsManager implements NativePreviewHandler, HasValueChangeHandlers<Integer> {
		public int current = 0;
		private HandlerManager hndlrMgr;
		
		public ModsManager() {
			hndlrMgr = new HandlerManager(this);
			Event.addNativePreviewHandler(this);
		}
		
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
			return hndlrMgr.addHandler(ValueChangeEvent.getType(), handler);
		}
		
		public void onPreviewNativeEvent(NativePreviewEvent evt) {
			if (evt == null) return; // Happens sometimes !?!?
			NativeEvent ne = evt.getNativeEvent();
			try {
				int oldMods = current;
				updateMods(evt);
				switch (evt.getTypeInt()) {
					case Event.ONKEYDOWN:
						if (ne.getKeyCode() == KeyCodes.KEY_SPACE && (current & MOD_SPACE) == 0) {
							current |= MOD_SPACE;
						}
						break;
					case Event.ONKEYUP:
						if (ne.getKeyCode() == KeyCodes.KEY_SPACE && (current & MOD_SPACE) != 0) {
							current &= ~MOD_SPACE;
						}
						break;
				}
				fireChanged(oldMods, current);
			} catch (Exception e) {
				// weird exception, should not happen
			}
		}
		
		private void updateMods(NativePreviewEvent evt) {
			NativeEvent ne = evt.getNativeEvent();
			try {
				if (ne.getAltKey()) current |= MOD_ALT;
				else current &= ~MOD_ALT;
				if (ne.getShiftKey()) current |= MOD_SHIFT;
				else current &= ~MOD_SHIFT;
				if (ne.getCtrlKey()) current |= MOD_CTRL;
				else current &= ~MOD_CTRL;
			} catch (Exception e) {
				// WEIRD
			}
		}
		
		public void fireEvent(GwtEvent<?> event) {
			hndlrMgr.fireEvent(event);
		}
		
		protected void fireChanged(int oldVal, int newVal) {
			ValueChangeEvent.fireIfNotEqual(this, Integer.valueOf(oldVal), Integer.valueOf(newVal));
		}
	}
	
	private static ModsManager modifiers = new ModsManager();

	private class WheelRecognizer {
		private int startDocX;
		private int startDocY;
		
		private int wheelCount = NOT_SET_I;
		private int startMods;
		
		private Timer wheelTimer = new Timer() {
			@Override
			public void run() {
				int hX = startDocX;
				int hY = startDocY;
				int hMods = startMods;
				int hCnt = wheelCount;
				gestureEnded();
				wheelMoved(hX, hY, hCnt, hMods);
			}
		};
		
		public boolean wheel(MouseWheelEvent evt, int mods) {
			if (findTarget(wheelActions, BUTTON_NA, mods, NOT_SET_I) == null) {
				return false;
			}
			if (wheelCount == NOT_SET_I) {
				updateParentPosition();
				wheelCount = 0;
				startDocX = getPageX(evt);// ExtDOM.eventGetWheelX(x,curX);
				startDocY = getPageY(evt);// ExtDOM.eventGetWheelX(y,curY);
				startMods = mods;
			} else {
				wheelTimer.cancel();
			}
			wheelPreview(startDocX, startDocY, wheelCount, startMods);
			wheelCount += getWheelDelta(evt);
			wheelTimer.schedule(WHEEL_MILLIS);
			return true;
		}
		
		private void gestureEnded() {
			wheelCount = NOT_SET_I;
			startDocX = -1;
			startDocY = -1;
			startMods = 0;
		}
	}
	
	private class GestureRecognizer {
			private int startX;
			
			private int startY;
			
			private int startBut;
			
			private int startMods;
			
	//		private boolean dragStarted = false;
			
			private int clickCnt = 0;
			
			private MouseActionBinding<? extends MouseAction> downTgt = null;
			// private long startTime;
			
			private Timer clickTimer = new Timer() {
				@Override
				public void run() {
					int hX = startX;
					int hY = startY;
					int hBut = startBut;
					int hMods = startMods;
					int hCnt = clickCnt;
					gestureEnded();
					clicked(hX, hY, hBut, hMods, hCnt);
				}
			};
			
			public boolean down(int x, int y, int but, int mods) {
				downTgt = findDownTarget(but, mods, clickCnt + 1);
				if (downTgt == null) return false;
				if (dragging != null) return true;
				startX = x;
				startY = y;
				startBut = but;
				startMods = mods;
				preventAllDefaults();
				updateCursor(startBut);
				return true;
			}
			
			public boolean move(MouseMoveEvent evt) {
				if (startBut == 0) { return false; }
				if (dragging != null) {
					dragMove(evt);
					return true;
				}
				
				int dist = MathUtil.distSq(startX, startY, getPageX(evt), getPageY(evt));
				if (clickCnt == 0) {
					if (downTgt != null) {
						if (downTgt.act instanceof MouseClickAction) {
							MouseClickAction mca = (MouseClickAction) downTgt.act;
							if (!mca.allowDrag()) return false;
						}
					}
					if (dist > DRAG_DIST_SQ && dragStart(startX, startY, startMods, startBut)) {
						dragMove(evt);
						return true;
					}
					return false;
				} else if (dist > 9) {
					clickTimer.cancel();
					clickTimer.run();
				}
				return true;
			}
			
			public boolean up(MouseEvent<?> evt) {
				if (dragging != null) {
					if (startBut == evt.getNativeButton()) {
						dragging.act.handleMoveScreen(evt, MouseHandler.this);
						dragEnd(evt);
						gestureEnded();
						return true;
					}
					return false;
				} else if (startBut == evt.getNativeButton()) {
					int pageX = getPageX(evt);
					int pageY = getPageY(evt);
					int distSq = MathUtil.distSq(startX, startY, pageX, pageY);
					startX = pageX;
					startY = pageY;
					clickTimer.cancel();
					
					if (distSq < DRAG_DIST_SQ) {
						clickCnt++;
						MouseActionBinding<?> dblTarget = findTarget(clickActions, startBut, startMods, clickCnt + 1);
						if (dblTarget == null || dblTarget == downTgt) {
							clickTimer.cancel();
							clickTimer.run();
						} else {
							clickTimer.schedule(DBL_CLICK_MILLIS);
						}
					}
					return true;
				}
				return false;
			}
			
			private void gestureEnded() {
				clickCnt = 0;
				startX = -1;
				startY = -1;
				startBut = 0;
				startMods = 0;
				restoreAllDefaults();
			}
		}

	private Timer positionTimer = new Timer() {
		@Override
		public void run() {
			try {
				updateParentPosition();
			} catch (Exception e) {}
		}
	};

	protected int parentOffX = 0;

	protected int parentOffY = 0;

	protected ArrayList<MouseActionBinding<MouseMoveAction>> moveActions = new ArrayList<MouseActionBinding<MouseMoveAction>>();
	protected ArrayList<MouseActionBinding<MouseClickAction>> clickActions = new ArrayList<MouseActionBinding<MouseClickAction>>();
	protected ArrayList<MouseActionBinding<MouseDragAction>> dragActions = new ArrayList<MouseActionBinding<MouseDragAction>>();
	protected ArrayList<MouseActionBinding<MouseWheelAction>> wheelActions = new ArrayList<MouseActionBinding<MouseWheelAction>>();
	
	
	private ArrayList<MouseActionBinding<MouseMoveAction>> savedMoveActions = new ArrayList<MouseActionBinding<MouseMoveAction>>();
	private ArrayList<MouseActionBinding<MouseClickAction>> savedClickActions = new ArrayList<MouseActionBinding<MouseClickAction>>();
	private ArrayList<MouseActionBinding<MouseDragAction>> savedDragActions = new ArrayList<MouseActionBinding<MouseDragAction>>();
	private ArrayList<MouseActionBinding<MouseWheelAction>> savedWheelActions = new ArrayList<MouseActionBinding<MouseWheelAction>>();
	
	private boolean actionsStateSaved = false;
	
	private GestureRecognizer recognizer = new GestureRecognizer();
	private WheelRecognizer wheelRecognizer = new WheelRecognizer();
	
	private Element parentEl;
	
	private boolean selectionAllowed = true;
	private boolean contextMenuAllowed = true;
	
	// Contains ActionSettings (so we know when to stop a certain action)
	private MouseActionBinding<MouseDragAction> dragging = null;
	
	EventOverrider eo;

	public MouseHandler(final Widget w) {
		this.parentEl = w.getElement();
		if (!w.isAttached()) {
			ScheduledCommand cmd = new ScheduledCommand() {
				public void execute() {
					if (!w.isAttached()) {
						Scheduler.get().scheduleDeferred(this);
					} else {
						initHandlers(w);
					}
				}
			};
			Scheduler.get().scheduleDeferred(cmd);
		} else {
			initHandlers(w);
		}
	}
	
	
	private void initHandlers(Widget w) {
		//add handlers
		w.addDomHandler(this, MouseDownEvent.getType());
		w.addDomHandler(this, MouseUpEvent.getType());
		w.addDomHandler(this, MouseMoveEvent.getType());
		w.addDomHandler(this, MouseWheelEvent.getType());
		w.addDomHandler(this, ClickEvent.getType());
		w.addDomHandler(this, DoubleClickEvent.getType());
		w.addDomHandler(this, MouseOverEvent.getType());
		w.addDomHandler(this, MouseOutEvent.getType());
		
		updateDefaultCursor();
		
		modifiers.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			public void onValueChange(ValueChangeEvent<Integer> event) {
				updateCursor(recognizer.startBut);
			}
		});
		positionTimer.scheduleRepeating(1000);
	}
	
	protected void updateParentPosition() {
		parentOffX = DOM.getAbsoluteLeft(parentEl) - Window.getScrollLeft();
		parentOffY = DOM.getAbsoluteTop(parentEl) - Window.getScrollTop();
	}

	public boolean saveActionsState() {
		if (actionsStateSaved)
			return false;
		
		savedMoveActions.addAll(moveActions);
		savedClickActions.addAll(clickActions);
		savedDragActions.addAll(dragActions);
		savedWheelActions.addAll(wheelActions);
		actionsStateSaved=true;
		return true;
	}

	public boolean restoreActionsState() {
		if (!actionsStateSaved)
			return false;
		
		moveActions.addAll(savedMoveActions);
		savedMoveActions.clear();
		clickActions.addAll(savedClickActions);
		savedClickActions.clear();
		dragActions.addAll(savedDragActions);
		savedDragActions.clear();
		wheelActions.addAll(savedWheelActions);
		savedWheelActions.clear();
		actionsStateSaved=false;
		return true;
	}

	public void deregisterAllActions() {
		moveActions.clear();
		dragActions.clear();
		wheelActions.clear();
		clickActions.clear();
		updateDefaultCursor();
	}

	private MouseActionBinding<? extends MouseAction> findDownTarget(int but, int mods, int clickCnt) {
		MouseActionBinding<?  extends MouseAction> ret = findTarget(clickActions, but, mods, clickCnt);
		if (ret != null) return ret;
		ret = findTarget(dragActions, but, mods, clickCnt);
		if (ret != null) return ret;
		return null;
	}

	void updateDefaultCursor() {
		DOM.setStyleAttribute(parentEl, "cursor", findDefaultCursor());
	}

	protected boolean dragStart(int screenX, int screenY, int mods, int but) {
		List<MouseActionBinding<MouseDragAction>> targets = findTargets(dragActions, but, mods, NOT_SET_I);
		if (targets.size()==0) return false;
		for (MouseActionBinding<MouseDragAction> target:targets) {			
			if (target.act.handleStartScreen(screenX, screenY, this)) {
				dragging = target;
				DOM.setStyleAttribute(parentEl, "cursor", target.act.getDragCursor());
				DOM.setCapture(getParentEl());
				return true;
			} 			
			if (!target.getAction().isChainable) // chain 
				return false;
		}		
		return false;
	}

	protected void dragMove(MouseMoveEvent evt) {
		if (dragging == null) {
			return;
		}
		dragging.act.handleMoveScreen(evt, this);
	}

	protected void dragEnd(MouseEvent<?> evt) {
		if (dragging == null) return;
		MouseDragAction mda = dragging.act;
		try {
			mda.handleEndScreen(getPageX(evt), getPageY(evt), this);
		} finally {
			dragging = null;
			updateDefaultCursor();
			DOM.releaseCapture(getParentEl());
		}
	}


	protected void clicked(int screenX, int screenY, int but, int mods, int clickCnt) {
		MouseActionBinding<MouseClickAction> ac = findTarget(clickActions, but, mods, clickCnt);
		if (ac == null) {
			return;
		}
		if (ac.clickCount <= clickCnt) {
			ac.act.handleClickScreen(screenX, screenY, this);
		}
	}

	public void setEventOverride(EventOverrider eo) {
		this.eo = eo;
		updateCursor(BUTTON_LEFT);
	}
	
	public void handleMouseEvent(MouseEvent<?> event) {
		if (eo != null && eo.handleEvent(event)) {
			return;
		}
		
		if (doNormalEvent(event)) {
			event.preventDefault();
			event.stopPropagation();
		}
	}
	
	/**
	 * @param evt
	 * @return true if default should be cancelled
	 */
	private boolean doNormalEvent(final MouseEvent<?> event) {
		final int mods = modifiers.current;
		final Type<?> type = event.getAssociatedType();
		
		if (type == MouseMoveEvent.getType()) {
			return handleMove((MouseMoveEvent)event, mods);
			
		} else if (type == MouseDownEvent.getType()) {
			updateParentPosition();
			return recognizer.down(getPageX(event), getPageY(event), event.getNativeButton(), mods);

		} else if (type == MouseUpEvent.getType()) {
			updateParentPosition();
			return recognizer.up(event);
			
			
		} else if (type == MouseWheelEvent.getType()) {
			return wheelRecognizer.wheel((MouseWheelEvent)event, mods);
			
		} else if (type == ClickEvent.getType()) {
			if (findTarget(clickActions, event.getNativeButton(), mods, 1) == null) return false;
			return true;
			
		} else if (type == DoubleClickEvent.getType()) {
			if (findTarget(clickActions, event.getNativeButton(), mods, 2) == null) return false;
			return true;
			
//		} else if (event instanceof MouseOutEvent) {
//			// do nothing
//		} else if (event instanceof MouseOverEvent) {
//			// do nothing
		} 
		
		return true;
	}
	
	private boolean handleMove(MouseMoveEvent evt, int mods) {
		for (MouseActionBinding<MouseMoveAction> st : moveActions) {
			if (st.canHandle(BUTTON_NA, mods, NOT_SET_I)) {
				st.act.handleMove(evt, this);
			}
		}
		return recognizer.move(evt);
	}
	
	public final int getElementX(MouseEvent<?> evt) {
		return evt.getClientX() - parentOffX;
	}
	
	public final int getElementY(MouseEvent<?> evt) {
		return evt.getClientY() - parentOffY;
	}
	
	public final int getElementX(int documentX) {
		return documentX - parentEl.getAbsoluteLeft();
	}
	
	public final int getElementY(int documentY) {
		return documentY - parentEl.getAbsoluteTop();
	}
	
	private void preventAllDefaults() {
		preventContextMenu(getParentEl());
		preventSelections(getParentEl());
	}
	
	private void restoreAllDefaults() {
		if ((dragging == null || dragging.button != BUTTON_RIGHT) && contextMenuAllowed) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					resetContextMenu(getParentEl());
				}
			});
		}
		if (dragging == null && selectionAllowed) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					resetSelections(getParentEl());
				}
			});
		}
	}
	
	private String findDefaultCursor() {
		return findCursor(BUTTON_LEFT, MOD_NONE, 1);
	}
	
	private String findCursor(int but, int mods, int clickCnt) {
		if (but <= 0) but = BUTTON_LEFT;
		if (clickCnt <= 0) clickCnt = 1;
		if (mods == MOD_ANY || mods == 0) mods = MOD_NONE;
		MouseActionBinding<? extends MouseAction> as = findDownTarget(but, mods, clickCnt);
		if (as != null) return as.act.getCursor();
		return "";
	}
	
	private void updateCursor(int but) {
		if (eo == null) {
			CSS.cursor(parentEl, findCursor(but, modifiers.current, 1));
		} else {
			CSS.cursor(parentEl, eo.getCursor());
		}
	}
	
	public void registerAction(MouseDragAction action, int button, int modifier) {
		registerActionBinding(MouseActionBinding.create(action, button, modifier));
	}
	
	public void registerAction(MouseClickAction action, int button, int modifier, int clickCount) {
		registerActionBinding(MouseActionBinding.create(action, button, modifier, clickCount));
	}
	
	public void registerAction(MouseWheelAction action, int modifier) {
		registerActionBinding(MouseActionBinding.create(action, modifier));
	}
	
	public void registerAction(MouseMoveAction action, int modifier) {
		registerActionBinding(MouseActionBinding.create(action, modifier));
	}
	
	@SuppressWarnings("unchecked")
	public void registerActionBinding(MouseActionBinding<?> binding) {
		deregisterActionBinding(binding);		
		Action action = binding.act;
		if (action instanceof MouseMoveAction) {
			moveActions.add(0, (MouseActionBinding<MouseMoveAction>)binding);
			
		} else if (action instanceof MouseClickAction) {
			clickActions.add(0, (MouseActionBinding<MouseClickAction>)binding);
			
		} else if (action instanceof MouseDragAction) {
			dragActions.add(0, (MouseActionBinding<MouseDragAction>)binding);
			
		} else if (action instanceof MouseWheelAction) {
			wheelActions.add(0, (MouseActionBinding<MouseWheelAction>)binding);
		}
		updateDefaultCursor();
	}
	
	private boolean deregisterAction(ArrayList<? extends MouseActionBinding<?>> actions, Action action) {
		boolean ret = false;
		for (int i = 0; i < actions.size(); i++) {
			MouseActionBinding<?> ac = actions.get(i);
			if (ac.act == action) {
				actions.remove(i--);
				ret = true;
			}
		}
		updateDefaultCursor();
		return ret;
	}
	
	
	
	public boolean isActionRegistered(MouseWheelAction action) {
		return isActionRegistered(wheelActions,action);
	}
	public boolean isActionRegistered(MouseDragAction action) {
		return isActionRegistered(dragActions,action);
	}
	public boolean isActionRegistered(MouseClickAction action) {
		return isActionRegistered(clickActions,action);
	}
	public boolean isActionRegistered(MouseMoveAction action) {
		return isActionRegistered(moveActions,action);
	}

	private static boolean isActionRegistered(ArrayList<? extends MouseActionBinding<?>> actions, Action action) {
		for (MouseActionBinding<?> ac : actions) {
			if (ac.act == action) {
				return true;
			}
		}
		return false;
	}
	public boolean deregisterAction(MouseWheelAction action) {
		return deregisterAction(wheelActions, action);
	}
	
	public boolean deregisterAction(MouseDragAction action) {
		return deregisterAction(dragActions, action);
	}
	
	public boolean deregisterAction(MouseClickAction action) {
		return deregisterAction(clickActions, action);
	}
	
	public boolean deregisterAction(MouseMoveAction action) {
		return deregisterAction(moveActions, action);
	}
	
	public void deregisterAction(MouseAction action) {
		if (action instanceof MouseMoveAction) {
			deregisterAction((MouseMoveAction)action);
			
		} else if (action instanceof MouseClickAction) {
			deregisterAction((MouseClickAction)action);
			
		} else if (action instanceof MouseDragAction) {
			deregisterAction((MouseDragAction)action);
			
		} else if (action instanceof MouseWheelAction) {
			deregisterAction((MouseWheelAction)action);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void deregisterActionBinding(MouseActionBinding<?> binding) {
		MouseAction action = binding.act;
		if (action instanceof MouseMoveAction) {
			deregisterActionBinding(moveActions, (MouseActionBinding<MouseMoveAction>)binding);
			
		} else if (action instanceof MouseClickAction) {
			deregisterActionBinding(clickActions, (MouseActionBinding<MouseClickAction>)binding);
			
		} else if (action instanceof MouseDragAction) {
			deregisterActionBinding(dragActions, (MouseActionBinding<MouseDragAction>)binding);
			
		} else if (action instanceof MouseWheelAction) {
			deregisterActionBinding(wheelActions, (MouseActionBinding<MouseWheelAction>)binding);
			
		}
	}
	
	private <T extends MouseAction> boolean deregisterActionBinding(ArrayList<MouseActionBinding<T>> actions, MouseActionBinding<T> binding) {
		boolean ret = false;
		for (int i = 0; i < actions.size(); i++) {
			MouseActionBinding<?> ac = actions.get(i);
			if (ac.equals(binding)) {
				actions.remove(i--);
				ret = true;
			}
		}
		updateDefaultCursor();
		return ret;
	}

	public Element getParentEl() {
		return parentEl;
	}
	
	public void setParentEl(Element parentEl) {
		this.parentEl = parentEl;
	}
	
	protected void wheelMoved(int docX, int docY, int delta, int mods) {
		MouseActionBinding<MouseWheelAction> ac = findTarget(wheelActions, BUTTON_NA, mods, NOT_SET_I);
		if (ac == null) {
			return;
		}
		ac.act.handleMove(docX, docY, delta, this);
	}
	
	protected void wheelPreview(int docX, int docY, int curDelta, int mods) {
		MouseActionBinding<MouseWheelAction> ac = findTarget(wheelActions, BUTTON_NA, mods, NOT_SET_I);
		if (ac == null) {
			return;
		}
		ac.act.handlePreview(docX, docY, curDelta, this);
	}
	
	public boolean isContextMenuAllowed() {
		return contextMenuAllowed;
	}
	
	public void setContextMenuAllowed(boolean allowContextMenu) {
		this.contextMenuAllowed = allowContextMenu;
		if (!contextMenuAllowed) preventContextMenu(parentEl);
		else restoreAllDefaults();
	}
	
	public boolean isSelectionAllowed() {
		return selectionAllowed;
	}
	
	public void setSelectionAllowed(boolean allowSelections) {
		this.selectionAllowed = allowSelections;
		if (!selectionAllowed) preventSelections(parentEl);
		else restoreAllDefaults();
	}

	public void onMouseDown(MouseDownEvent event) {
		handleMouseEvent(event);
	}

	public void onMouseUp(MouseUpEvent event) {
		handleMouseEvent(event);
	}

	public void onMouseMove(MouseMoveEvent event) {
		handleMouseEvent(event);
	}

	public void onMouseOut(MouseOutEvent event) {
		handleMouseEvent(event);
	}

	public void onMouseOver(MouseOverEvent event) {
		handleMouseEvent(event);
	}

	public void onMouseWheel(MouseWheelEvent event) {
		handleMouseEvent(event);
	}
	
	public void onClick(ClickEvent event) {
		handleMouseEvent(event);
	}
	
	public void onDoubleClick(DoubleClickEvent event) {
		handleMouseEvent(event);
	}

	public static final int getModifiers() {
		return modifiers.current;
	}

	/**
	 * Prevent context menu while dragging and after mouse up
	 */
	public static native void preventContextMenu(com.google.gwt.dom.client.Element el) /*-{
	       el.oncontextmenu = function() {return false;};
	   }-*/;

	private static <T extends MouseAction> List<MouseActionBinding<T>> findTargets(ArrayList<MouseActionBinding<T>> actions, int but, int mods, int clickCnt) {
		
		ArrayList<MouseActionBinding<T>> list =  new ArrayList<MouseActionBinding<T>>();
		
		for (Iterator<MouseActionBinding<T>> iter = actions.iterator(); iter.hasNext();) {
			MouseActionBinding<T> cur = iter.next();
			if (cur.canHandle(but, mods, clickCnt)) {
				int bestIdx=-1;
				if (list.size()>0) {				
					for (int j=list.size()-1;j>=0;j--) {
						MouseActionBinding<T> best = list.get(j);
						if (cur.isMoreSpecificThan(best.button, best.modifier, best.clickCount, clickCnt)) { 
							bestIdx=j;
						} else {
							break;
						}
							
					}
				} 
				if (bestIdx==-1)
					list.add(cur);
				else
					list.add(bestIdx,cur);
			}
		}
	
		return list;
	}

	private static <T extends MouseAction> MouseActionBinding<T> findTarget(ArrayList<MouseActionBinding<T>> actions, int but, int mods, int clickCnt) {
		MouseActionBinding<T> best = null;
		for (Iterator<MouseActionBinding<T>> iter = actions.iterator(); iter.hasNext();) {
			MouseActionBinding<T> cur = iter.next();
			if (cur.canHandle(but, mods, clickCnt)) {
				if (best == null) {
					best = cur;
				} else if (cur.isMoreSpecificThan(best.button, best.modifier, best.clickCount, clickCnt)) {
					best = cur;
				}
			}
		}
		return best;
	}

	public static final String eventToString(Event evt) {
		return toStringType(DOM.eventGetType(evt)) + " " + toStringMod(modifiers.current) + " "
			+ toStringBut(DOM.eventGetButton(evt));
	}

	private static String toStringType(int type) {
		switch (type) {
			case Event.ONMOUSEOVER:
				return "Over";
			case Event.ONMOUSEOUT:
				return "Out";
			case Event.ONMOUSEDOWN:
				return "Down";
			case Event.ONMOUSEMOVE:
				return "Move";
			case Event.ONMOUSEUP:
				return "Up";
			case Event.ONCLICK:
				return "Click";
			case Event.ONDBLCLICK:
				return "DoubleClick";
			case Event.ONKEYDOWN:
				return "KeyDown";
			case Event.ONKEYUP:
				return "KeyUp";
			case Event.ONKEYPRESS:
				return "KeyPress";
			case Event.ONFOCUS:
				return "Focus";
			case Event.ONBLUR:
				return "Blur";
			case Event.ONMOUSEWHEEL:
				return "Wheel";
			default:
				return "Unknown " + type;
		}
	}

	private static String toStringBut(int but) {
		StringBuffer ret = new StringBuffer();
		int cnt = 0;
		if ((but & BUTTON_LEFT) != 0) {
			if (cnt++ > 0) ret.append("+");
			ret.append("L");
		}
		if ((but & BUTTON_MIDDLE) != 0) {
			if (cnt++ > 0) ret.append("+");
			ret.append("M");
		}
		if ((but & BUTTON_RIGHT) != 0) {
			if (cnt++ > 0) ret.append("+");
			ret.append("R");
		}
		return ret.toString();
	}

	private static String toStringMod(int mods) {
		StringBuffer ret = new StringBuffer();
		int cnt = 0;
		if ((mods & MOD_SHIFT) != 0) {
			if (cnt++ > 0) ret.append("+");
			ret.append("SHIFT");
		}
		if ((mods & MOD_CTRL) != 0) {
			if (cnt++ > 0) ret.append("+");
			ret.append("CTRL");
		}
		if ((mods & MOD_ALT) != 0) {
			if (cnt++ > 0) ret.append("+");
			ret.append("ALT");
		}
		if ((mods & MOD_SPACE) != 0) {
			if (cnt++ > 0) ret.append("+");
			ret.append("SPACE");
		}
		return ret.toString();
	}

	public final static int getPageX(MouseEvent<?> evt) {
		return ExtDOM.eventGetPageX(evt.getNativeEvent());
	}

	public final static int getPageY(MouseEvent<?> evt) {
		return ExtDOM.eventGetPageY(evt.getNativeEvent());
	}

	public static final int getWheelDelta(MouseWheelEvent evt) {
		return evt.getDeltaY();
	}

	// Prevent click while dragging and after mouse up
	public static native void resetContextMenu(Element el) /*-{
	     if (el.oncontextmenu) {
	     	el.oncontextmenu=function() {return true;};
	     }
	 }-*/;

	private static native void preventSelections(Element el) /*-{
	     if ($doc.Body && $doc.Body.focus()) {
	     	$doc.Body.focus();
	     }
	     if (el.style.MozUserSelect) {
	     	el.style.MozUserSelect = 'none';
	     }
	     el.onselectstart = function () { return false; };
	     el.onselect = function () { return false; };
	}-*/;

	private static native void resetSelections(Element el) /*-{
	     if (el.style.MozUserSelect) {
	     	el.style.MozUserSelect = 'normal';
	     }
	     el.onselectstart = null;
	     el.onselect = null;
	}-*/;

	public static boolean isControlDown(int currentModifiers) {
		return (currentModifiers & MOD_CTRL) != 0;
	}
}
