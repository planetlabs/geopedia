package com.sinergise.geopedia.client.core.map;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.core.entities.Feature;

class TableFeatures
{
	final int tableId;
	long tableLastDataChangedID = 0;
	int[] featureIds = new int[4];
	int nFeats = 0;
	
	boolean hasExtents;
	double minx, miny, maxx, maxy;
	
	public TableFeatures(int tableId)
	{
		this.tableId = tableId;
		minx=Double.POSITIVE_INFINITY;
		miny=Double.POSITIVE_INFINITY;
		maxx=Double.NEGATIVE_INFINITY;
		maxy=Double.NEGATIVE_INFINITY;
	}
	
	public void addFeature(Feature feature)
	{
		boolean haveIt = false;
		for (int a=0; a<nFeats; a++)
			if (featureIds[a] ==feature.id) {
				haveIt = true;
				break;
			}
		
		Envelope extents = feature.envelope;
		if (extents != null) {
			if (hasExtents) {
				minx = minx < extents.getMinX() ? minx : extents.getMinX();
				miny = miny < extents.getMinY() ? miny : extents.getMinY();
				maxx = maxx < extents.getMaxX() ? maxx : extents.getMaxX();
				maxy = maxy < extents.getMaxY() ? maxy : extents.getMaxY();
			} else {
				hasExtents = true;
				minx = extents.getMinX();
				miny = extents.getMinY();
				maxx = extents.getMaxX();
				maxy = extents.getMaxY();
			}
		}
		
		if (!haveIt) {
			if (nFeats >= featureIds.length) {
				int[] tmp = new int[featureIds.length * 2];
				for (int a=0; a<nFeats; a++)
					tmp[a] = featureIds[a];
				featureIds = tmp;
			}
			featureIds[nFeats++] = feature.id;
			if (feature.tableDataTs>tableLastDataChangedID)
				tableLastDataChangedID = feature.tableDataTs;
			}
	}
	
	
	

	public void getUrlPart(StringBuffer buf)
    {
//		if (tableGeomType>=0 && !GeomType.isSomething(tableGeomType)) {
//			return;
//		}
		buf.append('l');
		buf.append(tableId);
		buf.append('@');
		buf.append(tableLastDataChangedID);
		buf.append(':');
		for (int a=0; a<nFeats; a++) {
			if (a > 0)
				buf.append(',');
			buf.append(featureIds[a]);
		}
    }

	public void removeFeature(int featId)
    {
		for (int a=0; a<nFeats; a++) {
			if (featureIds[a] == featId) {
				featureIds[a] = featureIds[--nFeats];
				return;
			}
		}
    }

	public boolean isEmpty()
	{
		return nFeats == 0;
    }
}