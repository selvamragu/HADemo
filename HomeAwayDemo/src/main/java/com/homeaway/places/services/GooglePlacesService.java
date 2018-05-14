package com.homeaway.places.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeaway.places.entities.Request.GooglePlacesRequest;
import com.homeaway.places.entities.Request.LatLong;
import com.homeaway.places.entities.Response.GooglePlacesResponse;
import com.homeaway.places.entities.Response.Result;

import java.sql.*;

public class GooglePlacesService {

	@SuppressWarnings("rawtypes")
	public ResponseEntity StorePlacesInDB(GooglePlacesRequest gpRequest) {
		for (LatLong latLong : gpRequest.getlatLong()) {
			for (String place : gpRequest.getPlaces()) {

				String requestUri = String.format(Strings.goolePlacesApiUri, latLong.getLatitude(),
						latLong.getLongitude(), gpRequest.getRadius(), place, Strings.apiKey);
				boolean nextPage = false;
				boolean invalidStatus = false;
				boolean firstPage = true;

				do {
					try {
						GooglePlacesResponse gpRes = GetPlacesFromGoogleApi(requestUri);
						invalidStatus = gpRes.getStatus().trim().equals("INVALID_REQUEST");
						if (!invalidStatus) {
							String selectQuery = String.format(Strings.selectPlaceType, place);
							String insertQuery = String.format(Strings.insertPlaceType, place);
							int placeTypeId = InsertIfNotPresent(selectQuery, insertQuery);

							selectQuery = String.format(Strings.selectSearchTerm, latLong.getLatitude(),
									latLong.getLongitude(), gpRequest.getRadius());
							insertQuery = String.format(Strings.insertSearchTerm, latLong.getLatitude(),
									latLong.getLongitude(), gpRequest.getRadius());
							int searchTermId = InsertIfNotPresent(selectQuery, insertQuery);

							for (Result result : gpRes.getResults()) {
								selectQuery = String.format(Strings.selectPlace, searchTermId, result.getName(),
										placeTypeId);
								insertQuery = String.format(Strings.insertPlace, searchTermId, result.getName(),
										placeTypeId);
								InsertIfNotPresent(selectQuery, insertQuery);
							}
						} else if (invalidStatus && !firstPage)
							Thread.sleep(500); // it take few seconds for the
												// next page token to become
												// valid, hence waiting for few
												// seconds before next call to
												// prevent lot of hits
						nextPage = gpRes.getNext_page_token() != null && !gpRes.getNext_page_token().isEmpty();
						if (nextPage) {
							firstPage = false;
							requestUri = String.format(Strings.googlePlacesNextPageUri, gpRes.getNext_page_token(),
									Strings.apiKey);
						}
					} catch (Exception ex) {
						System.out.println("Exception : " + ex.getMessage().toString());
					}
				} while (nextPage || (invalidStatus && !firstPage));
			}
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("Places inserted into db");
	}

	private GooglePlacesResponse GetPlacesFromGoogleApi(String requestUri) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(requestUri);
			HttpResponse response;
			response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(result.toString(), GooglePlacesResponse.class);
		} catch (Exception ex) {
			System.out.println("Exception : " + ex.getMessage());
			return null;
		}
	}

	private int InsertIfNotPresent(String selectQuery, String insertQuery) {
		int idToReturn = 0;
		try {
			Connection con = DriverManager.getConnection(Strings.connectionString);
			PreparedStatement st = con.prepareStatement(selectQuery);
			ResultSet rs = st.executeQuery();
			if (rs.next())
				idToReturn = rs.getInt(1);
			else {
				Statement stmt = con.createStatement();
				int count = stmt.executeUpdate(insertQuery, Statement.RETURN_GENERATED_KEYS);
				if (count > 0) {
					rs = stmt.getGeneratedKeys();

					ResultSetMetaData rsmd = rs.getMetaData();
					int columnCount = rsmd.getColumnCount();
					if (rs.next()) {
						do {
							for (int i = 1; i <= columnCount; i++) {
								idToReturn = rs.getInt(i);
							}
						} while (rs.next());
					} else {
						System.out.println("NO KEYS WERE GENERATED.");
					}
					rs.close();
					stmt.close();
				}
			}
			con.close();
		} catch (Exception ex) {
			System.out.println("Exception :" + ex.getMessage().toString());
		}
		return idToReturn;
	}
}
