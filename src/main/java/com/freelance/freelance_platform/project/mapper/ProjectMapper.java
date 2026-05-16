package com.freelance.freelance_platform.project.mapper;


import com.freelance.freelance_platform.identity.domain.Skill;
import com.freelance.freelance_platform.project.domain.Application;
import com.freelance.freelance_platform.project.domain.Contract;
import com.freelance.freelance_platform.project.domain.Project;
import com.freelance.freelance_platform.project.dto.ApplicationDto;
import com.freelance.freelance_platform.project.dto.ContractDto;
import com.freelance.freelance_platform.project.dto.ProjectDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(project.getClient().getFullName())")
    @Mapping(target = "skills", expression = "java(project.getSkills().stream().map(com.freelance.freelance_platform.identity.domain.Skill::getName).collect(java.util.stream.Collectors.toSet()))")
    ProjectDto toDto(Project project);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectTitle", source = "project.title")
    @Mapping(target = "freelancerId", source = "freelancer.id")
    @Mapping(target = "freelancerName", expression = "java(application.getFreelancer().getFullName())")
    ApplicationDto toDto(Application application);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectTitle", source = "project.title")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(contract.getClient().getFullName())")
    @Mapping(target = "freelancerId", source = "freelancer.id")
    @Mapping(target = "freelancerName", expression = "java(contract.getFreelancer().getFullName())")
    ContractDto toDto(Contract contract);
}