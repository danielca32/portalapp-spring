package com.danielcastro.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danielcastro.portal.domain.User;

public interface UserRepository extends JpaRepository<User, Long>{
	User findUserByUsername(String username);
	User findUserByEmail(String email);
	User deleteUserByUsername(String username);
}
