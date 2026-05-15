package com.freelance.freelance_platform.identity.service;


import com.freelance.freelance_platform.identity.domain.FreelancerProfile;
import com.freelance.freelance_platform.identity.domain.User;
import com.freelance.freelance_platform.identity.dto.*;
import com.freelance.freelance_platform.identity.mapper.UserMapper;
import com.freelance.freelance_platform.identity.repository.FreelancerProfileRepository;
import com.freelance.freelance_platform.identity.repository.UserRepository;
import com.freelance.freelance_platform.security.JwtService;
import com.freelance.freelance_platform.shared.enums.UserRole;
import com.freelance.freelance_platform.shared.enums.UserStatus;
import com.freelance.freelance_platform.shared.exception.BusinessException;
import com.freelance.freelance_platform.shared.exception.ErrorCode;
import com.freelance.freelance_platform.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final AppProperties appProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true); // TODO: implémenter vérification email

        user = userRepository.save(user);

        // Créer automatiquement un profil si c'est un freelancer
        if (request.role() == UserRole.FREELANCER) {
            FreelancerProfile profile = new FreelancerProfile();
            profile.setUser(user);
            freelancerProfileRepository.save(profile);
            log.info("FreelancerProfile créé pour l'utilisateur {}", user.getId());
        }

        // Générer les tokens
        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // Trouver l'utilisateur
        User user = userRepository.findActiveByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Vérifier le statut du compte
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        log.info("Utilisateur connecté : {}", user.getEmail());
        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {

        // Valider le refresh token et récupérer l'userId
        var userId = refreshTokenService.validateAndGetUserId(request.refreshToken());

        // Récupérer l'utilisateur
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Révoquer l'ancien token et générer un nouveau
        refreshTokenService.revoke(request.refreshToken());

        return generateAuthResponse(user);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
        log.info("Utilisateur déconnecté");
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtService.generateRefreshToken(user.getId());
        refreshTokenService.save(refreshToken, user);

        return AuthResponse.of(
                accessToken,
                refreshToken,
                appProperties.jwt().accessTokenExpiration(),
                userMapper.toDto(user)
        );
    }
}