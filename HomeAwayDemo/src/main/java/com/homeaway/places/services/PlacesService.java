package com.homeaway.places.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PlacesService {

	@SuppressWarnings("rawtypes")
	public ResponseEntity GetPlacesFromDB(String[] placeTypes, double latitude, double longitude, int radius) {
		Set<String> placesToReturn = new HashSet<String>();
		try {
			Connection conn = DriverManager.getConnection(Strings.connectionString);
				Statement stmt = conn.createStatement();
				ResultSet rs;
				for (String placeType : placeTypes) {
					String query;
					if (radius == 0 && latitude == 0 && longitude == 0) {
						query = Strings.selectPlaceByPlaceId;
						rs = stmt.executeQuery(String.format(query, placeType));
					} else {
						query = Strings.selectPlaceByPlaceIdAndGeoLocation;
						rs = stmt.executeQuery(String.format(query, latitude, longitude, radius, placeType));
					}

					while (rs.next()) {
						placesToReturn.add(rs.getString("name"));
					}
				}
				return ResponseEntity.status(HttpStatus.OK).body(placesToReturn);
		} catch (Exception ex) {
			System.out.println("Exception :" + ex.getMessage().toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error ocurred, cannot fetch data");
		}
	}
}
