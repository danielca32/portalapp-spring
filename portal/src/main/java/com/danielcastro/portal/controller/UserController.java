package com.danielcastro.portal.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.danielcastro.portal.constant.FileConstant;
import com.danielcastro.portal.constant.SecurityConstant;
import com.danielcastro.portal.domain.HttpResponse;
import com.danielcastro.portal.domain.User;
import com.danielcastro.portal.domain.UserPrincipal;
import com.danielcastro.portal.exception.EmailExistsException;
import com.danielcastro.portal.exception.EmailNotFoundException;
import com.danielcastro.portal.exception.ExceptionHandling;
import com.danielcastro.portal.exception.LockedException;
import com.danielcastro.portal.exception.NotAnImageFileException;
import com.danielcastro.portal.exception.UserNotFoundException;
import com.danielcastro.portal.exception.UsernameExistsException;
import com.danielcastro.portal.service.UserService;
import com.danielcastro.portal.utility.JWTTokenProvider;

@RestController
@RequestMapping(path = {"/user"})
//@CrossOrigin("http://localhost:4200")
public class UserController extends ExceptionHandling {
	@Autowired
	private UserService userService;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JWTTokenProvider jwtTokenProvider;

	private final String EMAIL_SENT = "Email sent to: ";
	private final String USER_DELETED = "User deleted successfully.";

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {
		User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(),
				user.getEmail());
		return new ResponseEntity<>(newUser, HttpStatus.OK);
	}

	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) throws LockedException {
		authenticate(user.getUsername(), user.getPassword());
		User loginUser = userService.findUserByUsername(user.getUsername());
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
		return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName, @RequestParam("username") String username,
			@RequestParam("email") String email, @RequestParam("role") String role,
			@RequestParam("isActive") String isActive, @RequestParam("isNonLocked") String isNonLocked,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {

		User newUser = userService.addNewUser(firstName, lastName, username, email, role,
				Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<>(newUser, HttpStatus.OK);

	}

	@PostMapping("/update")
	public ResponseEntity<User> update(@RequestParam("currentUsername") String currentUsername,
			@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
			@RequestParam("username") String newUsername, @RequestParam("email") String email,
			@RequestParam("role") String role, @RequestParam("isActive") String isActive,
			@RequestParam("isNonLocked") String isNonLocked,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {

		User updatedUser = userService.updateUser(currentUsername, firstName, lastName, newUsername, email, role,
				Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<>(updatedUser, HttpStatus.OK);

	}

	@GetMapping("/find/{username}")
	public ResponseEntity<User> getUser(@PathVariable("username") String username) {
		User user = userService.findUserByUsername(username);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	@GetMapping("/list")
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = userService.getUsers();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@GetMapping("/resetpassword/{email}")
	public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email)
			throws AddressException, EmailNotFoundException, MessagingException {
		userService.resetPassword(email);
		return response(HttpStatus.OK, EMAIL_SENT + email);
	}

	@DeleteMapping("/delete/{username}")
	@PreAuthorize("hasAnyAuthority('user_delete')")
	public ResponseEntity<HttpResponse> deleteUser(@PathVariable("username") String username) {
		userService.deleteUser(username);
		return response(HttpStatus.OK, USER_DELETED);
	}

	@GetMapping(path = "/image/{username}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName)
			throws IOException {
		return Files
				.readAllBytes(Paths.get(FileConstant.USER_FOLDER + username + FileConstant.FORWARD_SLASH + fileName));

	}

	@GetMapping(path = "/image/profile/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getTempProfileImage(@PathVariable("fileName") String fileName) throws IOException {
		URL url = new URL(FileConstant.TEMP_PROFILE_IMAGE_BASE_URL + FileConstant.FORWARD_SLASH + fileName);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (InputStream inputStream = url.openStream()) {
			int bytesRead;
			byte[] chunk = new byte[1024];
			while ((bytesRead = inputStream.read(chunk)) > 0) {
				byteArrayOutputStream.write(chunk, 0, bytesRead);
			}

		}
		return byteArrayOutputStream.toByteArray();

	}
	
	@PostMapping("/updateProfileImage")
	public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username,
			@RequestParam("profileImage") MultipartFile profileImage)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageFileException {

		User user = userService.updateProfileImage(username, profileImage);
		return new ResponseEntity<>(user, HttpStatus.OK);

	}

	private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
		HttpResponse body = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(),
				message.toUpperCase());
		return new ResponseEntity<HttpResponse>(body, httpStatus);
	}

	private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(SecurityConstant.JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
		return headers;
	}

	private void authenticate(String username, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}

}
