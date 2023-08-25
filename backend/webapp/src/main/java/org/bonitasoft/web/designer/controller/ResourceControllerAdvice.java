/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.controller;

import org.bonitasoft.web.designer.common.repository.exception.InUseException;
import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.exception.MalformedJsonException;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

import static org.springframework.http.HttpStatus.*;

/**
 * Definition of the exception handlers
 */
@Order
@ControllerAdvice
public class ResourceControllerAdvice implements BonitaExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceControllerAdvice.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException exception) {
        logger.error("Illegal Argument Exception", exception);
        return ResponseEntity.status(BAD_REQUEST).body(new ErrorMessage(exception));
    }

    @ExceptionHandler(ConstraintValidationException.class)
    @ResponseStatus(value = BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleConstraintValidationException(ConstraintValidationException exception) {
        logger.error("Constraint Validation Exception", exception);
        return ResponseEntity.status(BAD_REQUEST).body(new ErrorMessage(exception));
    }

    @ExceptionHandler(NotAllowedException.class)
    public ResponseEntity<ErrorMessage> handleNotAllowedException(NotAllowedException exception) {
        logger.error("Not Allowed Exception", exception);
        return ResponseEntity.status(FORBIDDEN).body(new ErrorMessage(exception));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> handleNotFoundException(NotFoundException exception) {
        logger.error("Element Not Found Exception", exception);
        return ResponseEntity.status(NOT_FOUND).body(new ErrorMessage(exception));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorMessage> handleIOException(IOException exception) {
        logger.error("Internal Server Error Exception", exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ErrorMessage(exception));
    }

    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<ErrorMessage> handleRepositoryException(RepositoryException exception) {
        logger.error("Internal Exception", exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ErrorMessage(exception));
    }

    @ExceptionHandler(InUseException.class)
    public ResponseEntity<ErrorMessage> handleInUseException(InUseException exception) {
        logger.error("Element In Use Exception", exception);
        return ResponseEntity.status(CONFLICT).body(new ErrorMessage(exception));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorMessage> handleRepositoryException(RuntimeException exception) {
        logger.error("Internal Exception", exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ErrorMessage(exception));
    }

    @ExceptionHandler(ImportException.class)
    public ResponseEntity<ErrorMessage> handleImportException(ImportException exception) {
        logger.error("Technical error when importing a component", exception);
        // BS-14113: HttpStatus.ACCEPTED internet explorer don't recognize response if sent with http error code
        var errorMessage = new ErrorMessage(exception.getType().toString(), exception.getMessage());
        errorMessage.addInfos(exception.getInfos());
        return ResponseEntity.status(ACCEPTED).body(errorMessage);
    }

    @ExceptionHandler(MalformedJsonException.class)
    public ResponseEntity<ErrorMessage> handleJsonProcessingException(MalformedJsonException exception) {
        logger.error("Error while uploading a json file {}", exception.getMessage());
        // BS-14113: HttpStatus.ACCEPTED internet explorer don't recognize response if sent with http error code
        var message = new ErrorMessage(exception);
        message.addInfo("location", exception.getLocationInfos());
        return ResponseEntity.status(ACCEPTED).body(message);
    }
}
