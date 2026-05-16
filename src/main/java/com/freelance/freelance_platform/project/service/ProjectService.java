package com.freelance.freelance_platform.project.service;

import com.freelance.freelance_platform.identity.repository.SkillRepository;
import com.freelance.freelance_platform.identity.repository.UserRepository;
import com.freelance.freelance_platform.project.domain.Application;
import com.freelance.freelance_platform.project.domain.Contract;
import com.freelance.freelance_platform.project.domain.Project;
import com.freelance.freelance_platform.project.dto.*;
import com.freelance.freelance_platform.project.mapper.ProjectMapper;
import com.freelance.freelance_platform.project.repository.ApplicationRepository;
import com.freelance.freelance_platform.project.repository.ContractRepository;
import com.freelance.freelance_platform.project.repository.ProjectRepository;
import com.freelance.freelance_platform.shared.enums.ApplicationStatus;
import com.freelance.freelance_platform.shared.enums.ContractStatus;
import com.freelance.freelance_platform.shared.enums.ProjectStatus;
import com.freelance.freelance_platform.shared.exception.BusinessException;
import com.freelance.freelance_platform.shared.exception.ErrorCode;
import com.freelance.freelance_platform.shared.response.PageResponse;
import com.freelance.freelance_platform.shared.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ProjectMapper projectMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ── Projets ───────────────────────────────────────────────────

    @Transactional
    public ProjectDto createProject(CreateProjectRequest request) {
        var clientId = SecurityUtils.getCurrentUserId();
        var client = userRepository.findActiveById(clientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        var project = new Project();
        project.setClient(client);
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setBudgetMin(request.budgetMin());
        project.setBudgetMax(request.budgetMax());
        project.setDeadline(request.deadline());
        project.setStatus(ProjectStatus.OPEN);

        // Associer les skills si fournis
        if (request.skillIds() != null && !request.skillIds().isEmpty()) {
            var skills = new HashSet<>(skillRepository.findAllById(request.skillIds()));
            project.setSkills(skills);
        }

        project = projectRepository.save(project);
        log.info("Projet créé : {} par {}", project.getId(), clientId);

        return projectMapper.toDto(project);
    }

    @Transactional(readOnly = true)
    public ProjectDto getProject(UUID id) {
        var project = projectRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        return projectMapper.toDto(project);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectDto> getOpenProjects(String query, Pageable pageable) {
        var page = projectRepository.searchOpenProjects(query, pageable);
        return PageResponse.from(page.map(projectMapper::toDto));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectDto> getMyProjects(Pageable pageable) {
        var clientId = SecurityUtils.getCurrentUserId();
        var page = projectRepository.findByClientIdAndDeletedAtIsNull(clientId, pageable);
        return PageResponse.from(page.map(projectMapper::toDto));
    }

    @Transactional
    public ProjectDto updateProject(UUID id, UpdateProjectRequest request) {
        var clientId = SecurityUtils.getCurrentUserId();
        var project = projectRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // Vérifier que c'est bien le client propriétaire
        if (!project.getClient().getId().equals(clientId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        if (request.title() != null) project.setTitle(request.title());
        if (request.description() != null) project.setDescription(request.description());
        if (request.budgetMin() != null) project.setBudgetMin(request.budgetMin());
        if (request.budgetMax() != null) project.setBudgetMax(request.budgetMax());
        if (request.deadline() != null) project.setDeadline(request.deadline());

        if (request.skillIds() != null) {
            var skills = new HashSet<>(skillRepository.findAllById(request.skillIds()));
            project.setSkills(skills);
        }

        return projectMapper.toDto(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID id) {
        var clientId = SecurityUtils.getCurrentUserId();
        var project = projectRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getClient().getId().equals(clientId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        project.softDelete();
        projectRepository.save(project);
        log.info("Projet supprimé : {}", id);
    }

    // ── Candidatures ──────────────────────────────────────────────

    @Transactional
    public ApplicationDto applyToProject(CreateApplicationRequest request) {
        var freelancerId = SecurityUtils.getCurrentUserId();

        var project = projectRepository.findActiveById(request.projectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // Vérifier que le projet est ouvert
        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_OPEN);
        }

        // Vérifier que le freelancer n'a pas déjà postulé
        if (applicationRepository.existsByProjectIdAndFreelancerId(
                request.projectId(), freelancerId)) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        var freelancer = userRepository.findActiveById(freelancerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        var application = new Application();
        application.setProject(project);
        application.setFreelancer(freelancer);
        application.setCoverLetter(request.coverLetter());
        application.setProposedRate(request.proposedRate());

        application = applicationRepository.save(application);
        log.info("Candidature créée : {} pour le projet {}", freelancerId, request.projectId());

        return projectMapper.toDto(application);
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationDto> getProjectApplications(
            UUID projectId, Pageable pageable) {
        var clientId = SecurityUtils.getCurrentUserId();
        var project = projectRepository.findActiveById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getClient().getId().equals(clientId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        var page = applicationRepository.findByProjectId(projectId, pageable);
        return PageResponse.from(page.map(projectMapper::toDto));
    }

    @Transactional
    public ContractDto acceptApplication(UUID applicationId) {
        var clientId = SecurityUtils.getCurrentUserId();

        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getProject().getClient().getId().equals(clientId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // Accepter la candidature
        application.setStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);

        // Rejeter les autres candidatures
        applicationRepository.findByProjectId(application.getProject().getId(),
                        Pageable.unpaged())
                .forEach(app -> {
                    if (!app.getId().equals(applicationId)) {
                        app.setStatus(ApplicationStatus.REJECTED);
                        applicationRepository.save(app);
                    }
                });

        // Créer le contrat
        var contract = new Contract();
        contract.setProject(application.getProject());
        contract.setApplication(application);
        contract.setClient(application.getProject().getClient());
        contract.setFreelancer(application.getFreelancer());
        contract.setAgreedRate(application.getProposedRate());

        // Mettre à jour le statut du projet
        application.getProject().setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(application.getProject());

        contract = contractRepository.save(contract);
        log.info("Contrat créé : {}", contract.getId());

        return projectMapper.toDto(contract);
    }
}