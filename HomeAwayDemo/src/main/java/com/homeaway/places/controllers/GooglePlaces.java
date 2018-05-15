package com.homeaway.places.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homeaway.places.entities.Request.GooglePlacesRequest;
import com.homeaway.places.services.GooglePlacesService;

@RestController
@RequestMapping("/googleplaces")
public class GooglePlaces {

	@SuppressWarnings("rawtypes")
	@PostMapping
	public ResponseEntity GetPlacesFromGoogleApi(@RequestBody GooglePlacesRequest gpRequest) {
		if(gpRequest.getRadius() <= 0)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Radius should be > zero");
		return new GooglePlacesService().StorePlacesInDB(gpRequest);
	}
}
