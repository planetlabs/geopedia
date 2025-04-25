package com.sinergise.common.geometry.tiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import com.sinergise.common.util.geom.EnvelopeI;


public class OptimalRectangleCover {
    public static final List<EnvelopeI> calculate(boolean[][] mtrx) {
        OptimalRectangleCover orc = new OptimalRectangleCover(mtrx);
        orc.go();
        return orc.getFoundMBRs();
    }
    
    public static final int EMPTY = -1;
    public static final int FULL = 0;
    
    public static final int[][] convertString(String[] str) {
        int[][] ret = new int[str.length][str[0].length()];
        for (int i = 0; i < str.length; i++) {
            for (int j = 0; j < str[i].length(); j++) {
                char ch = str[i].charAt(j);
                ret[i][j] = (ch==' ' || ch == '.' || ch == '_' || ch == '\t') ? EMPTY : FULL;
            }
        }
        return ret;
    }
    
    public static final int[][] convertMatrix(boolean[][] bm) {
        int[][] im = new int[bm.length][bm[0].length];
        for (int i = 0; i < bm.length; i++) {
            for (int j = 0; j < bm[i].length; j++) {
                im[i][j] = bm[i][j] ? FULL : EMPTY;
            }
        }
        return im;
    }
    
    int h;
    int w;
    private int[][] mtrx;
    private ArrayList<EnvelopeI> foundMBRs;
    
    public OptimalRectangleCover(boolean[][] matrix) {
        this(convertMatrix(matrix));
    }
    
    public OptimalRectangleCover(int[][] matrix) {
        this.mtrx = matrix;
        this.h = mtrx.length;
        this.w = mtrx[0].length;
    }
    
    public void go() {
        foundMBRs = new ArrayList<EnvelopeI>();
        findClusters();
        shrinkOverlappingClusters();
    }

	private void shrinkOverlappingClusters() {
		for (ListIterator<EnvelopeI> it = foundMBRs.listIterator(); it.hasNext();) {
            it.set(shrinkMBR(it.next()));
        }
	}

	private void findClusters() {
		for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                if (mtrx[row][col] == FULL) { // unvisited full pixel
                    EnvelopeI curMBR = findClusterStartingAt(row, col);
                    foundMBRs.add(curMBR);
                    col = curMBR.maxX()+1;
                }
            }
        }
	}

	private EnvelopeI findClusterStartingAt(int row, int col) {
		EnvelopeI curMBR = goRight(row, col);
		curMBR = goDown(curMBR);
		return curMBR;
	}
    
    private EnvelopeI shrinkMBR(EnvelopeI mbr) {
    	EnvelopeI.Builder eb = new EnvelopeI.Builder(mbr);
        while (colRangeMin(eb.minX(), eb.minY(), eb.maxY(), 2) > 1) {
            for (int row = eb.minY(); row <= eb.maxY(); row++) {
                mtrx[row][eb.minX()]--;
            }
            eb.addToX(1, 0);
        }
        while (colRangeMin(eb.maxX(), eb.minY(), eb.maxY(), 2) > 1) {
            for (int row = eb.minY(); row <= eb.maxY(); row++) {
                mtrx[row][eb.maxX()]--;
            }
            eb.addToX(0, -1);
        }
        while (rowRangeMin(eb.minY(), eb.minX(), eb.maxX(), 2) > 1) {
            for (int col = eb.minX(); col <= eb.maxX(); col++) {
                mtrx[eb.minY()][col]--;
            }
            eb.addToY(1, 0);
        }
        while (rowRangeMin(eb.maxY(), eb.minX(), eb.maxX(), 2) > 1) {
            for (int col = eb.minX(); col <= eb.maxX(); col++) {
                mtrx[eb.maxY()][col]--;
            }
            eb.addToY(0, -1);
        }
        return eb.getEnvelope();
    }
    
    private int colRangeMin(int col, int minRow, int maxRow, int atLeast) {
        int min = Integer.MAX_VALUE;
        for (int row = minRow; row <= maxRow; row++) {
            int val = mtrx[row][col];
            if (val < atLeast)
                return val;
            if (val < min)
                min = val;
        }
        return min;
    }
    
    private int rowRangeMin(int row, int minCol, int maxCol, int atLeast) {
        int min = Integer.MAX_VALUE;
        for (int col = minCol; col <= maxCol; col++) {
            int val = mtrx[row][col];
            if (val < atLeast)
                return val;
            if (val < min)
                min = val;
        }
        return min;
    }
    
    public ArrayList<EnvelopeI> getFoundMBRs() {
        return foundMBRs;
    }
    
    protected EnvelopeI goRight(int row, int inCol) {
    	int maxCol = inCol;
        for (int col = inCol; col < w; col++) {
            if (mtrx[row][col] != EMPTY) {
                maxCol = col;
                int val = mtrx[row][col];
                if (val < 0) {
                    val = 0;
                }
                val++;
                mtrx[row][col] = val; // no need to reserve, we're the last to visit
            } else {
                break;
            }
        }
        return new EnvelopeI(inCol, row, maxCol, row);
    }
    
    protected EnvelopeI goDown(EnvelopeI curMBR) {
    	int maxRow = curMBR.maxY();
        for (int row = curMBR.minY() + 1; row < h; row++) {
            // Check if we can extend down
            if (rowRangeMin(row, curMBR.minX(), curMBR.maxX(), 0) < 0) break;
            maxRow = row;
            for (int col = curMBR.minX(); col <= curMBR.maxX(); col++) {
                if (mtrx[row][col] < 1) {
                    mtrx[row][col] = 1;
                } else {
                    mtrx[row][col]++;
                }
            }
        }
        return new EnvelopeI(curMBR.minX(), curMBR.minY(), curMBR.maxX(), maxRow);
    }
    
    public static void main(String[] args) {
        String[][] datasets =
                              new String[][] {
                                              // 2
                                              new String[] { ".O.", "OOO" },
                                              // 2
                                              new String[] { "MM.", ".MM" },
                                              // 2
                                              new String[] { "MM.", "MMM", ".MM" },
                                              // 2
                                              new String[] { ".MM", "MMM", "MM." },
                                              // 8
                                              new String[] { "...M..", "..MMM.", "MMM.MM",
                                                            ".MMMM.", "MMMM..", "MM.M.." },
                                              // 4
                                              new String[] { "MMMM...", "MMMMM..", "MMMMMM.",
                                                            "MMMMMMM", ".MMMMMM", "..MMMMM",
                                                            "...MMMM" },
                                              new String[] { ".M.M.M.M.", "MMMMMMMMM", ".M.MMM.M.",
                                                            "MMMMMMMMM", ".MMM.MMM.", "MMMMMMMMM",
                                                            ".M.MMM.M.", "MMMMMMMMM", ".M.M.M.M." } };
        for (int i = 0; i < datasets.length; i++) {
            OptimalRectangleCover orc = new OptimalRectangleCover(convertString(datasets[i]));
            orc.go();
            System.out.println(Arrays.toString(orc.getFoundMBRs().toArray()));
            System.out.println(orc.getFoundMBRs().size());
            System.out.println(mtrxToString(orc.mtrx));
        }
    }
    
    public static String mtrxToString(int[][] mtrx) {
        StringBuilder buf = new StringBuilder();
        buf.append("-------------");
        buf.append("\n");
        for (int i = 0; i < mtrx.length; i++) {
            for (int j = 0; j < mtrx[i].length; j++) {
                int val = mtrx[i][j];
                if (val == EMPTY) {
                    buf.append(' ');
                } else if (val == FULL) {
                    buf.append('M');
                } else {
                    buf.append(val);
                }
            }
            buf.append("\n");
        }
        buf.append("-------------");
        return buf.toString();
    }
}
