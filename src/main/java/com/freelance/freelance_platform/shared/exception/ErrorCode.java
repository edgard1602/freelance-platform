package com.freelance.freelance_platform.shared.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth
    INVALID_CREDENTIALS("AUTH_001", "Email ou mot de passe incorrect", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_002", "Token expiré", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH_003", "Token invalide", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_VERIFIED("AUTH_004", "Email non vérifié", HttpStatus.FORBIDDEN),
    ACCOUNT_SUSPENDED("AUTH_005", "Compte suspendu", HttpStatus.FORBIDDEN),

    // Ressources
    USER_NOT_FOUND("USER_001", "Utilisateur introuvable", HttpStatus.NOT_FOUND),
    PROJECT_NOT_FOUND("PROJ_001", "Projet introuvable", HttpStatus.NOT_FOUND),
    APPLICATION_NOT_FOUND("APP_001", "Candidature introuvable", HttpStatus.NOT_FOUND),
    CONTRACT_NOT_FOUND("CONT_001", "Contrat introuvable", HttpStatus.NOT_FOUND),

    // Business rules
    ALREADY_APPLIED("APP_002", "Vous avez déjà postulé à ce projet", HttpStatus.CONFLICT),
    PROJECT_NOT_OPEN("PROJ_002", "Ce projet n'accepte plus de candidatures", HttpStatus.CONFLICT),
    CANNOT_REVIEW_OWN_CONTRACT("REV_001", "Impossible de s'auto-évaluer", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS("REV_002", "Vous avez déjà évalué ce contrat", HttpStatus.CONFLICT),
    UNAUTHORIZED_ACTION("SEC_001", "Action non autorisée", HttpStatus.FORBIDDEN),

    // Validation
    VALIDATION_ERROR("VAL_001", "Données invalides", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("VAL_002", "Cet email est déjà utilisé", HttpStatus.CONFLICT),

    // Serveur
    INTERNAL_ERROR("SRV_001", "Erreur interne du serveur", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}