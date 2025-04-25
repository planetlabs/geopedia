package com.sinergise.common.util.geom;

public interface HasCoordinateMutable extends HasCoordinate {
	HasCoordinateMutable setLocation(HasCoordinate location);
	HasCoordinateMutable clone();
}