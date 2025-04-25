package java.awt;

import java.io.Serializable;

import com.sinergise.common.util.math.ColorUtil;

public class Color implements Paint, Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final Color BLACK = new Color(0x000000);
	public static final Color BLUE = new Color(0x0000FF);
	public static final Color CYAN = new Color(0x00FFFF);
	public static final Color DARK_GRAY = new Color(64, 64, 64);
	public static final Color GRAY = new Color(0x808080);
	public static final Color GREEN = new Color(0x00FF00);
	public static final Color LIGHT_GRAY = new Color(192, 192, 192);
	public static final Color MAGENTA = new Color(0xFF00FF);
	public static final Color ORANGE = new Color(255, 200, 0);
	public static final Color PINK = new Color(255, 175, 175);
	public static final Color RED = new Color(0xFF0000);
	public static final Color WHITE = new Color(0xFFFFFF);
	public static final Color YELLOW = new Color(0xFFFF00);
	
	int value;
	
	protected Color() {
		//GWT serialization
	}
	
    public Color(int rgb) {
    	value = 0xFF000000 | rgb;
	}

    public Color(int rgb, boolean hasAlpha) {
    	if (hasAlpha) {
    		value = rgb;
    	} else {
    		value = 0xFF000000 | rgb;
    	}
	}

	public Color(int r, int g, int b, int a) {
		this((checkVal(a) << 24) | (checkVal(r) << 16) | (checkVal(g) << 8) | checkVal(b), true);
	}

	public Color(int r, int g, int b) {
		this(r, g, b, 0xFF);
	}

	public Color(float r, float g, float b) {
		this(ColorUtil.fromDouble4(new double[]{1, r, g, b}), true);
	}
	
	public Color(float r, float g, float b, float a) {
		this(ColorUtil.fromDouble4(new double[]{a, r, g, b}), true);
	}

	private static int checkVal(int a) {
		if (a < 0 || a > 255) throw new IllegalArgumentException("Value out of range [0-255], was "+a);
		return a;
	}

	public int getTransparency() {
        int alpha = getAlpha();
        if (alpha == 0xff) {
            return Transparency.OPAQUE;
        } else if (alpha == 0) {
            return Transparency.BITMASK;
        } else {
            return Transparency.TRANSLUCENT;
        }
    }

	public int getAlpha() {
		return (value >>> 24);
	}

	public int getRed() {
		return (value >>> 16) & 0xFF;
	}

	public int getGreen() {
		return (value >>> 8) & 0xFF;
	}

	public int getBlue() {
		return value & 0xFF;
	}

	public int getRGB() {
		return value;
	}

	public static Color decode(String nm) throws NumberFormatException {
		return new Color(Integer.decode(nm).intValue());
	}

	public static Color getHSBColor(float h, float s, float b) {
        return new Color(HSBtoRGB(h, s, b));
	}
	
    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
            case 0:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (t * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 1:
                r = (int) (q * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 2:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (t * 255.0f + 0.5f);
                break;
            case 3:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (q * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 4:
                r = (int) (t * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 5:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (q * 255.0f + 0.5f);
                break;
            }
		}
		return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
	}

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        
        float saturation;
        if (cmax != 0) {
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        } else {
            saturation = 0;
        }
        
        float hue;
        if (saturation == 0) {
            hue = 0;
        } else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax) {
                hue = bluec - greenc;
            } else if (g == cmax) {
                hue = 2.0f + redc - bluec;
            } else {
                hue = 4.0f + greenc - redc;
            }
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = cmax / 255.0f;
		return hsbvals;
	}
}
