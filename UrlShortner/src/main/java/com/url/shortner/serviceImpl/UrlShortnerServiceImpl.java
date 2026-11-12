package com.url.shortner.serviceImpl;

import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.url.shortner.dto.ResponseDto;
import com.url.shortner.model.Url;
import com.url.shortner.repository.UrlRespository;
import com.url.shortner.service.UrlShortnerService;

@Service
public class UrlShortnerServiceImpl implements UrlShortnerService{
	
	@Autowired
	UrlRespository urlRepo;
	@Value("${basePath}")
	String basePath;

	@Override
	public ResponseDto generateShortUrl(String longUrl) {
		
		String shortkey = generateRandomString();
		String shortUrl = basePath+shortkey;
		
		Url url = new Url();
		url.setLongUrl(longUrl);
		url.setShortUrl(shortUrl);
		url.setShortUrlKey(shortkey);
		
		urlRepo.save(url);
		
		ResponseDto response =  new ResponseDto();
		response.setShortCode(shortkey);
		response.setShortUrl(shortUrl);
		response.setUrl(longUrl);
		
		return response;
	}

	@Override
	public ResponseDto getshortUrlDeatails(String shortUrl) {
		System.out.println("ShortUrl-Service ---> "+shortUrl);
		Url url = urlRepo.findByShortUrl(shortUrl);
		System.out.println("JPA OUTPUT  ---> "+url);
		ResponseDto response = new ResponseDto();
		response.setShortCode(url.getShortUrlKey());
		response.setShortUrl(url.getShortUrl());
		response.setUrl(url.getLongUrl());
		return response;
	}

	@Override
	public String deleteShortUrl(String shortUrl) {
		Url url = urlRepo.findByShortUrl(shortUrl);
		urlRepo.delete(url);
		return "Deleted SuccessFully";
	}

	@Override
	public ResponseDto updateShortUrl(String shortUrl) {
		System.out.println("Searching short URL: [" + shortUrl + "]");

		Url url = urlRepo.findByShortUrl(shortUrl);

		System.out.println("JPA result: " + url);
		String randomString = generateRandomString();
		url.setShortUrlKey(randomString);
		url.setShortUrl(basePath+randomString);
		Url saveResponse = urlRepo.save(url);
		
		ResponseDto response = new ResponseDto();
		response.setShortCode(saveResponse.getShortUrlKey());
		response.setShortUrl(saveResponse.getShortUrl());
		response.setUrl(saveResponse.getLongUrl());
		
		return response;
	}
	
	private String generateRandomString() {

	    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	    Random random = new Random();

	    return random.ints(6, 0, characters.length())
	            .mapToObj(characters::charAt)
	            .map(String::valueOf)
	            .collect(Collectors.joining());
	}
	
	

}
