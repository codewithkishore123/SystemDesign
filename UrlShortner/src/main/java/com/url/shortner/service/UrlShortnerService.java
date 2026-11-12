package com.url.shortner.service;

import org.springframework.stereotype.Service;

import com.url.shortner.dto.ResponseDto;

@Service
public interface UrlShortnerService {
	
	
	public ResponseDto generateShortUrl(String longUrl);
	public ResponseDto getshortUrlDeatails(String shortUrl);
	public String deleteShortUrl(String shortUrl);
	public ResponseDto updateShortUrl(String shortUrl);

}
