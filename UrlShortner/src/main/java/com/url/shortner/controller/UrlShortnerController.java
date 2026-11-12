package com.url.shortner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.shortner.dto.ResponseDto;
import com.url.shortner.service.UrlShortnerService;

@RestController
@RequestMapping
public class UrlShortnerController {
	
   @Autowired
   UrlShortnerService urlShortnerService;	
	
	@PostMapping("/generateShortUrl")
	public ResponseDto generateShortUrl(@RequestBody String longUrl) {
		
	      return urlShortnerService.generateShortUrl(longUrl);
	}
	
	
	@GetMapping("/getUrlDetails")
	public ResponseDto getUrlDetails(@RequestBody String shortUrl) {
		System.out.println("ShortUrl-Controller ---> "+shortUrl);
		return urlShortnerService.getshortUrlDeatails(shortUrl);
	}
	
	@PutMapping("/updateUrlDetails")
	public ResponseDto updateUrlDetails(@RequestBody String shortUrl) {
		
		return urlShortnerService.updateShortUrl(shortUrl);
		
	}
	
	@DeleteMapping("/deleteUrl")
	public String deleteUrl(@RequestBody String shortUrl) {
		
		return urlShortnerService.deleteShortUrl(shortUrl);
		
	}

}
