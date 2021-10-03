package com.danielcastro.portal.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.web.multipart.MultipartFile;

import com.danielcastro.portal.domain.User;
import com.danielcastro.portal.exception.EmailExistsException;
import com.danielcastro.portal.exception.EmailNotFoundException;
import com.danielcastro.portal.exception.NotAnImageFileException;
import com.danielcastro.portal.exception.UserNotFoundException;
import com.danielcastro.portal.exception.UsernameExistsException;

public interface UserService {

	User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException;
	List<User> getUsers();
	User findUserByUsername(String username);
	User findUserByEmail(String email);
	User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
	void deleteUser(String username);
	void resetPassword(String email) throws EmailNotFoundException, AddressException, MessagingException;
	User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException;
}
