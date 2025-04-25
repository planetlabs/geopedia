package com.sinergise.geopedia.client.ui.widgets;

import java.util.HashMap;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.ImageUtilGWT;
import com.sinergise.gwt.util.html.CSS;

/**
 * @author smusevic
 * widget for viewing panoramic images
 * 
 */
@Deprecated
public class PanImageViewer extends Widget {
	
	private String imgSrc;

	private Element img1, img2;
	
	private Element viewPanel, navButtons, mainPanel;//, zoomButtons; 
	
	private static final  int imgDetectDragEventBits = Event.ONMOUSEDOWN  | Event.ONMOUSEUP |  Event.ONMOUSEMOVE | Event.ONMOUSEOUT | Event.ONLOSECAPTURE;
	
	private Image left, right, stop;
	private int panelOffset;
	private static final int panelOffsetMargin = 10;
	
	protected final PanImageState state = new PanImageState();
	 
	/**
	* This map is used to store prefetched images. If a reference is not kept to
	* the prefetched image objects, they can get garbage collected, which
	* sometimes keeps them from getting fully fetched.
	*/
	private static HashMap<String, Element> prefetchImages = new HashMap<String, Element>();

	
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#setHeight(java.lang.String)
	 * overriding because height change affects image position
	 */
	@Override
	public void setHeight(String height) {
		super.setHeight(height);
		if (panelOffset == 0) initPanelOffset();
		
		int newHeight =  getOffsetHeight()/* - panelOffset - panelOffsetMargin*/;
		state.viewHeight = newHeight > 0 ? newHeight : 0;
		CSS.height (viewPanel, state.viewHeight);
		
		
		if (state.viewHeight <  state.imgHeight){
			//TODO: do something????
		}
	}
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#setWidth(java.lang.String)
	 * overriding because width change affects image position
	 */
	@Override
	public void setWidth(String width) {
		int oldWidth = getOffsetWidth();
		super.setWidth(width);
		int newWidth = getOffsetWidth();
		
		state.viewWidth  = newWidth;
		CSS.width(viewPanel, state.viewWidth);
		if (newWidth > oldWidth){
			//TODO: ??? what to do here?
		}
	}
	
	
	 
	/**
	 * @author Saso
	 * this is a state of pan image viewer
	 */
	private static final class PanImageState{
		
		private static final int frameDelay = 10;
		private static final int speedStep = 5;
		private static final int zoomStep = 10;
		private final SlideTimer timer = new SlideTimer(this);
		private int currentSpeed = 0;
		
		public int img1x, img2x,  imgY, cursorX, cursorY,  imgWidth, viewWidth, imgHeight, viewHeight, offset;
		public double imgRatio;
		public  Element img1,img2;
		public  boolean isDraging = false, isSliding = false;
		
		protected void changeSpeed(int amnt){
			//System.out.println("changeSpeed");
			if (currentSpeed == 0 && amnt != 0){
				timer.scheduleRepeating(frameDelay);
				currentSpeed += amnt;
				isSliding = true;
			} else if (currentSpeed != 0 && amnt != 0){
				currentSpeed += amnt;
				if (currentSpeed == 0) stopSliding();
			}
		}
		
		public static final class SlideTimer extends Timer {
			private PanImageState state;
			public SlideTimer(PanImageState state){
				this.state = state;
			}
			@Override
			public void run() {
				state.slideHorizontal();
			}
		}
		 
		 public void slideHorizontal(){
			 driftHorizontal(currentSpeed);
		 }
		 
		 public void resumeSliding(){
			 timer.scheduleRepeating(frameDelay);
		 }

		 public void slideLeft(){
			 changeSpeed(speedStep);
		 }
		 
		 public void slideRight(){
			 changeSpeed(-speedStep);
		 }
		 private void moveHorizontal(int newImg1X){
			//System.out.println("before moveHorizontal");
			//printLocations();
			int newImg2X = (img1x > img2x ? newImg1X - imgWidth :  newImg1X + imgWidth);
			
			if (newImg1X > -offset && newImg2X > newImg1X){
				newImg2X = newImg1X - imgWidth;
			} else if (newImg1X +  imgWidth < viewWidth - offset && newImg2X < newImg1X)
				newImg2X = newImg1X + imgWidth;
			else if (newImg2X > -offset && newImg1X > newImg2X){
				newImg1X = newImg2X - imgWidth;
			} else if (newImg2X +  imgWidth < viewWidth - offset && newImg1X < newImg2X)
				newImg1X = newImg2X + imgWidth;
			
			CSS.left(img1,newImg1X);
			CSS.left(img2,newImg2X);
			img1x = newImg1X;
			img2x = newImg2X;
			//System.out.println("after moveHorizontal");
			//printLocations();
			
		 }
		 
		private void moveVertical(int newImgY){
			////System.out.println("moveVertical");
			int newY =  newImgY;
			if (viewHeight - newImgY > imgHeight) 
				newImgY = viewHeight - imgHeight;
			else if (newImgY > 0) 
				newImgY = 0;
			
			imgY = newY;
			
			CSS.top(img1, newImgY);
			CSS.top(img2, newImgY);
										 
		 }
		 
		 public void driftHorizontal(int driftX){
			 int newImg1X =  img1x + driftX;
			 //System.out.println("driftHorizontal, driftX=" + driftX);
			 moveHorizontal(newImg1X);
		 }
		 
		 public void driftVertical(int driftY){
				int newImgY = imgY + driftY;
				moveVertical(newImgY);
		 }
//		 private void printLocations(){
//			 //System.out.println("  cursorX=" + cursorX + ", cursorY=" + cursorY + ", img1x=" + img1x + ", img2x=" + img2x);			 
//		 }
		 public void drag(int x, int y){
			//System.out.println("drag, x=" + x + ", y=" + y);
			int driftX = x - cursorX;
		 	driftHorizontal(driftX);
		 	
		 	int driftY = y - cursorY;
		 	driftVertical(driftY);
		 	
		 	cursorX = x;
			cursorY = y;
			
			//printLocations();
		 }

		public void stopDragigng() {
			//System.out.println("stopDraging");
			isDraging = false;
			//printLocations();
			if (isSliding) 
				resumeSliding();
		}
		
		public void stopSliding() {
			//System.out.println("stopSliding");
			timer.cancel();
			isSliding = false;
			currentSpeed = 0;			
		}

		public void startDragging(int curPosX, int curPosY) {
			this.cursorX = curPosX;
			this.cursorY = curPosY;
			isDraging = true;
			//System.out.println("startDragging");
			//printLocations();
			if (isSliding) 
				timer.cancel();
		}

		private void changeImageSize(int percent){
			
			if(viewHeight == imgHeight && percent < 100) return;
			
			int oldWidth = imgWidth;
			
			imgWidth =  imgWidth * percent / 100;
			imgHeight =  imgHeight * percent / 100;


			if (imgHeight < viewHeight || imgWidth < viewWidth){
				imgHeight = viewHeight;
				imgWidth = (int) (viewHeight * imgRatio);
				percent =  imgWidth * 100 / oldWidth;
			}

			CSS.size(img1, imgWidth, imgHeight);
			CSS.size(img2, imgWidth, imgHeight);
			
			moveHorizontal((percent *( ( img1x < 0 && ( img1x + viewWidth < imgWidth) ? img1x : img2x ) * 2 - viewWidth )   + (viewWidth * 100) ) / 200);
			moveVertical( (percent *( imgY * 2 - viewHeight )   + viewHeight * 100 ) / 200);
		}
		
		public void zoomIn() {
			changeImageSize(100 + zoomStep);
		}

		public void zoomOut() {
			changeImageSize(100 - zoomStep);
		}
	 }
	 
	 
	private static abstract class ClickEventListener implements EventListener{

		public abstract void doClick();
		public void onBrowserEvent(Event event){
			int eventCode = DOM.eventGetType(event);
			if ( (eventCode & Event.ONCLICK)  != 0) doClick();
//			else {
//				 //System.out.println("caught unimportant event: " +DOM.eventToString(event) );
//			}
		}
	}
	private static final class ImgDragEventPreview implements EventListener{
		
		Element captureTarget;
		private static final int offset = 50;
		private PanImageState state;
		 
		public ImgDragEventPreview(PanImageState state){
			this.state = state;
		}
		 
		private void releaseCapture(){
			//System.out.println("release capture");
			if (captureTarget != null)
				DOM.releaseCapture(captureTarget);
			captureTarget = null;
		}
		 
		private void capture(Event event){
			//System.out.println("capture");
			Element newCapture =  DOM.eventGetTarget(event);
			if (captureTarget != null){
				if(!captureTarget.equals(newCapture)){
					DOM.releaseCapture(captureTarget);
				} else
					return;
			}
			captureTarget = newCapture;
			DOM.setCapture(captureTarget);
		 }
		 public void onBrowserEvent(Event evnt) {
			 int eventCode = DOM.eventGetType(evnt);
			 
			 if ((eventCode & Event.ONMOUSEMOVE ) > 0 && state.isDraging){
				 state.drag( DOM.eventGetClientX(evnt),  DOM.eventGetClientY(evnt));
			 } else if ( (eventCode & Event.ONMOUSEUP ) != 0 ){ 
				 state.stopDragigng();
				 releaseCapture();
			 } else if ((eventCode & Event.ONMOUSEDOWN ) != 0){
				 capture(evnt);
				 state.startDragging(DOM.eventGetClientX(evnt), DOM.eventGetClientY(evnt));
			 } else if ((eventCode & Event.ONLOSECAPTURE) != 0) {
				 state.isDraging = false;
			 }
			 DOM.eventPreventDefault(evnt);
		}
		public static int getOffset() {
			return offset;
		}
	}
	
	private final  ImgDragEventPreview dragDetectEP = new ImgDragEventPreview(state);

	private Image zoomIn, zoomOut;

	private ImageResource imgLeft,  imgRight, imgStop, imgZoomIn, imgZoomOut;
	
	/**
	 * use this constructor to use it with sample panoramic image
	 */
	public PanImageViewer(){
		this("./img/panImg1.jpg");
        setPixelSize(200, 100);
		//test image
	}
	/**
	 * use this constructor for default icons 
	 * @param imgSrc: panoramic image src
	 */
	public PanImageViewer(String imgSrc){
		this(imgSrc, null,null,null,null,null);
	}

	/**
	 * use this constructor for custom icons
	 * 
	 * @param imgSrc: panoramic image src
	 * @param imgLeft: slide left icon 
	 * @param imgRight: slide right icon
	 * @param imgStop: slide stop icon
	 * @param imgZoomIn: zoom-in icon
	 * @param imgZoomOut: zoom-out icon
	 */
	public PanImageViewer(String imgSrc, ImageResource imgLeft, ImageResource imgRight, ImageResource imgStop, ImageResource imgZoomIn, ImageResource imgZoomOut){
		this.imgLeft = imgLeft;
		this.imgRight = imgRight;
		this.imgStop = imgStop;
		this.imgZoomIn = imgZoomIn;
		this.imgZoomOut= imgZoomOut;
		this.imgSrc = imgSrc;
		initPanel();
		initButtonPanels();
	}
	
	/**
	 * clear images
	 * used when you change image 
	 */
	private void clear(){
		Element div = getElement();
		if (div != null) {
			if (img1 != null)
				DOM.removeChild(div, img1);
			if (img2 != null)
				DOM.removeChild(div, img2);
		}
	}

	/**
	 * initializes img element, various html/css magic
	 * @param img: img element to initialize
	 */
	private void initImg(Element img){
		if (img != null){
			CSS.position(img, "absolute");
			CSS.cursor(img, CSS.CURSOR_MOVE);
			CSS.margin0(img);
			DOM.sinkEvents(img,  imgDetectDragEventBits );
			if (imgSrc != null ){
				ImageUtilGWT.setSource(img, imgSrc);
				prefetchImages.put(imgSrc, img);
			}
			else
				ImageUtilGWT.setSource(img, "");
		}
	}
	
	/**
	 * creates the button panel (left, right, stop, zoom-in, zoom-out)
	 */
	private void initButtonPanels(){
		if (mainPanel != null){
			navButtons = DOM.createDiv();
			DOM.setElementAttribute(navButtons, "class", "navButtons");
			DOM.appendChild(mainPanel, navButtons);
			CSS.position(navButtons,  CSS.POS_ABSOLUTE);
			CSS.top(navButtons, 250);
			CSS.left(navButtons, 95);
			
			Element tdL = DOM.createSpan();
			DOM.setElementAttribute(tdL, "class", "left");
			DOM.appendChild(navButtons, tdL);
			
			if (imgLeft == null)
				left = new Image();
			else 
				left = new Image(imgLeft);
			DOM.sinkEvents(left.getElement(), Event.ONCLICK);
			DOM.setEventListener(
					left.getElement(), 
					new ClickEventListener(){
						@Override
						public void doClick() {
							state.slideLeft();
						}
					}
				);
			DOM.appendChild(tdL, left.getElement());
			
			Element tdC = DOM.createSpan();
			DOM.setElementAttribute(tdC, "class", "pause");
			DOM.appendChild(navButtons, tdC);
			if (imgStop == null)
				stop = new Image();
			else 
				stop = new Image(imgStop);
			DOM.sinkEvents(stop.getElement(), Event.ONCLICK);
			DOM.setEventListener(
					stop.getElement(), 
					new ClickEventListener(){
						@Override
						public void doClick() {
							state.stopSliding();
						}
					}
				);					
			DOM.appendChild(tdC, stop.getElement());
			
			Element tdR = DOM.createSpan();
			DOM.setElementAttribute(tdR, "class", "right");
			DOM.appendChild(navButtons, tdR);
			if (imgRight == null)
				right = new Image();
			else
				right = new Image(imgRight);
			CSS.cursor(right.getElement(), CSS.CURSOR_HAND);
			DOM.sinkEvents(right.getElement(), Event.ONCLICK);
			DOM.setEventListener(
					right.getElement(), 
					new ClickEventListener(){
						@Override
						public void doClick() {
							state.slideRight();
						}
					}
				);			
			DOM.appendChild(tdR, right.getElement());
			
			Element tdO;
			tdO =  DOM.createSpan();
			DOM.setElementAttribute(tdO, "class", "out");
			DOM.appendChild(navButtons, tdO);
			if (imgZoomOut == null)
				zoomOut = new Image();
			else
				zoomOut = new Image(imgZoomOut);
			CSS.cursor(zoomOut.getElement(), CSS.CURSOR_HAND);
			DOM.sinkEvents(zoomOut.getElement(), Event.ONCLICK);
			DOM.setEventListener(
					zoomOut.getElement(), 
					new ClickEventListener(){
						@Override
						public void doClick() {
							state.zoomOut();
						}
					}
				);
			CSS.cursor(zoomOut.getElement(), CSS.CURSOR_HAND);
			DOM.appendChild(tdO, this.zoomOut.getElement());
			
			Element td;
			td =  DOM.createSpan();
			DOM.setElementAttribute(td, "class", "in");
			DOM.appendChild(navButtons, td);
			if (imgZoomIn == null)
				zoomIn = new Image();
			else
				zoomIn = new Image(imgZoomIn);
			DOM.sinkEvents(zoomIn.getElement(), Event.ONCLICK);
			DOM.setEventListener(
					zoomIn.getElement(), 
					new ClickEventListener(){
						@Override
						public void doClick() {
							state.zoomIn();
						}
					}
				);
			CSS.cursor(zoomIn.getElement(), CSS.CURSOR_HAND);
			DOM.appendChild(td, this.zoomIn.getElement());
			
		}
	}
	/**
	 * initializes main html stuff
	 */
	private void initPanel(){
		if  (viewPanel == null) {
			viewPanel = 	DOM.createDiv();
		
			CSS.position(viewPanel,  CSS.POS_RELATIVE);
		}
		if (mainPanel == null){
			mainPanel = DOM.createDiv();
			CSS.position(mainPanel,  CSS.POS_RELATIVE);
			DOM.appendChild(mainPanel, viewPanel);
			setElement(mainPanel);
		}
		if (img1 == null){
			img1 = DOM.createImg();
			initImg(img1);	
		}

		if (img2 == null){
			img2 = DOM.createImg();
			initImg(img2);	
		}
		DOM.setEventListener(img2,dragDetectEP);
		DOM.setEventListener(img1,dragDetectEP);

		DOM.appendChild(viewPanel, img2);
		DOM.appendChild(viewPanel, img1);
		CSS.overflow(viewPanel,"hidden");
		
		
	}
	/**
	 * main init
	 */
	private void init(){
		initPanel();
		initButtonPanels();
	}

	
	/**
	 * @return panoramic image src
	 */
	public String getImgSrc() {
		return imgSrc;
	}
	/**
	 * @param imgSrc: new panoramic image src
	 */
	public void setImgSrc(String imgSrc) {
		if (this.imgSrc != null && this.imgSrc.equals(imgSrc)) return;
		this.imgSrc = imgSrc;
		clear();
		init();
	}
	
	/**
	 * positions the buttons
	 * call this when widget is resized, I think
	 */
	private void positionNavButtons(){
		int h = 0;
		h = CSS.getOffsetHeight(viewPanel);
		//System.out.println("positionNavButtons.viewPanel.height: " + h);
		int w = getOffsetWidth();
//		CSS.left(navButtons, (w -  CSS.getOffsetWidth(navButtons)) / 2 );
//		CSS.top(navButtons, h);
	}
	
	private void initPanelOffset(){
		if (left != null && panelOffset < left.getOffsetHeight()) panelOffset =  left.getOffsetHeight();
		if (right != null && panelOffset < right.getOffsetHeight()) panelOffset =  right.getOffsetHeight();
		if (stop != null && panelOffset < stop.getOffsetHeight()) panelOffset =  stop.getOffsetHeight();
		if (zoomIn != null && panelOffset < zoomIn.getOffsetHeight()) panelOffset =  zoomIn.getOffsetHeight();
		if (zoomOut != null && panelOffset < zoomOut.getOffsetHeight()) panelOffset =  zoomOut.getOffsetHeight();
		//panelOffset -= panelOffsetMargin;
		//System.out.println("initPanelOffset: " + panelOffset);
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.Widget#onAttach()
	 * 
	 * 
	 * 
	 */
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.Widget#onAttach()
	 * 
	 * determine size of widget here...
	 * 
	 */
	@Override
	protected void onAttach() {
		//System.out.println("onAttach");
		super.onAttach();
		
		int width = 0;
		int height = 0;

		width =CSS.getOffsetWidth(img1);
		height = CSS.getOffsetHeight(img1);
		
		state.imgWidth = width;
		state.imgHeight = height;

		int initOffset = -2 * ImgDragEventPreview.getOffset();
		
		state.img1x = initOffset;
		state.img2x = width;
		
		CSS.left(img1, state.img1x);
		CSS.left(img2, state.img2x);
		
		initPanelOffset();
		
		setHeight(getOffsetHeight() + "px");
		
		setWidth(getOffsetWidth() + "px");
		
		positionNavButtons();
		
		state.img1 = img1;
		state.img2 = img2;
        
		width = getOffsetWidth();
		height = getOffsetHeight();
		
		state.viewWidth = width;
		state.viewHeight = height;
		
		state.imgRatio = (double)state.imgWidth / (double) state.imgHeight;
		
	}
	
}
