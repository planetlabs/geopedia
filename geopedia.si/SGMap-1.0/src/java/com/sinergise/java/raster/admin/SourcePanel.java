package com.sinergise.java.raster.admin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.util.ImageUtil;
import com.sinergise.common.util.io.FileUtil;
import com.sinergise.common.util.lang.SGProgressMonitorImpl;
import com.sinergise.java.raster.pyramid.SourceImageProvider;
import com.sinergise.java.swing.map.raster.ImageCollectionLayer;
import com.sinergise.java.util.io.FileUtilJava;

public class SourcePanel extends JPanel {
	public static final java.io.FileFilter IMAGES_FILTER = new java.io.FileFilter() {
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || ImageUtil.isImageFile(f.getName());
		}
	};
	
	private static class ImageFileSystemModel extends DefaultTreeModel {
		public ImageFileSystemModel(File root) {
			super(new FileTreeNode(root), true);
		}
		public ImageFileSystemModel(File[] allImages) {
			super(new DefaultMutableTreeNode(), true);
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Object(), true);
			for (File f : allImages) {
				root.add(new FileTreeNode(f));
			}
			setRoot(root);
		}
	}
	
	private class SelectDirAction extends AbstractAction {
		public SelectDirAction() {
			putValue(NAME, "Select...");
			putValue(SHORT_DESCRIPTION, "Select the source folder");
		}
		public void actionPerformed(ActionEvent e) {
			fileChooser.setCurrentDirectory(RasterAdminApp.getLastSourceDir());
			fileChooser.setSelectedFile(null);
			fileChooser.setDialogTitle("Select Source Folder");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setFileFilter(new FileFilter() {
				@Override
				public String getDescription() {
					return "Raster Dataset Source";
				}
				
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().toUpperCase().endsWith(".TXT");
				}
			});
			if (fileChooser.showOpenDialog(SourcePanel.this) == JFileChooser.APPROVE_OPTION) {
				File selFile = fileChooser.getSelectedFile();
				RasterAdminApp.storeLastSource(selFile);
				setSource(selFile);
			}
		}
	}
	
	private final class UpDirAction extends AbstractAction {
		public UpDirAction() {
			super("../");
			setEnabled(sourceFileOrDir != null && sourceFileOrDir.isDirectory());
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (sourceFileOrDir != null && sourceFileOrDir.isDirectory()) {
				setSource(sourceFileOrDir.getParentFile());
			}
		}
	}
	
	private static class FileTreeNode implements TreeNode, MutableTreeNode {
		List<FileTreeNode> children = null;
		final File data;
		final FileTreeNode parent;
		
		public FileTreeNode(File data) {
			this(data, null);
		}
		
		public FileTreeNode(File data, FileTreeNode parent) {
			this.data = data;
			this.parent = parent;
		}
		
		protected void checkChildren() {
			if (children != null) return;
			File[] chlds = data.listFiles(IMAGES_FILTER);
			if (chlds == null) {
				children = Collections.emptyList();
				return;
			}
			final FileTreeNode[] childArr = new FileTreeNode[chlds.length];
			for (int i = 0; i < chlds.length; i++) {
				childArr[i] = new FileTreeNode(chlds[i], this);
			}
			children = Arrays.asList(childArr); 
		}
		
		@Override
		public Enumeration<FileTreeNode> children() {
			checkChildren();
			return Collections.enumeration(children);
		}
		@Override
		public boolean getAllowsChildren() {
			return data.isDirectory();
		}
		public TreeNode getChildAt(int childIndex) {
			checkChildren();
			return children.get(childIndex);
		}
		
		@Override
		public int getChildCount() {
			checkChildren();
			return children.size();
		}
		
		public int getIndex(TreeNode node) {
			return children.indexOf(node);
		}
		
		@Override
		public TreeNode getParent() {
			return parent;
		}
		@Override
		public boolean isLeaf() {
			return data.isDirectory();
		}
		
		@Override
		public String toString() {
			if (parent == null) return data.toString();
			return data.getName();
		}

		@Override
		public void insert(MutableTreeNode child, int index) {
		}

		@Override
		public void remove(int index) {
		}

		@Override
		public void remove(MutableTreeNode node) {
		}

		@Override
		public void setUserObject(Object object) {
		}

		@Override
		public void removeFromParent() {
		}

		@Override
		public void setParent(MutableTreeNode newParent) {
		}
	}

	/**
	 * @wbp.nonvisual location=64,379
	 */
	final JFileChooser fileChooser = new JFileChooser();

	private final RasterAdminApp application;

	SourceImageProvider imgProvider;
	
	SourceControlPanel controlPanel = new SourceControlPanel();
	
	JTree tree;
	
	File sourceFileOrDir;
	
	final UpDirAction upDirAction;
	
	public SourcePanel(RasterAdminApp app) {
		this.application = app;
		setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);
		toolBar.add(upDirAction = new UpDirAction());
		toolBar.add(new SelectDirAction());
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		
		tree = new JTree((TreeNode)null);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				FileTreeNode node = (FileTreeNode)e.getPath().getLastPathComponent();
				dirSelected(node.data);
			}
		});
		tree.setCellRenderer(new DefaultTreeCellRenderer(){
			@Override
			public Component getTreeCellRendererComponent(JTree rendTree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean cellHasFocus) {
				return super.getTreeCellRendererComponent(rendTree, value, sel, expanded, leaf, row, cellHasFocus);
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					FileTreeNode node = (FileTreeNode)tree.getSelectionPath().getLastPathComponent();
					setSource(node.data);
				}
			}
		});
		splitPane.setLeftComponent(new JScrollPane(tree));
		
		splitPane.setRightComponent(controlPanel);
	}

	protected void dirSelected(File dir) {
		System.out.println(dir);
	}

	public void setSource(File fileOrDir) {
		upDirAction.setEnabled(false);
		this.sourceFileOrDir = fileOrDir;
		if (fileOrDir.isDirectory()) {
			setSourceDir(fileOrDir);
		} else {
			setSourceFile(fileOrDir);
		}
		
		application.mapComponent.setSourcesLayer(new ImageCollectionLayer(imgProvider.getImages()));
		upDirAction.setEnabled(true);
	}

	private void setSourceFile(File file) {
		try {
			if (FileUtilJava.isSuffixIgnoreCase(file, "xml")) {
				setSourcesXML(file);
			} else {
				setSourcesTxtList(file);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setSourcesTxtList(File file) throws IOException {
		File[] fls = SourceImageProvider.readSourcesFile(file);
		tree.setModel(new ImageFileSystemModel(fls));
		tree.setRootVisible(false);
		ProgressMonitor pm = new ProgressMonitor(this, "Analyzing source images", "File 1/", 0, 100);
		imgProvider = new SourceImageProvider(fls, CRS.NONAME_WORLD_CRS);
		scan(pm);
	}

	private void setSourcesXML(File file) {
		imgProvider = new SourceImageProvider(new File[] {file}, CRS.NONAME_WORLD_CRS);
	}

	public void setSourceDir(File dir) {
		tree.setModel(new ImageFileSystemModel(dir));
		tree.setRootVisible(false);
		ProgressMonitor pm = new ProgressMonitor(this, "Scanning source folders", "File 1/", 0, 100);
		imgProvider = new SourceImageProvider(new File[]{dir}, CRS.NONAME_WORLD_CRS);
		scan(pm);
	}

	public void scan(final ProgressMonitor pm) {
		Runnable rnbl = new Runnable() {
			@Override
			public void run() {
				try {
					imgProvider.initializeSources(new SGProgressMonitorImpl() {
						@Override
						protected void progressUpdated(final double fractionComplete) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									pm.setProgress(Math.round(100*(float)fractionComplete));
								}
							});
						}
						
						@Override
						protected void messageUpdated(final String newMessage) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									pm.setNote(newMessage);
								}
							});
						}
					});
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							pm.close();
						}
					});
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};
		Thread thrd = new Thread(rnbl);
		thrd.start();
	}

}
