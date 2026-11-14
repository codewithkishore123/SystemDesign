package com.ratelimiter.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.ratelimiter.entity.UserEntity;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	
	 @Value("${jwt.expiration}")
	    private long expiration;

	    public JwtService(JwtEncoder jwtEncoder) {
	        this.jwtEncoder = jwtEncoder;
	    }
	    
	    public String  generateToken(UserEntity user) {
	    	
	    	
	    	   Instant now = Instant.now();
	    	   
	    	   JwtClaimsSet claims = JwtClaimsSet.builder()
	    			                 .issuer("rate-limiter")
	    			                 .issuedAt(now)
	    			                 .expiresAt(now.plus(expiration, ChronoUnit.MILLIS))
	    			                 .subject(user.getId().toString())
	    			                 .claim("username",user.getUserName())
	    			                 .claim("roles",List.of(user.getRole().name()))
	    			                 .build();
	    	   
	    	   
	    	   JwsHeader header = JwsHeader
	                   .with(MacAlgorithm.HS256)
	                   .build();
	    	   
	    	   return jwtEncoder.encode(
	                   JwtEncoderParameters.from(header, claims)
	           ).getTokenValue();
	    	
	    }
	
}
