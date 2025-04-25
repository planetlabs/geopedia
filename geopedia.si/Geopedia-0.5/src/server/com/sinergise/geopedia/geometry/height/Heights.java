package com.sinergise.geopedia.geometry.height;

import java.io.IOException;

import com.sinergise.geopedia.config.ServerConfiguration;

public class Heights {
        
    
    DMVHolder data = null;
    
    public void readData(ServerConfiguration config ) throws IOException {
    	
    	String dataFile = config.getStringProperty(ServerConfiguration.PROP_DMVFILE);
        if (dataFile == null) {
            return;
        }
        data = DMVHolder.loadRaw(dataFile, 12.5);
    }
    
    public double calcZ(double x, double y) {
        return calcZ(x, y, 5);
    }
    
    public double calcZ(double x, double y, double step) {
        if (data == null)
            return step * Math.sin((x + y) / step);
        
        long copyMinX = (long) Math.floor(x / data.step - step);
        long copyMaxX = (long) Math.ceil(x / data.step + step);
        long copyMinY = (long) Math.floor(y / data.step - step);
        long copyMaxY = (long) Math.ceil(y / data.step + step);
        
        DMVHolder subset = data.subset(copyMinX, copyMaxX, copyMinY, copyMaxY);
        if (subset == null)
            return Double.NaN;
        
        Interpolator i = new Interpolator(subset);
        
        double[] loc = new double[3];
        
        if (!i.eval(x, y, loc))
            return Double.NaN;
        
        return loc[0];
    }

	public DMVHolder getData() {
		return data;
	}
    
    
    /*
     * 
    static int MARGIN = 4;
    static int OUTER_BORDER_WIDTH = 1;
    static int LINE_WIDTH = 2;
    static int COLOR_HEIGHT = 0xFF0000FF;
    static int COLOR_BORDER = 0xFF000000;
    static int COLOR_VERTICALS = 0xFFFF9090;
    static int COLOR_HORIZONTALS = 0xFFCCCCCC;
    static int COLOR_LINE = 0xFF0030C0;
    static int COLOR_TEXT = 0xFF808080;
    static int LEFTTEXT_WIDTH = 44;
    static int RIGHTTEXT_WIDTH = 100;
    static int TEXT_HEIGHT = 15;
    
    static Font fontPlain = new Font("SansSerif", 0, 10);
    static Font fontBold = fontPlain.deriveFont(Font.BOLD, 11);
    
    public static BufferedImage calcProfile(LineString line, int w, int h,
            Point from, Point to, Point current, int index, boolean addStats, boolean normalize) {
        
        int paddingW = (2 * MARGIN + 2 * OUTER_BORDER_WIDTH + LEFTTEXT_WIDTH);
        if (addStats) {
            paddingW += RIGHTTEXT_WIDTH;
        }
        int paddingH = (2 * MARGIN + 2 * OUTER_BORDER_WIDTH + TEXT_HEIGHT);
        
//        if (line == null || w < paddingW + 100 || h < paddingH + 160)
        if (line == null || w < paddingW || h < paddingH)
            throw new IllegalArgumentException();
        if (data == null)
            return null;
        
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Graphics2D textg = img.createGraphics();
        textg.setFont(fontPlain);
        textg.setColor(new Color(COLOR_TEXT, true));
        
        double totalLen = line.getLength();
        if (!(totalLen > 0)) // handles NaN too
            return null;
        
        int graphW = w - paddingW;
        int graphH = h - paddingH;
        double[] coords = line.coords;
        double[] zs = new double[graphW];
        
        double minZ = 10000000;
        double maxZ = -10000000;
        
        double cumUp = 0;
        double cumDown = 0;
        double cumUpDist = 0;
        double cumDownDist = 0;
        
        double startZ = calcZ(coords[0], coords[1]);
        double endZ = calcZ(coords[coords.length - 2], coords[coords.length - 1]);
        
        double prevX = coords[0];
        double prevY = coords[1];
        double prevZ = calcZ(prevX, prevY);
        double currPosX = 0;
        double prevLen = 0;
        double testTotLen = 0;
        
        for (int p = 2; p < coords.length;) {
            double thisX = coords[p++];
            double thisY = coords[p++];
            
            double len = Math.hypot(thisX - prevX, thisY - prevY);
            
            double newPosX = currPosX + len;
            
            int iCurrX = (int) (currPosX * graphW / totalLen);
            int iNewX = (int) (newPosX * graphW / totalLen);
            if (iNewX >= graphW)
                iNewX = graphW - 1;
            
            int n = iNewX - iCurrX;
            double deltaX = thisX - prevX;
            double deltaY = thisY - prevY;
            
            for (int outOffset = 0; outOffset <= n; outOffset++) {
                double fac = n == 0 ? 0.5 : (double) outOffset / n;
                double zx = prevX + fac * deltaX;
                double zy = prevY + fac * deltaY;
                double curLen = currPosX + len * fac;
                
                double z = zs[iCurrX + outOffset] = calcZ(zx, zy);
                double deltaLen = curLen - prevLen;
                testTotLen += deltaLen;
                if (!Double.isNaN(z)) {
                    if (z > prevZ) {
                        // Calculate cumulative height (up and down);
                        cumUp += z - prevZ;
                        // Calculate real total distance (not projected)
                        cumUpDist += Math.sqrt(deltaLen * deltaLen + Math.pow(z - prevZ, 2));
                    } else {
                        cumDown += prevZ - z;
                        cumDownDist += Math.sqrt(deltaLen * deltaLen + Math.pow(z - prevZ, 2));
                    }
                    
                    // Setting min and max height
                    if (z < minZ)
                        minZ = z;
                    if (z > maxZ)
                        maxZ = z;
                    prevZ = z;
                    prevLen = curLen;
                }
            }
            prevX = thisX;
            prevY = thisY;
            currPosX = newPosX;
        }
        // System.out.println("Test total length: "+testTotLen+" totLen: "+totalLen);
        
        if (minZ > maxZ)
            return null;
        
        double drawZmin, drawZmax;
        
        if (minZ == maxZ) {
            drawZmin = minZ - 1;
            drawZmax = minZ + 1;
        } else {
            double spanZ = maxZ - minZ;
            double factZ = spanZ / (graphH - 10);
            drawZmin = minZ - 5 * factZ;
            drawZmax = maxZ + 5 * factZ;
        }
        
        if (normalize) {
            double zSpan = maxZ - minZ;
            double steepness = (zSpan) / totalLen;
            double ratio = (double) graphH / graphW;
            double normalZSpan = totalLen * ratio / 2;
            if (zSpan <= normalZSpan) {
                if (steepness < 0.1) {
                    normalZSpan /= Math.min(3, 0.1 / steepness);
                }
                double mid = 0.5 * (drawZmin + drawZmax);
                drawZmax = mid + normalZSpan * 0.5;
                drawZmin = mid - normalZSpan * 0.5;
            }
        }
        
        double drawZspan = drawZmax - drawZmin;
        double zStep = Math.max(0.1, roundToDecimal(drawZspan / (graphH / 30)));
        
        double zScale = graphH / drawZspan;
        
        int graphX = MARGIN + LEFTTEXT_WIDTH + OUTER_BORDER_WIDTH;
        int graphY = h - MARGIN - TEXT_HEIGHT - OUTER_BORDER_WIDTH;
        
        int minGuide = (int) Math.floor(drawZmin / zStep);
        int maxGuide = (int) Math.ceil(drawZmax / zStep);
        
        g.setStroke(new BasicStroke(1,
                                    BasicStroke.CAP_SQUARE,
                                    BasicStroke.JOIN_MITER,
                                    10,
                                    new float[] { 0, 2 },
                                    0));
        g.setColor(new Color(COLOR_HORIZONTALS, true));
        
        for (int i = minGuide; i <= maxGuide; i++) {
            double z = i * zStep;
            if (z < drawZmin || z > drawZmax)
                continue;
            int y = graphY - (int) ((z - drawZmin) * zScale);
            
            g.drawLine(graphX, y, graphX + graphW, y);
            
            if (z < 0 || z > 4000)
                continue;
            
            String txt = textFor(i, zStep);
            Rectangle2D bounds = textg.getFontMetrics().getStringBounds(txt, textg);
            float x = (float) (MARGIN + LEFTTEXT_WIDTH - bounds.getMaxX());
            float ty = (float) (graphY - ((z - drawZmin) * zScale) - bounds.getCenterY());
            textg.drawString(txt, x - 1, ty);
        }
        
        
        double lenScale = graphW / totalLen;
        double lenStep = roundToDecimal(totalLen / (graphW / 50));
        ArrayList<int[]> list = new ArrayList<int[]>();
        for (int a = 1; a < 1000; a++) {
            int x = (int) (graphX + a * lenStep * lenScale);
            if (x >= graphX + graphW)
                break;
            list.add(new int[] { x, a });
        }
        int lastTextX = graphX + graphW;
        for (int e = list.size() - 1; e >= 0; e--) {
            int[] xa = (list.get(e));
            int x = xa[0];
            int a = xa[1];
            g.drawLine(x, MARGIN, x, graphY);
            
            String txt = lenTextFor(a * lenStep, totalLen);
            Rectangle2D bounds = textg.getFontMetrics().getStringBounds(txt, textg);
            float tx =
                       Math.min((float) (x - bounds.getCenterX()),
                                (float) (lastTextX - 2 - bounds.getWidth()));
            float ty = (float) (h - MARGIN - 0.5f * TEXT_HEIGHT - bounds.getCenterY());
            textg.drawString(txt, tx, ty);
            lastTextX = (int) tx;
        }
        
        Graphics2D noneg = null;
        
        g.setColor(new Color(COLOR_LINE, true));
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int x = 1; x < graphW; x++) {
            double z = zs[x];
            if (isNaN(z)) {
                if (noneg == null) {
                    noneg = img.createGraphics();
                    noneg.setColor(new Color(0x30000000, true));
                }
                noneg.drawLine(graphX + x, MARGIN, graphX + x, graphY);
                continue;
            }
            int px = x - 1;
            double tz = zs[px];
            if (!isNaN(tz)) {
                int prevy = graphY - (int) ((tz - drawZmin) * zScale);
                int thisy = graphY - (int) ((z - drawZmin) * zScale);
                g.drawLine(graphX + px, prevy, graphX + x, thisy);
            }
        }
        
        // display cicle
        if (from!=null && to!=null && current!=null) {
            double deltaX = current.x - from.x;
            double deltaY = current.y - from.y;
            double partialSegment = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
            
            double[] coordsSoFar = Arrays.copyOf(line.coords, (index+1)*2);
            LineString completeSegments = new LineString(coordsSoFar);
            
            double distanceSoFar = completeSegments.getLength() + partialSegment;
            double faktor = distanceSoFar/line.getLength();
            
            double altitude = calcZ(current.x, current.y);
            int displayX = graphX + (int)(graphW * faktor);
            int displayY = graphY - (int) ((altitude - drawZmin) * zScale);
            
            g.setColor(Color.RED);
            
            int radius = 6;
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(displayX, displayY - radius / 2, displayX, displayY + radius / 2);
            g.drawLine(displayX - radius / 2, displayY, displayX + radius / 2, displayY);
            
            radius = 22;
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(displayX - radius / 2, displayY - radius / 2, radius, radius);
        }
        
        textg.drawRect(graphX - 1, graphY - graphH - 1, graphW + 1, graphH + 1);
        
        if (addStats) {
            float py;
            textg.setFont(fontBold);
            py = displayStatistic(textg, graphX, graphW, "Višina:", Double.NaN, graphY - graphH);
            textg.setFont(fontPlain);
            py = displayStatistic(textg, graphX, graphW, "Začetek = ", startZ, py + 5);
            py = displayStatistic(textg, graphX, graphW, "Konec = ", endZ, py + 5);
            py = displayStatistic(textg, graphX, graphW, "Najvišja = ", maxZ, py + 10);
            py = displayStatistic(textg, graphX, graphW, "Najnižja = ", minZ, py + 5);
            py = displayStatistic(textg, graphX, graphW, "Razlika = ", maxZ - minZ, py + 10);
            py = displayStatistic(textg, graphX, graphW, "Vzpon = ", cumUp, py + 5);
            py = displayStatistic(textg, graphX, graphW, "Spust = ", cumDown, py + 5);
            textg.setFont(fontBold);
            py = displayStatistic(textg, graphX, graphW, "Razdalja:", Double.NaN, py + 10);
            textg.setFont(fontPlain);
            py = displayStatistic(textg, graphX, graphW, "Vzpon = ", cumUpDist, py + 5);
            py = displayStatistic(textg, graphX, graphW, "Spust = ", cumDownDist, py + 5);
            py = displayStatistic(textg, graphX, graphW, "Skupaj = ", cumUpDist + cumDownDist, py + 5);
        }
        
        return img;
    }
    public static BufferedImage calcProfile(LineString line, int w, int h) {
        return calcProfile(line, w, h,
                null, null, null, -1, true, true);
    }
    
   
    
    public static BufferedImage calcProfile(LineString line, LineProfileParams params) {
        
        if ((params.suggestedW <=0) || (params.suggestedH <=0)) {
            updateWidthAndHeight(line, params);
        }
        
        BufferedImage image = calcProfile(line, params.suggestedW, params.suggestedH,
                params.from, params.to, params.current, params.index, params.addStats, params.normalize);
        
        double paddingW = (2 * MARGIN + 2 * OUTER_BORDER_WIDTH + LEFTTEXT_WIDTH);
        if (params.addStats) {
            paddingW += RIGHTTEXT_WIDTH;
        }
        final double paddingH = (2 * MARGIN + 2 * OUTER_BORDER_WIDTH + TEXT_HEIGHT);
        
        if ((params.imgW > params.suggestedW + paddingW) || (params.imgH > params.suggestedH+paddingH)) {
            BufferedImage resizedImage = new BufferedImage(params.imgW, params.imgH, BufferedImage.TYPE_INT_ARGB);
            resizedImage.getGraphics().drawImage(image, 0, 0, image.getWidth(), image.getHeight(), Color.WHITE, null);
            return resizedImage;
        }
        return image;
    }
    
    private static float displayStatistic(Graphics2D textg, int graphX, int graphW, String txt,
                                          double val, float y) {
        String sTxt;
        
        if (!isNaN(val))
            sTxt = txt + Math.round(val) + " m";
        else
            sTxt = txt;
        Rectangle2D sBounds = textg.getFontMetrics().getStringBounds(sTxt, textg);
        float sx = graphX + graphW + MARGIN + 5;
        float sy = (float) (y + sBounds.getHeight());
        textg.drawString(sTxt, sx, sy);
        return (sy);
    }
    
    private static String lenTextFor(double len, double totalLen) {
        if (totalLen > 8000) {
            long hundreds = Math.round(len / 100);
            if (hundreds % 10 == 0) {
                return (hundreds / 10) + "km";
            } else {
                return (hundreds / 10) + "," + (hundreds % 10) + "km";
            }
        } else if (totalLen > 8) {
            long hundreds = Math.round(len * 10);
            if (hundreds % 10 == 0) {
                return (hundreds / 10) + "m";
            } else {
                return (hundreds / 10) + "," + (hundreds % 10) + "m";
            }
        } else if (totalLen > 0.08) {
            long hundreds = Math.round(len * 1000);
            if (hundreds % 10 == 0) {
                return (hundreds / 10) + "cm";
            } else {
                return (hundreds / 10) + "," + (hundreds % 10) + "cm";
            }
        } else {
            long hundreds = Math.round(len * 10000);
            if (hundreds % 10 == 0) {
                return (hundreds / 10) + "mm";
            } else {
                return (hundreds / 10) + "," + (hundreds % 10) + "mm";
            }
        }
    }
    
    private static String textFor(int i, double step) {
        if (step >= 1) {
            return Math.round(i * step) + "m";
        } else {
            String tmp = String.valueOf(Math.round(10 * i * step));
            if (tmp.length() > 1) {
                return tmp.substring(0, tmp.length() - 1) + ',' + tmp.charAt(tmp.length() - 1)
                       + "m";
            } else {
                return tmp + "m";
            }
        }
    }
    
    static double roundToDecimal(double approxStep) {
        double dec = Math.pow(10, Math.floor(Math.log10(approxStep)));
        return dec * Math.round(approxStep / dec);
    }
    
    public static LineProfileStats getProfileStats(LineString line, int steps) {
        
        double totalLen = line.getLength();
        
        double[] coords = line.coords;
        double[] zs = new double[steps];
        
        double minZ = 10000000;
        double maxZ = -10000000;
        
        double cumulativeUp = 0;
        double cumulativeDown = 0;
        double cumulativeUpDist = 0;
        double cumulativeDownDist = 0;
        
        double startZ = calcZ(coords[0], coords[1]);
        double endZ = calcZ(coords[coords.length - 2], coords[coords.length - 1]);
        
        double prevX = coords[0];
        double prevY = coords[1];
        double prevZ = calcZ(prevX, prevY);
        double currPosX = 0;
        double prevLen = 0;
        double testTotLen = 0;
        
        for (int p = 2; p < coords.length;) {
            double thisX = coords[p++];
            double thisY = coords[p++];
            
            double len = Math.hypot(thisX - prevX, thisY - prevY);
            
            double newPosX = currPosX + len;
            
            int iCurrX = (int) (currPosX * steps / totalLen);
            int iNewX = (int) (newPosX * steps / totalLen);
            if (iNewX >= steps)
                iNewX = steps - 1;
            
            int n = iNewX - iCurrX;
            double deltaX = thisX - prevX;
            double deltaY = thisY - prevY;
            
            for (int outOffset = 0; outOffset <= n; outOffset++) {
                double fac = n == 0 ? 0.5 : (double) outOffset / n;
                double zx = prevX + fac * deltaX;
                double zy = prevY + fac * deltaY;
                double curLen = currPosX + len * fac;
                
                double z = zs[iCurrX + outOffset] = calcZ(zx, zy);
                double deltaLen = curLen - prevLen;
                testTotLen += deltaLen;
                if (!Double.isNaN(z)) {
                    if (z > prevZ) {
                        // Calculate cumulative height (up and down);
                        cumulativeUp += z - prevZ;
                        // Calculate real total distance (not projected)
                        cumulativeUpDist += Math.sqrt(deltaLen * deltaLen + Math.pow(z - prevZ, 2));
                    } else {
                        cumulativeDown += prevZ - z;
                        cumulativeDownDist += Math.sqrt(deltaLen * deltaLen + Math.pow(z - prevZ, 2));
                    }
                    
                    // Setting min and max height
                    if (z < minZ)
                        minZ = z;
                    if (z > maxZ)
                        maxZ = z;
                    prevZ = z;
                    prevLen = curLen;
                }
            }
            prevX = thisX;
            prevY = thisY;
            currPosX = newPosX;
        }
        
        LineProfileStats stats = new LineProfileStats();
        stats.zacetek = startZ;
        stats.konec = endZ;
        stats.najvisja = maxZ;
        stats.najnizja = minZ;
        stats.razlika = maxZ - minZ;
        stats.vzpon = cumulativeUp;
        stats.spust = cumulativeDown;
        stats.vzponRazdalja = cumulativeUpDist;
        stats.spustRazdalja = cumulativeDownDist;
        stats.skupaj = cumulativeUpDist + cumulativeDownDist;
        return stats;
    }
    
    private static void updateWidthAndHeight(LineString line, LineProfileParams params) {
        
        final int MIN_ALTITUDE = 0;
        final int MAX_ALTITUDE = 3000;
        
        final int MAX_NUMBER_OF_PIXELS = 10000000; // 10 mega pixels
        
        final double MAX_WIDTH = 20000;
        final double MAX_HEIGHT = 2000;
        
        final double DEFAULT_HEIGHT = 600;
        
        double paddingW = (2 * MARGIN + 2 * OUTER_BORDER_WIDTH + LEFTTEXT_WIDTH);
        if (params.addStats) {
            paddingW += RIGHTTEXT_WIDTH;
        }
        final double paddingH = (2 * MARGIN + 2 * OUTER_BORDER_WIDTH + TEXT_HEIGHT);
        
        double horizontalDistance = line.getLength();
        
        // estimate minZ and maxZ
        int steps = (int) horizontalDistance/5;
        if (steps > 10000) {
            steps = 10000;
        }
        LineProfileStats stats = Heights.getProfileStats(line, steps);
        double minZ = stats.najnizja;
        double maxZ = stats.najvisja;
        if (maxZ > MAX_ALTITUDE) {
            maxZ = MAX_ALTITUDE;
        }
        if (minZ < MIN_ALTITUDE) {
            minZ = MIN_ALTITUDE;
        }
        
        double deltaZ = maxZ-minZ;
        double ratio = horizontalDistance/deltaZ;
        
        // default size
        double suggestedH = DEFAULT_HEIGHT;
        double suggestedW = DEFAULT_HEIGHT * ratio;
        
        if (suggestedW > MAX_WIDTH) {
            suggestedW = MAX_WIDTH;
            suggestedH = suggestedW / ratio;
        }

        double pixels = suggestedH * suggestedW;
        if (pixels > MAX_NUMBER_OF_PIXELS) {
            double _ratio = MAX_NUMBER_OF_PIXELS / pixels;
            suggestedW = suggestedW * _ratio;
            suggestedH = suggestedW / ratio;
        }
        
        boolean keepRatio = params.keepRatio;
        
        double imageW = params.imgW;
        double imageH = params.imgH;
        
        boolean adjustW = imageW < paddingW || imageW > MAX_WIDTH;
        boolean adjustH = imageH < paddingH || imageH > MAX_HEIGHT;
        
        if (adjustW) {
            imageW = suggestedW + paddingW;
        } else {
            if (imageW < suggestedW + paddingW) {
                suggestedW = imageW - paddingW;
                if (keepRatio) {
                    suggestedH = suggestedW / ratio;
                }
            }
        }
        
        if (adjustH) {
            if (keepRatio) {
                imageH = suggestedH + paddingH;
            } else {
                imageH = DEFAULT_HEIGHT;
            }
        } else {
            if (imageH < suggestedH + paddingH) {
                suggestedH = imageH - paddingH;
                if (keepRatio) {
                    suggestedW = suggestedH * ratio;
                }
            }
        }
        
        // suggestedW and suggestedH are not too big.
        // imageW and imageH are set
        // adjust suggestedW and suggestedH to imageW and imageH
        
        suggestedW = imageW - paddingW;
        if (keepRatio) {
            suggestedH = suggestedW / ratio;
        } else {
            suggestedH = imageH - paddingH;
            if (suggestedH > MAX_HEIGHT) {
                suggestedH = MAX_HEIGHT;
            }
        }
        
        suggestedW += paddingW;
        suggestedH += paddingH;
        
        params.imgW = (int) imageW;
        params.imgH = (int) imageH;
        
        params.suggestedW = (int) suggestedW;
        params.suggestedH = (int) suggestedH;
    }*/
}
