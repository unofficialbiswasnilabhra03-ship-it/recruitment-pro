package com.recruitpro.dto.request;

import com.recruitpro.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    private String hrNotes;

    private String rejectionReason;
}
