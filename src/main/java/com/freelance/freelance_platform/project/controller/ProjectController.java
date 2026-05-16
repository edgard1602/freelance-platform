package com.freelance.freelance_platform.project.controller;

import com.freelance.freelance_platform.project.dto.*;
import com.freelance.freelance_platform.project.service.ProjectService;
import com.freelance.freelance_platform.shared.response.ApiResponse;
import com.freelance.freelance_platform.shared.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Gestion des projets et candidatures")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    // ── Projets ───────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Créer un projet")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        projectService.createProject(request),
                        "Projet créé avec succès"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un projet par ID")
    public ResponseEntity<ApiResponse<ProjectDto>> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProject(id)));
    }

    @GetMapping
    @Operation(summary = "Lister les projets ouverts")
    public ResponseEntity<ApiResponse<PageResponse<ProjectDto>>> getOpenProjects(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                projectService.getOpenProjects(query, pageable)));
    }

    @GetMapping("/my")
    @Operation(summary = "Mes projets (client)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<PageResponse<ProjectDto>>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                projectService.getMyProjects(pageable)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un projet")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                projectService.updateProject(id, request),
                "Projet modifié avec succès"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un projet")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Projet supprimé"));
    }

    // ── Candidatures ──────────────────────────────────────────────

    @PostMapping("/applications")
    @Operation(summary = "Postuler à un projet")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ApplicationDto>> applyToProject(
            @Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        projectService.applyToProject(request),
                        "Candidature envoyée avec succès"));
    }

    @GetMapping("/{projectId}/applications")
    @Operation(summary = "Voir les candidatures d'un projet")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationDto>>> getProjectApplications(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                projectService.getProjectApplications(projectId, pageable)));
    }

    @PostMapping("/applications/{applicationId}/accept")
    @Operation(summary = "Accepter une candidature et créer un contrat")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ContractDto>> acceptApplication(
            @PathVariable UUID applicationId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        projectService.acceptApplication(applicationId),
                        "Candidature acceptée — contrat créé"));
    }
}
