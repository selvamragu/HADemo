package test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.homeaway.places.controllers.GooglePlaces;
import com.homeaway.places.entities.Request.GooglePlacesRequest;

public class paramcheck {
	
	@Test
	public void CheckRadius()
	{
		GooglePlaces gplaces = new GooglePlaces();
		GooglePlacesRequest gpRequest = new GooglePlacesRequest();
		gpRequest.setRadius(-100);
		ResponseEntity outputResponse = gplaces.GetPlacesFromGoogleApi(gpRequest);
		assertEquals(outputResponse.getStatusCode(),HttpStatus.BAD_REQUEST);
	} 
}