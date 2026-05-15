package com.freelance.freelance_platform.identity.repository;

import com.freelance.freelance_platform.identity.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findByNameIgnoreCase(String name);

    List<Skill> findByCategory(String category);

    boolean existsByNameIgnoreCase(String name);
}
