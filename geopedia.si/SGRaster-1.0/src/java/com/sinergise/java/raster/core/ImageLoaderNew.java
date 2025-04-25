package com.sinergise.java.raster.core;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.Arrays;

import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;

public class ImageLoaderNew {
	public static final int		INTERPOLATION_NEAREST		= Interpolation.INTERP_NEAREST;
	public static final int		INTERPOLATION_BILINEAR		= Interpolation.INTERP_BILINEAR;
	public static final int		INTERPOLATION_BICUBIC		= Interpolation.INTERP_BICUBIC;
	public static final int		INTERPOLATION_BICUBIC_2		= Interpolation.INTERP_BICUBIC_2;
	public static final int		INTERPOLATION_SMART_RESIZE	= INTERPOLATION_BICUBIC_2 + 1;

	private ImageLoaderNew() {}

	public static RenderedImage scale(RenderedImage input, Dimension targetDimension, int interpolation) {
		float scalex = (float)((double)targetDimension.width / (double)input.getWidth());
		float scaley = (float)((double)targetDimension.height / (double)input.getHeight());
		if (targetDimension.width <= 0 || targetDimension.height <= 0) {
			return new BufferedImage(targetDimension.width, targetDimension.height, BufferedImage.TYPE_BYTE_BINARY);
		}
		return scale(input, scalex, scaley, interpolation);
	}

	public static RenderedImage scale(RenderedImage input, float scalex, float scaley, int interpolation) {
		if (scalex == 1f && scaley == 1f) {
			return input;
		} else if (scalex <= 0f || scaley <= 0f) {}
		if (interpolation != INTERPOLATION_NEAREST) {
			// SampleModel sm = input.getSampleModel();
			ColorModel cm = input.getColorModel();
			boolean isIndexed = (cm instanceof IndexColorModel);
			boolean hasAlpha = cm.hasAlpha();
			if (isIndexed) {
				IndexColorModel icm = (IndexColorModel)cm;
				boolean isColor = false;
				if (!hasAlpha) {
					int len = icm.getMapSize();
					byte[] rd = new byte[len];
					byte[] gr = new byte[len];
					byte[] bl = new byte[len];
					icm.getReds(rd);
					icm.getGreens(gr);
					icm.getBlues(bl);
					if (!Arrays.equals(rd, gr)) isColor = true;
					else if (!Arrays.equals(rd, bl)) isColor = true;
				}
				BufferedImage bufImg = null;
				if (isColor) {
					if (hasAlpha) {
						// bufImg=new BufferedImage(input.getWidth(), input.getHeight(),
						// BufferedImage.TYPE_4BYTE_ABGR);
					} else {
						// bufImg=new BufferedImage(input.getWidth(), input.getHeight(),
						// BufferedImage.TYPE_3BYTE_BGR);
					}
				} else {
					if (hasAlpha) {
						bufImg = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
					} else {
						bufImg = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
					}
				}
				if (bufImg != null) {
					bufImg.createGraphics().drawRenderedImage(input, new AffineTransform());
					input = bufImg;
				}
			}
		}

		if (interpolation >= INTERPOLATION_SMART_RESIZE) {
			float tresh = 0.51f;
			if (scalex < tresh || scaley < tresh) {

				// 841 ms
				float tempScaleX = (float)Math.sqrt(scalex);
				float tempScaleY = (float)Math.sqrt(scaley);

				// 731 ms
				// float tempScaleX=scalex/tresh;
				// float tempScaleY=scaley/tresh;

				// float tempScaleX=tresh/2;
				// float tempScaleY=tresh/2;

				int interp = INTERPOLATION_BILINEAR;
				if (scalex / tempScaleX < 0.2f && scaley / tempScaleY < 0.2f) {
					interp = INTERPOLATION_NEAREST;
				}
				RenderedImage img = scale(input, tempScaleX, tempScaleY, interp);
				img = scale(img, scalex / tempScaleX, scaley / tempScaleY, interpolation);
				return img;
			}
			if ((0.8f < scalex && scalex < 1f) || (0.8f < scaley && scaley < 1f)) {
				interpolation = INTERPOLATION_BICUBIC;
			} else {
				interpolation = INTERPOLATION_BILINEAR;
			}
		}
		// NEAREST: 60 ms
		// BILINEAR: 220 ms
		// BICUBIC: 501 ms
		// BICUBIC2: 501 ms
		ParameterBlock pb1 = new ParameterBlock();
		pb1.addSource(input);
		pb1.add(scalex);
		pb1.add(scaley);
		pb1.add(0f);
		pb1.add(0f);
		pb1.add(Interpolation.getInstance(interpolation));
		if (interpolation == Interpolation.INTERP_NEAREST) {
			return JAI.create("scale", pb1);
		}
		RenderingHints rh = new RenderingHints(null);
		rh.put(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_REFLECT));
		rh.put(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.TRUE);
		return JAI.create("scale", pb1, rh);
	}
}
