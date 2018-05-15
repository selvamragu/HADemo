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
import com.homeaway.places.services.Utils.Strings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GooglePlacesService {

	@SuppressWarnings("rawtypes")
	public ResponseEntity StorePlacesInDB(GooglePlacesRequest gpRequest) {
		List<String> responseToReturn = new ArrayList<String>();
		for (LatLong latLong : gpRequest.getlatLong()) {

			if (latLong.getLatitude() < -90 || latLong.getLatitude() > 90 || latLong.getLongitude() < -180
					|| latLong.getLongitude() > 180) {
				responseToReturn
						.add(String.format(
								"Latitude : %1s, Lonitude : %2s is invalid. Latitude range : -90 to 90 "
										+ "& Longitude range : -180 to 180",
								latLong.getLatitude(), latLong.getLongitude()));
				continue;
			}

			for (String place : gpRequest.getPlaces()) {

				if (place == null || place.isEmpty()) {
					responseToReturn.add("place type cannot be null or empty");
					continue;
				}

				String requestUri = String.format(Strings.goolePlacesApiUri, latLong.getLatitude(),
						latLong.getLongitude(), gpRequest.getRadius(), place, Strings.apiKey);
				boolean nextPage = false;
				boolean invalidStatus = false;
				boolean firstPage = true;
				int pageNum = 0;

				do {
					try {
						GooglePlacesResponse gpRes = GetPlacesFromGoogleApi(requestUri);

						if (gpRes == null) {
							responseToReturn.add(String.format(Strings.responseString, "null response",
									latLong.getLatitude(), latLong.getLongitude(), place));
							continue;
						}

						String googleResponseStatus = gpRes.getStatus().trim();

						if (googleResponseStatus.equals("ZERO_RESULTS")) {
							responseToReturn.add(String.format(Strings.responseString, "zero results",
									latLong.getLatitude(), latLong.getLongitude(), place));
							continue;
						}

						invalidStatus = googleResponseStatus.equals("INVALID_REQUEST");

						if (invalidStatus && firstPage) {
							responseToReturn.add(String.format(Strings.responseString, "invalid request",
									latLong.getLatitude(), latLong.getLongitude(), place));
							continue;
						}
						
						if (!invalidStatus) {
							int placeTypeId = InsertPlaceType(place);
							int searchTermId = InsertSearchTerm(latLong, gpRequest.getRadius());
							for (Result result : gpRes.getResults()) {
								InsertPlace(searchTermId, placeTypeId, result.getName().contains("'") 
										? result.getName().replaceAll("'", "''") : result.getName());
							}
							responseToReturn.add(String.format(Strings.responseStringWithPageNum, "records", latLong.getLatitude(),
									latLong.getLongitude(), place,++pageNum));
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
						System.out.println("Exception in StorePlacesInDB: " + ex.getMessage().toString());
					}
				} while (nextPage || (invalidStatus && !firstPage));
			}
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(responseToReturn);
	}

	private int InsertPlaceType(String place) {
		String selectQuery = String.format(Strings.selectPlaceType, place);
		String insertQuery = String.format(Strings.insertPlaceType, place);
		return InsertIfNotPresent(selectQuery, insertQuery);
	}

	private int InsertSearchTerm(LatLong latLong, int radius) {
		String selectQuery = String.format(Strings.selectSearchTerm, latLong.getLatitude(), latLong.getLongitude(),
				radius);
		String insertQuery = String.format(Strings.insertSearchTerm, latLong.getLatitude(), latLong.getLongitude(),
				radius);
		return InsertIfNotPresent(selectQuery, insertQuery);
	}

	private int InsertPlace(int searchTermId, int placeTypeId, String place) {
		String selectQuery = String.format(Strings.selectPlace, searchTermId, place, placeTypeId);
		String insertQuery = String.format(Strings.insertPlace, searchTermId, place, placeTypeId);
		return InsertIfNotPresent(selectQuery, insertQuery);
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
			System.out.println("Exception in GetPlacesFromGoogleApi: " + ex.getMessage());
			return null;
		}
	}

	private int InsertIfNotPresent(String selectQuery, String insertQuery) {
		int idToReturn = 0;
		Connection con = null;
		try {
			con = DriverManager.getConnection(Strings.connectionString);
			PreparedStatement st = con.prepareStatement(selectQuery);
			ResultSet rs = st.executeQuery();
			if (rs != null && rs.next())
				idToReturn = rs.getInt(1);
			else {
				Statement stmt = con.createStatement();
				int count = stmt.executeUpdate(insertQuery, Statement.RETURN_GENERATED_KEYS);
				if (count > 0) {
					rs = stmt.getGeneratedKeys();
					ResultSetMetaData rsmd = rs.getMetaData();
					int columnCount = rsmd.getColumnCount();
					if (rs != null && rs.next()) {
						do {
							for (int i = 1; i <= columnCount; i++) {
								idToReturn = rs.getInt(i);
							}
						} while (rs.next());
					} 
					rs.close();
					stmt.close();
				}
			}
		} catch (Exception ex) {
			System.out.println("Exception  in InsertIfNotPresent :" + ex.getMessage().toString());
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					System.out.println("Exception" + ex.getMessage().toString());
					idToReturn = -1;
				}
			}
		}
		return idToReturn;
	}
}
