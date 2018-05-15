package com.homeaway.places.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.homeaway.places.services.Utils.Strings;

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
						if (latitude < -90 || latitude > 90 || longitude < -180
								|| longitude > 180 || radius <= 0) {
							  return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									  .body("Latitude range : -90 to +90; Longitude range : -180 to 180; radius should be > 0");
						}
						query = Strings.selectPlaceByPlaceIdAndGeoLocation;
						rs = stmt.executeQuery(String.format(query, latitude, longitude, radius, placeType));
					}

					while (rs.next()) {
						placesToReturn.add(rs.getString("name"));
					}
				}
				if (placesToReturn.size() > 0) 
				    return ResponseEntity.status(HttpStatus.OK).body(placesToReturn);
				else
					return ResponseEntity.status(HttpStatus.OK).body("No places found for the given request");
		} catch (Exception ex) {
			System.out.println("Exception :" + ex.getMessage().toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error ocurred, cannot fetch data");
		}
	}
}
