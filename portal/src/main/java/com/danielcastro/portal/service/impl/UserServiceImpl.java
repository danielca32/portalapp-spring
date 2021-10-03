package com.danielcastro.portal.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.danielcastro.portal.constant.FileConstant;
import com.danielcastro.portal.constant.UserImplConstant;
import com.danielcastro.portal.domain.User;
import com.danielcastro.portal.domain.UserPrincipal;
import com.danielcastro.portal.enumeration.Role;
import com.danielcastro.portal.exception.EmailExistsException;
import com.danielcastro.portal.exception.EmailNotFoundException;
import com.danielcastro.portal.exception.NotAnImageFileException;
import com.danielcastro.portal.exception.UserNotFoundException;
import com.danielcastro.portal.exception.UsernameExistsException;
import com.danielcastro.portal.repository.UserRepository;
import com.danielcastro.portal.service.EmailService;
import com.danielcastro.portal.service.LoginAttemptService;
import com.danielcastro.portal.service.UserService;
import static com.danielcastro.portal.constant.UserImplConstant.*;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

	private org.slf4j.Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	LoginAttemptService loginAttemptService;

	@Autowired
	EmailService emailService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findUserByUsername(username);
		if (user == null) {
			LOGGER.error(NO_USER_FOUND_BY_USERNAME + username);
			throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
		} else {
			validateLoginAttempt(user);
			user.setLastLoginDate(user.getLastLoginDate());
			user.setLastLoginDate(new Date());
			userRepository.save(user);
			UserPrincipal userPrincipal = new UserPrincipal(user);
			LOGGER.info(FOUND_USER_BY_USERNAME + username);
			return userPrincipal;
		}
	}

	private void validateLoginAttempt(User user) {
		if (user.isNotLocked()) {
			if (loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
				user.setNotLocked(false);
			} else {
				user.setNotLocked(true);
			}

		} else {
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}
	}

	@Override
	public User register(String firstName, String lastName, String username, String email)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {
		validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
		User user = new User();
		user.setUserId(generateUserId());
		String password = generatePassword();
		String encodedPassword = encodePassword(password);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		user.setJoinDate(new Date());
		user.setPassword(encodedPassword);
		user.setActive(true);
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		userRepository.save(user);
		LOGGER.info("New user password: " + password); // TESTING FEATURE, TODO: DISABLE
		//emailService.sendNewPasswordEmail(firstName, password, email); DISABLED TO AVOID SENDING MORE EMAILS
		return user; 
	}

	private String getTemporaryProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath()
				.path(FileConstant.DEFAULT_USER_IMAGE_PATH + username).toUriString();
	}

	private String encodePassword(String password) {
		return bCryptPasswordEncoder.encode(password);
	}

	private String generatePassword() {
		return RandomStringUtils.randomAlphanumeric(10);
	}

	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);
	}

	private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException {
		User userByNewUsername = findUserByUsername(newUsername);
		User userByNewEmail = findUserByEmail(newEmail);

		if (StringUtils.isNotBlank(currentUsername)) {
			User currentUser = findUserByUsername(currentUsername);
			if (currentUser == null) {
				throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
			}
			User userByUsername = findUserByUsername(newUsername);
			if (userByUsername != null && !currentUser.getId().equals(userByUsername.getId())) {
				throw new UsernameExistsException(USERNAME_ALREADY_EXISTS);
			}
			if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
				throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
			}
			return currentUser;
		} else {
			if (userByNewUsername != null) {
				throw new UsernameExistsException(USERNAME_ALREADY_EXISTS);
			}
			if (userByNewEmail != null) {
				throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
			}
			return null;
		}
	}

	@Override
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@Override
	public User findUserByUsername(String username) {
		return userRepository.findUserByUsername(username);
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}

	@Override
	public User addNewUser(String firstName, String lastName, String username, String email, String role,
			boolean isNonLocked, boolean isActive, MultipartFile profileImage)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
		User user = new User();
		String password = generatePassword();
		String encodedPassword = encodePassword(password);
		user.setUserId(generateUserId());
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setJoinDate(new Date());
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(encodedPassword);
		user.setActive(isActive);
		user.setNotLocked(isNonLocked);
		user.setRole(getRoleEnumName(role).name());
		user.setAuthorities(getRoleEnumName(role).getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		userRepository.save(user);
		LOGGER.info("New user password: " + password); // TESTING FEATURE, TODO: DISABLE
		//emailService.sendNewPasswordEmail(firstName, password, email); DISABLED TO AVOID SENDING MORE EMAILS
		saveProfileImage(user, profileImage);
		return user;
	}

	private Role getRoleEnumName(String role) {
		return Role.valueOf(role.toUpperCase());
	}

	@Override
	public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
			String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
		currentUser.setFirstName(newFirstName);
		currentUser.setLastName(newLastName);
		currentUser.setUsername(newUsername);
		currentUser.setEmail(newEmail);
		currentUser.setActive(isActive);
		currentUser.setNotLocked(isNonLocked);
		currentUser.setRole(getRoleEnumName(role).name());
		currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
		userRepository.save(currentUser);
		saveProfileImage(currentUser, profileImage);
		return currentUser;
	}

	@Override
	public void deleteUser(String username) {
		userRepository.deleteUserByUsername(username);
	}

	@Override
	public void resetPassword(String email) throws EmailNotFoundException, AddressException, MessagingException {
		User user = userRepository.findUserByEmail(email);
		if (user == null) {
			throw new EmailNotFoundException(UserImplConstant.NO_USER_FOUND_BY_EMAIL);
		}
		String password = generatePassword();
		user.setPassword(encodePassword(password));
		userRepository.save(user);
		emailService.sendNewPasswordEmail(user.getFirstName(), password, user.getEmail());
	}

	@Override
	public User updateProfileImage(String username, MultipartFile profileImage)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {
		User user = validateNewUsernameAndEmail(username, null, null);
		saveProfileImage(user, profileImage);
		return user;
	}

	private void saveProfileImage(User user, MultipartFile profileImage) throws IOException, NotAnImageFileException {
		if (profileImage != null) {
			if(!Arrays.asList(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
				throw new NotAnImageFileException(profileImage.getOriginalFilename() + "is not an image file. Please upload an image.");
			}
			Path userFolder = Paths.get(FileConstant.USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
			if (!Files.exists(userFolder)) {
				Files.createDirectories(userFolder);
				LOGGER.info(FileConstant.DIRECTORY_CREATED);
			}

			Files.deleteIfExists(
					Paths.get(userFolder + user.getUsername() + FileConstant.DOT + FileConstant.JPG_EXTENSION));
			Files.copy(profileImage.getInputStream(),
					userFolder.resolve(user.getUsername() + FileConstant.DOT + FileConstant.JPG_EXTENSION),
					StandardCopyOption.REPLACE_EXISTING);
			user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
			userRepository.save(user);
			LOGGER.info(FileConstant.FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
		}
	}

	private String setProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.USER_IMAGE_PATH + username + FileConstant.FORWARD_SLASH + username + FileConstant.DOT + FileConstant.JPG_EXTENSION).toUriString();
	}

}
