package com.sinergise.geopedia.core.crs;

import static com.sinergise.common.geometry.crs.misc.NgaTransforms.DC_WGS84_TO_MINNA_TRANSL;
import static com.sinergise.common.geometry.crs.misc.NgaTransforms.WGS84_TO_WUTM31N;
import static com.sinergise.common.geometry.crs.misc.NgaTransforms.WGS84_UTM31N;
import static com.sinergise.common.geometry.crs.misc.NgaTransforms.WUTM31N_TO_WGS84;
import static com.sinergise.common.geometry.crs.transform.Transforms.compose;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.TransverseMercator;
import com.sinergise.common.geometry.crs.TransverseMercator.GeographicToTM;
import com.sinergise.common.geometry.crs.TransverseMercator.UTM;
import com.sinergise.common.geometry.crs.misc.NgaTransforms;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.Identity;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transform.InvertibleTransform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.tiles.WithBounds;

public class CRSSettingsNGA extends CRSSettings{

	GeographicToTM MINNA_TO_MUTM32N = new GeographicToTM(NgaTransforms.MINNA_UTM32N);
	Transform<TransverseMercator, TransverseMercator> WUTM31N_TO_MUTM32N = compose(NgaTransforms.WUTM31N_TO_WGS84, compose(DC_WGS84_TO_MINNA_TRANSL, MINNA_TO_MUTM32N));
	Transform<TransverseMercator, TransverseMercator> MUTM32N_TO_WUTM31N = ((InvertibleTransform<TransverseMercator, TransverseMercator>)WUTM31N_TO_MUTM32N).inverse();
	
	
	public CRSSettingsNGA () {
		addCRS(NgaTransforms.WGS84_UTM31N);
		addCRS(CRS.WGS84);
		addCRS(NgaTransforms.MINNA_UTM32N);
		
		addTransform(new Identity<UTM>(WGS84_UTM31N));
		
		addTransform(WUTM31N_TO_WGS84);
		addTransform(WGS84_TO_WUTM31N);
		
		addTransform(WUTM31N_TO_MUTM32N);
		addTransform(MUTM32N_TO_WUTM31N);
	}
	

	@Override
	public WithBounds getMainCRS() {
		return (WithBounds)NgaTransforms.cs;
	}
	
	public static void main(String[] args) {
		CRSSettingsNGA set = new CRSSettingsNGA();
		Point wgs= new Point(7.508012999999989,6.009689999999957);
		Point out = new Point();
		set.getTransform(CRS.WGS84, NgaTransforms.WGS84_UTM31N).point(wgs,out);
		System.out.println(out);
	}
}
