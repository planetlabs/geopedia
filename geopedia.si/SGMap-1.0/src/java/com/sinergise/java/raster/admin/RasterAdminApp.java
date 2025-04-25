package com.sinergise.java.raster.admin;


import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.java.util.UtilJava;

public class RasterAdminApp {
	static {
		UtilJava.initStaticUtils();
	}
	public static final Logger logger = LoggerFactory.getLogger(RasterAdminApp.class);
	public static Preferences PREFS = Preferences.userNodeForPackage(RasterAdminApp.class);
	
	public static final void storeLastPyramidDir(File lastDir) {
		store("LastPyramidDir", lastDir.getAbsolutePath());
	}

	public static final File getLastPyramidDir() {
		return new File(PREFS.get("LastPyramidDir", ""));
	}
	
	public static final void storeLastSource(File lastDir) {
		store("LastSourceDir", lastDir.getAbsolutePath());
	}
	
	public static final File getLastSourceDir() {
		return new File(PREFS.get("LastSourceDir", ""));
	}
	
	
	public static final void store(String key, String value) {
		try {
			PREFS.put(key, value);
			PREFS.flush();
		} catch(BackingStoreException e) {
			logger.error("Cannot store preferences", e);
		}
	}

	
	JFrame	frame;
	RasterAdminMap mapComponent;
	private SourcePanel sourcesPanel;

	/**
	 * Launch the application.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RasterAdminApp window = new RasterAdminApp();
					window.frame.setVisible(true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public RasterAdminApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.25);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel mapPanel = new JPanel();
		splitPane.setRightComponent(mapPanel);
		mapPanel.setLayout(new BorderLayout(0, 0));
		
		JToolBar mapToolBar = new JToolBar();
		mapPanel.add(mapToolBar, BorderLayout.NORTH);
		
		JButton zoomAll = new JButton("All");
		zoomAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapComponent.zoomAll();
			}
		});
		mapToolBar.add(zoomAll);
		
		JButton zoomIn = new JButton("+");
		zoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapComponent.zoomBy(1);
			}
		});
		mapToolBar.add(zoomIn);
		
		JButton zoomOut = new JButton("-");
		zoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapComponent.zoomBy(-1);
			}
		});
		mapToolBar.add(zoomOut);
		
		mapComponent =  new RasterAdminMap();
		mapPanel.add(mapComponent, BorderLayout.CENTER);
		
		JPanel leftPanel = new JPanel();
		splitPane.setLeftComponent(leftPanel);
		leftPanel.setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		leftPanel.add(tabbedPane, BorderLayout.CENTER);
		
		RasterDatasetPanel rasterDatasetPanel = new RasterDatasetPanel(this);
		tabbedPane.addTab("Dataset", null, rasterDatasetPanel, null);
		
		sourcesPanel = new SourcePanel(this);
		tabbedPane.addTab("Sources", null, sourcesPanel, null);
	}		
	
	public SourcePanel getSourcesPanel() {
		return sourcesPanel;
	}

	public void showError(String string, Exception e1) {
		logger.error(string, e1);
		e1.printStackTrace();
		JOptionPane.showMessageDialog(frame, string + ":\n" + e1.getMessage(), "Application Error", JOptionPane.ERROR_MESSAGE);
	}
}
