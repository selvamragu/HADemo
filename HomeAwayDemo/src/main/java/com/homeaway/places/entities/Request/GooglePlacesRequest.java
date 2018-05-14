package com.homeaway.places.entities.Request;

import java.util.List;

public class GooglePlacesRequest {
	private int radius;
	private List<LatLong> latLong;
	private List<String> places;
	private String dbName;

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public List<LatLong> getlatLong() {
		return latLong;
	}

	public void setlatLong(List<LatLong> latLong) {
		this.latLong = latLong;
	}

	public List<String> getPlaces() {
		return places;
	}

	public void setPlaces(List<String> places) {
		this.places = places;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

}
