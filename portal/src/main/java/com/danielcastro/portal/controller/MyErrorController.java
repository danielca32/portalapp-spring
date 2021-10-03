package com.danielcastro.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.danielcastro.portal.domain.HttpResponse;
import com.danielcastro.portal.exception.ExceptionHandling;

@RestController
public class MyErrorController implements ErrorController {
	
	@Autowired
	ExceptionHandling exceptionHandling;

	@RequestMapping("/error")
	public ResponseEntity<HttpResponse> notFound404() {
		return exceptionHandling.notFound404();
	}

}
