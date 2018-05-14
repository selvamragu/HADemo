package com.homeaway.places.services;

public class Strings {
	final static String apiKey = "AIzaSyA9B6q32pA-HEqVy880RA5PTRpZONU09nM";
	// final String apiKey = "AIzaSyCdb0_G3whD-KNfgduFhow13FMMxsjswWY";
	final static String goolePlacesApiUri = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%1s,%2s&radius=%3s&type=%4s&key=%5s";
	final static String googlePlacesNextPageUri = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=%1s&key=%2s";

	// queries

	final static String connectionString = "jdbc:sqlserver://localhost;databaseName=HASample;integratedSecurity=true;";

	final static String insertPlaceType = "Insert into dbo.PlaceTypes (placetype) values ('%1s')";
	final static String insertSearchTerm = "Insert into dbo.SearchTerm (latitude,longitude,radius) values (%1s,%2s,%3s)";
	final static String insertPlace = "Insert into dbo.Places (searchid,name,placetypeid) values (%1s,'%2s',%3s)";

	final static String selectPlaceType = "Select placetypeid from dbo.PlaceTypes where placetype = '%1s'";
	final static String selectSearchTerm = "Select searchid from dbo.searchterm where latitude = %1s and "
			+ "longitude = %2s and radius = %3s";
	final static String selectPlace = "Select searchid from dbo.places where searchid = %1s and name = '%2s' and placetypeid = %3s";

	final static String selectPlaceByPlaceId = "Select distinct(p.name) from dbo.places p join "
			+ "dbo.placetypes pt on pt.placetypeid = p.placetypeid " + "where pt.placetype = '%1s'";

	final static String selectPlaceByPlaceIdAndGeoLocation = "select distinct(p.name) from dbo.places p join "
			+ "dbo.searchterm sr on sr.searchid = p.searchid join "
			+ "dbo.placetypes pt on pt.placetypeid = p.placetypeid " + "where sr.latitude = %1s and sr.longitude = %2s "
			+ "and sr.radius = %3s and pt.placetype = '%4s'";
}
