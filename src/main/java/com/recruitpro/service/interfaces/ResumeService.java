package com.recruitpro.service.interfaces;

import com.recruitpro.dto.response.ResumeResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    ResumeResponse uploadResume(Long userId, MultipartFile file);

    ResumeResponse getMyResume(Long userId);

    ResumeResponse getResumeByCandidateId(Long candidateId);

    Resource downloadResume(Long candidateId);

    void deleteResume(Long userId);
}
