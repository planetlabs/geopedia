package com.sinergise.java.raster.io;

import static com.sinergise.common.geometry.util.GeomUtil.toDeg;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.metadata.IIOMetadata;

import com.sinergise.common.geometry.crs.misc.MontenegroTransforms;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.java.raster.core.RasterIO;
import com.sun.media.imageio.plugins.tiff.GeoTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageMetadata;

public class GeoTIFFUtilJava {
	public static void main(String[] args) throws IOException {
		System.out.println(
				MontenegroTransforms.ETRS89_TO_GK6.point(new Point(toDeg(43, 15, 0),toDeg(18, 52, 30)), new Point())
				);
		System.out.println(
				MontenegroTransforms.ETRS89_TO_ME_UTM34N.point(new Point(toDeg(43, 15, 0.6),toDeg(18, 52, 11.6)), new Point())
				);
		double[] ret = readTFW(new File("S:\\unsorted\\srdja\\129-4-2.tif"));
		for (int i = 0; i < ret.length; i++) {
			System.out.println(ret[i]);
		}
	}

	public static double[] readTFW(File geoTiff) throws IOException {
		return readTFW(RasterIO.readImageMetadata(geoTiff));
	}

	public static double[] readTFW(URL geoTiff) throws IOException {
		return readTFW(RasterIO.readImageMetadata(geoTiff));
	}

	public static double[] readTFW(IIOMetadata meta) {
		TIFFImageMetadata tifMeta = (TIFFImageMetadata)meta;
		
		TIFFField tiffField = tifMeta.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TRANSFORMATION);
		if (tiffField != null) {
			return readTfwWithModelTransformation(tiffField);
		}
		
		tiffField = tifMeta.getTIFFField(GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE);
		if (tiffField != null) {
			TIFFField tiePointField = tifMeta.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT);
			return readTfwWithPixelScale(tiffField, tiePointField);
		}
		
		tiffField = tifMeta.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT);
		if (tiffField != null) {
			return readTfwWithTiePoints(tiffField);
		}

		throw new IllegalArgumentException("GeoTiff tags not found (MODEL_TRANSFORMATION or MODEL_PIXEL_SCALE)");
	}
	
	public static HasCoordinate[][] readTiePoints(File geoTiff) throws IOException {
		TIFFImageMetadata tifMeta = (TIFFImageMetadata)RasterIO.readImageMetadata(geoTiff);
		TIFFField tiffField = tifMeta.getTIFFField(GeoTIFFTagSet.TAG_MODEL_TIE_POINT);
		return readTiePoints(tiffField);
	}

	private static double[] readTfwWithTiePoints(TIFFField tiffField) {
		HasCoordinate[][] pts = readTiePoints(tiffField);
		return tfwFromTiePoints(pts[0], pts[1]);
	}

	public static double[] tfwFromTiePoints(HasCoordinate[] src, HasCoordinate[] tgt) {
		AffineTransform2D t11 = AffineTransform2D.fromFiducials(src, tgt);
		// First pixel is 1,1
		double[] ret = t11.paramsToArray();
		ret[4] = t11.x(0.5, 0.5);
		ret[5] = t11.y(0.5, 0.5);
		return ret;
	}

	private static HasCoordinate[][] readTiePoints(TIFFField tiffField) {
		double[] g = tiffField.getAsDoubles(); // {I,J,K,X,Y,Z, ...}
		int numPoints = g.length/6;
		Position2D[][] pts = new Position2D[2][numPoints];
		for (int i = 0; i < numPoints; i++) {
			pts[0][i] = new Position2D(g[6*i + 0], g[6*i + 1]);
			pts[1][i] = new Position2D(g[6*i + 3], g[6*i + 4]);
		}
		return pts;
	}
	
	private static double[] readTfwWithPixelScale(TIFFField tiffField, TIFFField tiePointField) {
		double[] g = tiffField.getAsDoubles(); 
		double[] ret = new double[6];
		ret[0] = g[0];
		ret[1] = 0;
		ret[2] = 0;
		ret[3] = -g[1];

		g = tiePointField.getAsDoubles();
		ret[4] = g[3] - (g[0] - 0.5)*ret[0];
		ret[5] = g[4] + (g[1] + 0.5)*ret[3];
		return ret;
	}

	private static double[] readTfwWithModelTransformation(TIFFField tiffField) {
		double[] g = tiffField.getAsDoubles();
		return new double[]{
				g[0],g[4],g[1],g[5],g[3],g[7]
		};
	}
}
