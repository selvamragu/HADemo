package com.homeaway.places.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homeaway.places.services.PlacesService;

@RestController
@RequestMapping("/places")
public class Places {

	@SuppressWarnings("rawtypes")
	@GetMapping
	public ResponseEntity GetPlaces(@RequestParam(value = "placetypes", required = true) String[] placeTypes,
			@RequestParam(value = "latitude", required = false, defaultValue = "0") double latitude,
			@RequestParam(value = "longitude", required = false, defaultValue = "0") double longitude,
			@RequestParam(value = "radius", required = false, defaultValue = "0") int radius) {

		return new PlacesService().GetPlacesFromDB(placeTypes, latitude, longitude, radius);
	}

}
