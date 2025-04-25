/**
 * 
 */
package com.sinergise.java.geometry.topo;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.SimpleTransform;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.topo.Edge;
import com.sinergise.common.geometry.topo.Face;
import com.sinergise.common.geometry.topo.ITopoMap;
import com.sinergise.common.geometry.topo.Node;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;


/**
 * @author tcerovski
 */
public class DefaultTopoDrawer {
	public static final DefaultTopoDrawer show(final ITopoMap map) {
		final DisplayCoordinateAdapter testDca = new DisplayCoordinateAdapter(new CartesianCRS(new CrsIdentifier("0"), map.getMBR()));
		testDca.setDisplaySize(800, 800);
		Envelope env = map.getMBR().expandedFor(3);
		testDca.setDisplayedRect(env);

		final DefaultTopoDrawer dr = new DefaultTopoDrawer();
		
		final JPanel testPaintPanel = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(java.awt.Graphics g) {
				g.clearRect(0, 0, testDca.getDisplaySize().w(), testDca.getDisplaySize().h());
				dr.draw(testDca.pixFromWorld, (Graphics2D)g, map);
			}
		};
		Timer tmr = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				testDca.bounds.setMBR(map.getMBR());
				testDca.setDisplaySize(testPaintPanel.getWidth(), testPaintPanel.getHeight());
				testDca.setDisplayedRect(map.getMBR().expandedForSizeRatio(0.2));
				System.out.println(map.getMBR());
				testPaintPanel.repaint();
			}
		});
		tmr.setRepeats(true);
		tmr.start();
		
		JFrame testWin = new JFrame();
		testWin.setContentPane(testPaintPanel);
		testWin.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		testWin.setSize(testDca.getDisplaySize().w(), testDca.getDisplaySize().h());
		testWin.setVisible(true);
		return dr;
	}	
	
	protected Color colorLocked = new Color(0xFF2020);
	protected Color colorAvailable = Color.GREEN;
	
	protected ThreadLocal<ITopoMap> tMap = new ThreadLocal<ITopoMap>();
	
	public DefaultTopoDrawer() {
		
	}
	
	public void draw(SimpleTransform.ToInt<CartesianCRS, CartesianCRS> trans, Graphics2D g, ITopoMap map) {
		tMap.set(map);
		try {
			initStyle(g);
			for(Face face : map.getFaces()) {
				drawFace(trans, g, face);
			}
		
			for(Edge edge : map.getEdges()) {
				drawEdge(trans, g, edge);
			}
		
			for(Node node : map.getNodes()) {
				drawNode(trans, g, node);
			}
		} finally {
			tMap.set(null);
		}
	}
	
	protected void drawEdge(SimpleTransform.ToInt<CartesianCRS, CartesianCRS> trans, Graphics2D g, Edge edge) {
		styleEdge(g, edge);
		
		int x1 = trans.xInt(edge.x1()); 
		int y1 = trans.yInt(edge.y1());
		int x2 = trans.xInt(edge.x2());	
		int y2 = trans.yInt(edge.y2());
		
		int x = (int)Math.round(0.5* (x2+x1));
		int y = (int)Math.round(0.5* (y2+y1));
		
		Object oldAntiAliasing = g.getRenderingHint(KEY_ANTIALIASING);
		try {
			g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
			g.drawLine(x1,y1,x2,y2);
		} finally {
			g.setRenderingHint(KEY_ANTIALIASING, oldAntiAliasing);
		}
		
		FontMetrics fontMetrics = g.getFontMetrics();
		
		if(shouldDrawEdgeFaceLabels(trans.getScale())) {
			String edgeFacesLabel = "l"+
				(edge.getLeftFace()!=null?edge.getLeftFace().getLocalID()+",":"")+
				(edge.getRightFace()!=null?" r"+edge.getRightFace().getLocalID():"");
			
			double len=GeomUtil.distance(x1, y1, x2, y2);
			double c=(y2-y1)/len;
			double s=(x2-x1)/len;
			g.drawLine(x, y, x-(int)(10*(c+s)), y-(int)(10*(c-s)));
			
			Rectangle2D rect1 = fontMetrics.getStringBounds(edgeFacesLabel, g);
			
			g.setColor(Color.BLUE);
			g.drawString(edgeFacesLabel,
	        		(int) (x - rect1.getCenterX()),
	                (int) (y - rect1.getCenterY()));
		}
		
		if(shouldDrawEdgeLabels(trans.getScale())) {
			String edgeLabel = edge.getLocalID();
			Rectangle2D rect2 = fontMetrics.getStringBounds(edgeLabel, g);
			g.drawString(edgeLabel, 
					(int) (x - rect2.getCenterX()), 
					(int) (y - rect2.getCenterY()+rect2.getHeight()));
		}
	}
	
	protected void drawNode(SimpleTransform.ToInt<CartesianCRS, CartesianCRS> trans, Graphics2D g, Node node) {
		styleNode(g, node);
		
		int x = trans.xInt(node.x());
		int y = trans.yInt(node.y());
		
		g.drawRect(x-2, y-2, 4,4);
		
		if (shouldDrawNodeLabels(trans.getScale())) {
			g.drawString(node.getLocalID(), x, y-10);
		}
	}
	
	protected void drawFace(SimpleTransform.ToInt<CartesianCRS, CartesianCRS> trans, Graphics2D g, Face face) {
		styleFace(g, face);
		if(Double.isNaN(face.cenX()) || Double.isNaN(face.cenY()))
			return;
		
		if(shouldDrawFaceLabels(trans.getScale())) {
			drawFaceLabel(trans, g, face);
		}
	}
	
	protected void drawFaceLabel(SimpleTransform.ToInt<CartesianCRS, CartesianCRS> trans, Graphics2D g, Face face) {
		double x = trans.xInt(face.cenX());
		double y = trans.yInt(face.cenY());
		String text = face.getLocalID();
		
		Rectangle2D rect = g.getFontMetrics().getStringBounds(text, g);
		g.drawString(text, (int) (x - rect.getCenterX()), (int) (y - rect.getCenterY()));
	}
	
	protected void initStyle(Graphics2D g) {
		g.setComposite(AlphaComposite.SrcOver);
		g.setFont(Font.decode("Dialog-12-Plain"));
	}
	
	protected void styleEdge(Graphics2D g, Edge edge) {
		g.setColor(edge.isLocked()?colorLocked:colorAvailable);
		g.setStroke(new BasicStroke(1.0f));
	}
	
	protected void styleNode(Graphics2D g, Node node) {
		g.setColor(node.isLocked()?colorLocked:colorAvailable);
	}
	
	protected void styleFace(Graphics2D g, Face face) {
		g.setColor(face.isLocked()?colorLocked:colorAvailable);
	}
	
	/**
	 * @param scale as pixels/meter
	 */
	protected boolean shouldDrawEdgeLabels(double scale) {
		return true;
	}
	
	/**
	 * @param scale as pixels/meter
	 */
	protected boolean shouldDrawEdgeFaceLabels(double scale) {
		return true;
	}
	
	/**
	 * @param scale as pixels/meter
	 */
	protected boolean shouldDrawFaceLabels(double scale) {
		return true;
	}
	
	/**
	 * @param scale as pixels/meter
	 */
	protected boolean shouldDrawNodeLabels(double scale) {
		return true;
	}
	
}
