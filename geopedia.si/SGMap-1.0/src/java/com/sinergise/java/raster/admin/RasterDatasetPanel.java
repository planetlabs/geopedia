package com.sinergise.java.raster.admin;

import static com.sinergise.java.raster.admin.RasterAdminApp.getLastPyramidDir;
import static java.awt.event.KeyEvent.VK_O;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileFilter;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsPix;
import com.sinergise.common.raster.core.VersionedRasterDataset;
import com.sinergise.java.raster.pyramid.GPTilesProvider;
import com.sinergise.java.raster.pyramid.VersionedTilesProvider;
import com.sinergise.java.swing.map.raster.ImagePyramidLayer;
import com.sinergise.java.util.UtilJava;


public class RasterDatasetPanel extends JPanel {
	static {
		UtilJava.initStaticUtils();
	}
	private class OpenAction extends AbstractAction {
		public OpenAction() {
			putValue(MNEMONIC_KEY, Integer.valueOf(VK_O));
			putValue(NAME, "Open...");
			putValue(SHORT_DESCRIPTION, "Open an existing raster dataset");
		}
		public void actionPerformed(ActionEvent e) {
			fileChooser.setDialogTitle("Open Raster Dataset");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setSelectedFile(getLastPyramidDir());
			fileChooser.setFileFilter(new FileFilter() {
				@Override
				public String getDescription() {
					return "Raster Dataset";
				}
				
				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) return true;
					return f.getName().toUpperCase().endsWith(".XML");
				}
			});
			int option = fileChooser.showOpenDialog(RasterDatasetPanel.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				try {
					openDataset(fileChooser.getSelectedFile());
				} catch(Exception e1) {
					application.showError("Error while opening raster dataset", e1);
				}
			}
		}
	}
	
	protected void openDataset(File datasetFileOrDir) throws Exception {
		RasterAdminApp.storeLastPyramidDir(datasetFileOrDir);
		curDataset = new VersionedTilesProvider(datasetFileOrDir);
		updateForDatasetChange(datasetFileOrDir);
	}

	private void updateForDatasetChange(File baseDir) {
		dirLabel.setText(baseDir.getAbsolutePath());
		
		VersionedRasterDataset info = curDataset.getInfo();
		
		typeCombo.setModel(new DefaultComboBoxModel(info.types));
		
		String[] verNames = info.getVersionNames();
		versionCombo.setModel(new DefaultComboBoxModel(verNames));
		versionCombo.setSelectedIndex(verNames.length-1);
		
		try {
			updateLayerForSelection();
		} catch(IOException ex) {
			application.showError("Error initializing dataset", ex);
		}
	}
	
	private void updateLayerForSelection() throws IOException {
		curPyramid = curDataset.getForVersion((String)versionCombo.getSelectedItem(), (String)typeCombo.getSelectedItem());
		ImagePyramidLayer ipl = new ImagePyramidLayer(curPyramid);
		ipl.setDebug(true);
		application.mapComponent.setDatasetLayer(ipl);
	}

	/**
	 * @wbp.nonvisual location=64,379
	 */
	final JFileChooser fileChooser = new JFileChooser();
	private final Action openAction = new OpenAction();
	private final RasterAdminApp application;
	private VersionedTilesProvider curDataset;
	private GPTilesProvider curPyramid;

	private JComboBox typeCombo;
	private JComboBox versionCombo;
	private JLabel dirLabel;
	
	public RasterDatasetPanel(final RasterAdminApp app) {
		this.application = app;
		setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);
		
		toolBar.add(openAction);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		JLabel lblDirectory = new JLabel("Directory:");
		sl_panel.putConstraint(SpringLayout.NORTH, lblDirectory, 10, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblDirectory, 10, SpringLayout.WEST, panel);
		panel.add(lblDirectory);
		
		JLabel lblVersion = new JLabel("Version:");
		sl_panel.putConstraint(SpringLayout.NORTH, lblVersion, 6, SpringLayout.SOUTH, lblDirectory);
		sl_panel.putConstraint(SpringLayout.WEST, lblVersion, 0, SpringLayout.WEST, lblDirectory);
		panel.add(lblVersion);
		
		JLabel lblImageType = new JLabel("Image Type:");
		sl_panel.putConstraint(SpringLayout.NORTH, lblImageType, 6, SpringLayout.SOUTH, lblVersion);
		sl_panel.putConstraint(SpringLayout.WEST, lblImageType, 0, SpringLayout.WEST, lblVersion);
		panel.add(lblImageType);

		dirLabel = new JLabel("<dir>");
		sl_panel.putConstraint(SpringLayout.NORTH, dirLabel, 0, SpringLayout.NORTH, lblDirectory);
		panel.add(dirLabel);
		
		
		versionCombo = new JComboBox();
		versionCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				try {
					changeFileVersion((String)e.getItem());
				} catch(Exception ex) {
					app.showError("Error changing version", ex);
				}
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, versionCombo, 0, SpringLayout.SOUTH, lblVersion);
		panel.add(versionCombo);
		
		typeCombo = new JComboBox();
		sl_panel.putConstraint(SpringLayout.WEST, dirLabel, 0, SpringLayout.EAST, typeCombo);
		sl_panel.putConstraint(SpringLayout.WEST, versionCombo, 0, SpringLayout.WEST, typeCombo);
		typeCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				try {
					changeFileType((String)e.getItem());
				} catch(IOException ex) {
					app.showError("Error changing tile image type", ex);
				}
			}
		});
		
		sl_panel.putConstraint(SpringLayout.WEST, dirLabel, 0, SpringLayout.WEST, typeCombo);
		sl_panel.putConstraint(SpringLayout.WEST, typeCombo, 6, SpringLayout.EAST, lblImageType);
		sl_panel.putConstraint(SpringLayout.SOUTH, typeCombo, 0, SpringLayout.SOUTH, lblImageType);
		panel.add(typeCombo);
		
		JButton btnSyncScale = new JButton("Sync Scale");
		btnSyncScale.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				syncScale();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, btnSyncScale, 6, SpringLayout.SOUTH, lblImageType);
		sl_panel.putConstraint(SpringLayout.WEST, btnSyncScale, 0, SpringLayout.WEST, lblDirectory);
		panel.add(btnSyncScale);
		
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileHidingEnabled(true);
		fileChooser.setMultiSelectionEnabled(true);
	}

	protected void syncScale() {
		ZoomLevelsPix zooms = curDataset.getInfo().cs.zoomLevels;
		DisplayCoordinateAdapter dca = application.mapComponent.getCoordinateAdapter();
		int level = zooms.nearestZoomLevel(dca.getScale(), dca.pixSizeInMicrons);
		application.mapComponent.getDisplay().setDisplayScale(zooms.scale(level, dca.pixSizeInMicrons));
		application.mapComponent.repaintAll();
	}

	protected void changeFileVersion(String item) throws IOException {
		curPyramid = curDataset.getForVersion(item, (String)typeCombo.getSelectedItem());
		
		ImagePyramidLayer ipl = new ImagePyramidLayer(curPyramid);
		ipl.setDebug(true);
		application.mapComponent.setDatasetLayer(ipl);
	}

	protected void changeFileType(String newType) throws IOException {
		if (newType == null) return;
		if (curPyramid.getFileType().equalsIgnoreCase(curPyramid.getBaseDir().getName())) {
			curPyramid.setBaseDir(new File(curPyramid.getBaseDir().getParentFile(), newType));
		}
		curPyramid.setFileType(newType);
	}
}
