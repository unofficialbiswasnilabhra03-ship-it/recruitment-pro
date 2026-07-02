package com.recruitpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CandidateSkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(max = 100)
    private String skillName;

    @Size(max = 30)
    private String proficiencyLevel; // BEGINNER / INTERMEDIATE / ADVANCED / EXPERT

    private Integer yearsOfExperience;
}
