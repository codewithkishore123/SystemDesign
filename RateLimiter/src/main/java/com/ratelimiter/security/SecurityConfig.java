package com.ratelimiter.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	
	
	 @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	 
	 @Bean
	    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {

	        JwtGrantedAuthoritiesConverter authoritiesConverter =
	                new JwtGrantedAuthoritiesConverter();

	        authoritiesConverter.setAuthoritiesClaimName("roles");
	        authoritiesConverter.setAuthorityPrefix("ROLE_");

	        JwtAuthenticationConverter authenticationConverter =
	                new JwtAuthenticationConverter();

	        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

	        return authenticationConverter;
	    }

	    @Bean
	    public SecurityFilterChain securityFilterChain(
	            HttpSecurity http,
	            JwtAuthenticationConverter jwtAuthenticationConverter)
	            throws Exception {

	        http
	            .csrf(csrf -> csrf.disable())

	            .sessionManagement(session -> session
	                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

	            .authorizeHttpRequests(auth -> auth
	                .requestMatchers(HttpMethod.POST,
	                        "/auth/register", "/auth/login","/redis/set").permitAll()
	                .requestMatchers(HttpMethod.GET, "/redis/get").permitAll()
	                .requestMatchers(HttpMethod.DELETE, "/redis/delete").permitAll()
	                .requestMatchers("/admin/**").hasRole("ADMIN")
	                .requestMatchers("/api/**").authenticated()
	                .anyRequest().authenticated())

	            .oauth2ResourceServer(oauth2 -> oauth2
	                .jwt(jwt -> jwt
	                    .jwtAuthenticationConverter(jwtAuthenticationConverter)));

	        return http.build();
	    }

}
