package com.url.shortner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.url.shortner.model.Url;

@Repository
public interface UrlRespository extends JpaRepository<Url, Integer>{
	
	Url findByShortUrl(String shortUrl);

}
