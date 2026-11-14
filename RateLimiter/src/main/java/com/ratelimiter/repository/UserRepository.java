package com.ratelimiter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ratelimiter.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
	
	    boolean existsByUserName(String userName);

	    boolean existsByEmail(String email);
	    
	    Optional<UserEntity> findByUserName(String username);

}
