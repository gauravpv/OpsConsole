package com.opsconsole.auth;

import com.opsconsole.admin.ServiceAdminException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = {RoleAdminApiController.class, com.opsconsole.admin.ServiceAdminApiController.class})
public class AdminApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RoleAdminApiController.ErrorResponse> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new RoleAdminApiController.ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ServiceAdminException.class)
    public ResponseEntity<RoleAdminApiController.ErrorResponse> serviceAdminFailed(ServiceAdminException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new RoleAdminApiController.ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RoleAdminApiController.ErrorResponse> unauthorized(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new RoleAdminApiController.ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<RoleAdminApiController.ErrorResponse> responseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode())
                .body(new RoleAdminApiController.ErrorResponse(message));
    }
}
