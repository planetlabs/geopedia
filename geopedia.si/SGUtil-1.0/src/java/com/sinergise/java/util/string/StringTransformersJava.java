/*
 *
 */
package com.sinergise.java.util.string;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;

import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.common.util.string.StringTransformer;
import com.sinergise.java.util.settings.ResolvedTypeUtil;

public class StringTransformersJava {

	@SuppressWarnings("rawtypes")
	public static class ResTyp implements StringTransformer<ResolvedType> {
		private static final long serialVersionUID = 1L;

		@Override
		public String store(final ResolvedType obj) {
			return ResolvedTypeUtil.writeToString(obj, null, null);
		}
		
		@Override
		public ResolvedType valueOf(final String str) {
			return ResolvedTypeUtil.readFromString(str, null, null);
		}
	}

	public static class Clr implements StringTransformer<Color> {
		private static final long serialVersionUID = 1L;

		@Override
		public String store(final Color obj) {
			return Integer.toHexString(obj.getRGB());
		}
		
		@Override
		public Color valueOf(final String str) {
			return new Color((int)Long.parseLong(str, 16), str.length() >= 6);
		}
	}

	public static class Rect implements StringTransformer<Rectangle2D> {
		private static final long serialVersionUID = 1L;

		@Override
		public String store(final Rectangle2D obj) {
			return obj.getMinX() + " " + obj.getMinY() + " " + obj.getWidth() + " " + obj.getHeight();
		}
		
		@Override
		public Rectangle2D valueOf(final String str) {
			final String[] xywh = str.split("\\w*");
			try {
				// Check for integer Rectangle
				if (str.indexOf('.') < 0 && str.indexOf(',') < 0 && str.indexOf('E') < 0 && str.indexOf('e') < 0) {
					return new Rectangle(Integer.parseInt(xywh[0]), Integer.parseInt(xywh[1]), Integer.parseInt(xywh[2]), Integer.parseInt(xywh[3]));
				}
				return new Rectangle2D.Double(Double.parseDouble(xywh[0]), Double.parseDouble(xywh[1]), Double.parseDouble(xywh[2]),
				                              Double.parseDouble(xywh[3]));
			} catch(final Exception e) {
				return null;
			}
		}
	}

	public static class Dim implements StringTransformer<Dimension> {

		private static final long serialVersionUID = 1L;

		@Override
		public String store(final Dimension obj) {
			return obj.width + " " + obj.height;
		}
		
		@Override
		public Dimension valueOf(final String str) {
			final String[] wh = str.split("\\w*");
			try {
				return new Dimension(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
			} catch(final Exception e) {
				return null;
			}
		}
	}

	public static class Fnt implements StringTransformer<Font> {

		private static final long serialVersionUID = 1L;

		@Override
		public String store(final Font obj) {
			String strStyle;
			if (obj.isBold()) {
				strStyle = obj.isItalic() ? "bolditalic" : "bold";
			} else {
				strStyle = obj.isItalic() ? "italic" : "plain";
			}
			return obj.getName() + "-" + strStyle + "-" + obj.getSize();
		}
		
		@Override
		public Font valueOf(final String str) {
			try {
				return Font.decode(str);
			} catch(final Exception e) {
				return null;
			}
		}
	}
	
	
	public static class TrFile implements StringTransformer<File> {

		private static final long serialVersionUID = 1L;

		@Override
		public String store(File f) {
			return f.getPath();
		}
		
		@Override
		public File valueOf(String str) {
			return new File(str);
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	public static class Cls implements StringTransformer<Class> {

		private static final long serialVersionUID = 1L;

		@Override
		public String store(final Class obj) {
			return obj.getName();
		}
		
		@Override
		public Class valueOf(final String str) {
			try {
				return Class.forName(str);
			} catch(final ClassNotFoundException e) {
				return null;
			}
		}
	}
}
