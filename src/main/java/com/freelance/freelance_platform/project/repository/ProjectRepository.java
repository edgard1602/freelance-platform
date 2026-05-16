package com.freelance.freelance_platform.project.repository;


import com.freelance.freelance_platform.project.domain.Project;
import com.freelance.freelance_platform.shared.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Page<Project> findByStatusAndDeletedAtIsNull(ProjectStatus status, Pageable pageable);

    Page<Project> findByClientIdAndDeletedAtIsNull(UUID clientId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Project> findActiveById(UUID id);

    @Query("""
            SELECT p FROM Project p
            WHERE p.deletedAt IS NULL
            AND p.status = 'OPEN'
            AND (CAST(:query AS string) IS NULL
                OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Project> searchOpenProjects(String query, Pageable pageable);
}