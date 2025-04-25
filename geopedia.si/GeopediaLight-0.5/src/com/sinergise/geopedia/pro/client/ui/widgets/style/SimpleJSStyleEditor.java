package com.sinergise.geopedia.pro.client.ui.widgets.style;

import java.awt.Color;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.core.symbology.FillSymbolizerImpl;
import com.sinergise.geopedia.client.core.symbology.GWTSymbologyUtils;
import com.sinergise.geopedia.client.core.symbology.LineSymbolizerImpl;
import com.sinergise.geopedia.client.core.symbology.PointSymbolizerImpl;
import com.sinergise.geopedia.client.core.symbology.SymbolizerImpl;
import com.sinergise.geopedia.core.constants.Icons;
import com.sinergise.geopedia.core.symbology.LineSymbolizer;
import com.sinergise.geopedia.core.symbology.LineSymbolizer.LineType;
import com.sinergise.geopedia.core.symbology.PaintingPass;
import com.sinergise.geopedia.core.symbology.Symbolizer;
import com.sinergise.geopedia.core.symbology.Symbology;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.theme.layeredit.LayerEditStyle;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;

public class SimpleJSStyleEditor extends FlowPanel {

	private FlowPanel symbEditorsHolder = new FlowPanel();
	
	public SimpleJSStyleEditor() {
		setStyleName("scrollPanel");
		add(symbEditorsHolder);	
	}

	public Symbology getSymbology() {
		GWTSymbologyUtils gsu = new GWTSymbologyUtils();
		Symbolizer[] symbolizers = new SymbolizerImpl[symbEditorsHolder.getWidgetCount()];
		for (int i=0;i<symbolizers.length;i++) {
			SymbolizerEditor se = (SymbolizerEditor)symbEditorsHolder.getWidget(i);
			symbolizers[i]=se.getSymbolizer();
		}
		return  gsu.createSymbology(new PaintingPass[]{gsu.createPaintingPass(symbolizers)});
	}
	public void setSymbology(Symbology symbology) {
		symbEditorsHolder.clear();
		PaintingPass pp = symbology.getPaintingPasses()[0];
		for (Symbolizer sym:pp.getSymbolizers()) {
			SymbolizerImpl symbolizer = (SymbolizerImpl)sym;
			
			if (symbolizer.getClassId() == SymbolizerImpl.ID_LINESYMBOLIZER) {
				LineSymbolizerEditor lse = new LineSymbolizerEditor(symbolizer);
				symbEditorsHolder.add(lse);
			} else if (symbolizer.getClassId() == SymbolizerImpl.ID_POINTSYMBOLIZER) {
				PointSymbolizerEditor pse = new PointSymbolizerEditor(symbolizer);
				symbEditorsHolder.add(pse);
			} else if (symbolizer.getClassId() == SymbolizerImpl.ID_FILLSYMBOLIZER) {
				FillSymbolizerEditor fse = new FillSymbolizerEditor(symbolizer);
				symbEditorsHolder.add(fse);
			}
		}
	}
	
	
	
	
	private static abstract class SymbolizerEditor extends FlowPanel {
		protected static FlowPanel stylePanel(String label, Widget widget) {
			FlowPanel holder = new FlowPanel();
			holder.setStyleName("stylerPanel");
			holder.add(new Label(label));
			holder.add(widget);
			return holder;
		}
		
		public abstract void setSymbolizer(Symbolizer symbolizer);
		public abstract Symbolizer getSymbolizer();
	}
	
	private static class FillSymbolizerEditor extends SymbolizerEditor {
		ColorPicker fillColorPicker;
		OpacityPicker opacityPicker;
		FillSymbolizerImpl fillSymbolizer;
		
		public FillSymbolizerEditor(Symbolizer symbolizer) {
			fillColorPicker = new ColorPicker(new Color[]{ 
					new Color(0xf31c1c),
					new Color(0xe2a92f),
					new Color(0x49e030),
					new Color(0x29ca9e),
					new Color(0x27cbde),
					new Color(0x2775de),
					new Color(0x0000ff),
					new Color(0xa827de),
					new Color(0xde27b1),
					new Color(0xffffff)
			});
			SGFlowPanel fillCont = new SGFlowPanel("filling");
			fillCont.add(new Image(LayerEditStyle.INSTANCE.polyFillBg()));
			
			opacityPicker = new OpacityPicker();
			fillCont.add(stylePanel(GeopediaTerms.INSTANCE.fillBackground()+":", fillColorPicker));
			fillCont.add(stylePanel(GeopediaTerms.INSTANCE.opacityBackground()+":", opacityPicker));
			add(fillCont);
			
			setStyleName("symbolizerEditor fill");
			setSymbolizer(symbolizer);
		}
		
		@Override
		public void setSymbolizer(Symbolizer symbolizer) {
			if (((SymbolizerImpl)symbolizer).getClassId() != SymbolizerImpl.ID_FILLSYMBOLIZER)
				throw new RuntimeException("Illegal symbolizer!");
			
			fillSymbolizer = (FillSymbolizerImpl)symbolizer;			
			fillColorPicker.setValue(fillSymbolizer.getFillBackground());
			opacityPicker.setValue((int) (fillSymbolizer.getOpacity()*100.0));
		}

		@Override
		public Symbolizer getSymbolizer() {
			fillSymbolizer.setOpacity(opacityPicker.getValue()/100.0);
			fillSymbolizer.setFillBackground(fillColorPicker.getValue());
			return fillSymbolizer;
		}
		
	}
	
	private static class PointSymbolizerEditor extends SymbolizerEditor {

		private ColorPicker fillPicker;
		private OpacityPicker opacityPicker;
		private SymbolPicker symbolPicker;
		private AbstractLinearSelector<Integer> symbolSizePicker;
		private PointSymbolizerImpl pointSymbolizer; 
		
		public  PointSymbolizerEditor(SymbolizerImpl symbolizer) {
			symbolPicker = new SymbolPicker();
			
			fillPicker = new ColorPicker();
			opacityPicker = new OpacityPicker();
			
			symbolSizePicker = new AbstractLinearSelector<Integer>(new Integer[]{Icons.Sizes.SYM_SMALL, Icons.Sizes.SYM_MEDIUM, Icons.Sizes.SYM_LARGE}) {

				@Override
				protected void renderItemAnchor(Anchor anchor, Integer item) {
					switch (item) {
						case Icons.Sizes.SYM_SMALL:
							anchor.setHTML("<b>S</b>");
							anchor.setStyleName("small");
						break;
						case Icons.Sizes.SYM_MEDIUM:
							anchor.setHTML("<b>M</b>");
							anchor.setStyleName("medium");
						break;
						case Icons.Sizes.SYM_LARGE:
							anchor.setHTML("<b>L</b>");
							anchor.setStyleName("large");
						break;

					}
				}
				
				@Override
				protected void onAfterItemSelected(Integer item) {
					if (selectedItem==null) {
						setValue(Icons.Sizes.SYM_MEDIUM);
					}
				}
			};
			symbolSizePicker.addStyleName("symbolSizePicker");
			
			add(new Image(LayerEditStyle.INSTANCE.symbolBg()));
			add(stylePanel(GeopediaTerms.INSTANCE.symbol()+":", symbolPicker));
			add(stylePanel(GeopediaTerms.INSTANCE.color()+":", fillPicker));
			add(stylePanel(GeopediaTerms.INSTANCE.opacity()+":", opacityPicker));
			add(stylePanel(GeopediaTerms.INSTANCE.size()+":", symbolSizePicker));
			
			setStyleName("symbolizerEditor point");
			setSymbolizer(symbolizer);
		}
		
		@Override
		public void setSymbolizer(Symbolizer symbolizer) {
			if (((SymbolizerImpl)symbolizer).getClassId() != SymbolizerImpl.ID_POINTSYMBOLIZER)
				throw new RuntimeException("Illegal symbolizer!");
			pointSymbolizer = (PointSymbolizerImpl)symbolizer;
			
			fillPicker.setValue(pointSymbolizer.getFill());
			opacityPicker.setValue((int) (pointSymbolizer.getOpacity()*100.0));
			symbolPicker.setValue(pointSymbolizer.getSymbolId());
			symbolSizePicker.setValue(new Double(pointSymbolizer.getSize()).intValue());
			
		}

		@Override
		public Symbolizer getSymbolizer() {
			pointSymbolizer.setSymbolId(symbolPicker.getValue());
			pointSymbolizer.setSize(symbolSizePicker.getValue());
			pointSymbolizer.setFill(fillPicker.getValue());
			pointSymbolizer.setOpacity(opacityPicker.getValue()/100.0);
			return pointSymbolizer;
		}
		
	}
	private static class LineSymbolizerEditor extends SymbolizerEditor {
		private ColorPicker strokePicker;
		private OpacityPicker opacityPicker;
		private LineWidthPicker strokeWidthPicker;
		private EnumLinearSelector<LineSymbolizer.LineType> lineTypePicker;
		private LineSymbolizerImpl lineSymbolizer;
		
		public  LineSymbolizerEditor(SymbolizerImpl symbolizer) {
			lineTypePicker = new EnumLinearSelector<LineSymbolizer.LineType>(LineType.values());
			lineTypePicker.addStyleName("lineStylePicker");			
			strokePicker = new ColorPicker();	
			opacityPicker = new OpacityPicker();
			strokeWidthPicker = new LineWidthPicker();
			add(new Image(LayerEditStyle.INSTANCE.lineBg()));
			add(stylePanel(GeopediaTerms.INSTANCE.style()+":", lineTypePicker));
			add(stylePanel(GeopediaTerms.INSTANCE.color()+":", strokePicker));
			add(stylePanel(GeopediaTerms.INSTANCE.opacity()+":", opacityPicker));
			add(stylePanel(GeopediaTerms.INSTANCE.width()+":", strokeWidthPicker));
			
			setStyleName("symbolizerEditor line");
			setSymbolizer(symbolizer);
		}

		@Override
		public void setSymbolizer(Symbolizer symbolizer) {
			if (((SymbolizerImpl)symbolizer).getClassId() != SymbolizerImpl.ID_LINESYMBOLIZER)
				throw new RuntimeException("Illegal symbolizer!");
			lineSymbolizer = (LineSymbolizerImpl)symbolizer;
			
			strokePicker.setValue(lineSymbolizer.getStroke());	
			opacityPicker.setValue((int) (lineSymbolizer.getOpacity()*100.0));
			strokeWidthPicker.setValue(new Double(lineSymbolizer.getStrokeWidth()).longValue());
			lineTypePicker.setValue(lineSymbolizer.getLineType());

		}

		@Override
		public Symbolizer getSymbolizer() {
			lineSymbolizer.setLineType((LineType) lineTypePicker.getValue());
			lineSymbolizer.setStrokeWidth(strokeWidthPicker.getValue());
			lineSymbolizer.setStroke(strokePicker.getValue());
			lineSymbolizer.setOpacity(opacityPicker.getValue()/100.0);
			return lineSymbolizer;
		}
	}
}
