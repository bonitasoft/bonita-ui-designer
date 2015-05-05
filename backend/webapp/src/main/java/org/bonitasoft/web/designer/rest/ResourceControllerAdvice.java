/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.rest;

import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Definition of the exception handlers
 */
@ControllerAdvice
public class ResourceControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ResourceControllerAdvice.class);

    /**
     * Construct Header to specify content-type
     */
    public static HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException exception) {
        logger.error("Illegal Argument Exception", exception);
        return new ResponseEntity<>(new ErrorMessage(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintValidationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleConstraintValidationException(ConstraintValidationException exception) {
        logger.error("Constraint Validation Exception", exception);
        return new ResponseEntity<>(new ErrorMessage(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotAllowedException.class)
    public ResponseEntity<ErrorMessage> handleNotAllowedException(NotAllowedException exception) {
        logger.error("Not Allowed Exception", exception);
        return new ResponseEntity<>(new ErrorMessage(exception), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> handleNotFoundException(NotFoundException exception) {
        logger.error("Element Not Found Exception", exception);
        return new ResponseEntity<>(new ErrorMessage(exception), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<ErrorMessage> handleRepositoryException(RepositoryException exception) {
        logger.error("Internal Exception", exception);
        return new ResponseEntity<>(new ErrorMessage(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InUseException.class)
    public ResponseEntity<ErrorMessage> handleInUseException(InUseException exception) {
        logger.error("Element In Use Exception", exception);
        return new ResponseEntity<>(new ErrorMessage(exception), HttpStatus.CONFLICT);
    }
}
