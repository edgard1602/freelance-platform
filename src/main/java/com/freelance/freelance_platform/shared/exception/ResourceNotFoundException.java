package com.freelance.freelance_platform.shared.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(ErrorCode.USER_NOT_FOUND, resourceName + " introuvable avec l'id : " + id);
    }
}