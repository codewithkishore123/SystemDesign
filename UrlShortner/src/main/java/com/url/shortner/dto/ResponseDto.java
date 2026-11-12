package com.url.shortner.dto;


public class ResponseDto {
	
	private String url;
	private String shortCode;
	private String shortUrl;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getShortCode() {
		return shortCode;
	}
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}
	public String getShortUrl() {
		return shortUrl;
	}
	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}
	@Override
	public String toString() {
		return "ResponseDto [url=" + url + ", shortCode=" + shortCode + ", shortUrl=" + shortUrl + "]";
	}
	
	

}
