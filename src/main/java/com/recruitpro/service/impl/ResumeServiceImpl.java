package com.recruitpro.service.impl;

import com.recruitpro.constants.AppConstants;
import com.recruitpro.dto.response.ResumeResponse;
import com.recruitpro.entity.Candidate;
import com.recruitpro.entity.Resume;
import com.recruitpro.exception.BadRequestException;
import com.recruitpro.exception.ResourceNotFoundException;
import com.recruitpro.repository.CandidateRepository;
import com.recruitpro.repository.ResumeRepository;
import com.recruitpro.service.interfaces.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository     resumeRepository;
    private final CandidateRepository  candidateRepository;

    @Value("${app.storage.mode:LOCAL}")
    private String storageMode;

    @Value("${app.storage.local.base-path:./uploads}")
    private String localBasePath;

    // ── Upload ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ResumeResponse uploadResume(Long userId, MultipartFile file) {
        validateFile(file);

        Candidate candidate = findCandidateByUserId(userId);

        // Delete existing resume before replacing
        resumeRepository.findByCandidateId(candidate.getId())
                .ifPresent(existing -> {
                    deletePhysicalFile(existing.getFilePath());
                    resumeRepository.delete(existing);
                });

        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath   = storeFile(file, storedName);

        Resume resume = Resume.builder()
                .candidate(candidate)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedName)
                .filePath(filePath)
                .fileSizeBytes(file.getSize())
                .contentType(file.getContentType())
                .storageBackend(storageMode)
                .build();

        return toResponse(resumeRepository.save(resume));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getMyResume(Long userId) {
        Candidate candidate = findCandidateByUserId(userId);
        Resume resume = resumeRepository.findByCandidateId(candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found for this candidate"));
        return toResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResumeByCandidateId(Long candidateId) {
        Resume resume = resumeRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "candidateId", candidateId));
        return toResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadResume(Long candidateId) {
        Resume resume = resumeRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "candidateId", candidateId));
        try {
            Path path = Paths.get(resume.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BadRequestException("Resume file is not accessible");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BadRequestException("Could not resolve resume file path");
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteResume(Long userId) {
        Candidate candidate = findCandidateByUserId(userId);
        Resume resume = resumeRepository.findByCandidateId(candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found for this candidate"));
        deletePhysicalFile(resume.getFilePath());
        resumeRepository.delete(resume);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String contentType = file.getContentType();
        if (!AppConstants.ALLOWED_RESUME_TYPE_PDF.equals(contentType)
                && !AppConstants.ALLOWED_RESUME_TYPE_DOCX.equals(contentType)) {
            throw new BadRequestException("Only PDF and DOCX files are allowed");
        }
        if (file.getSize() > AppConstants.MAX_RESUME_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds the 5 MB limit");
        }
    }

    private String storeFile(MultipartFile file, String storedName) {
        try {
            Path uploadDir = Paths.get(localBasePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            log.error("Failed to store resume file: {}", e.getMessage());
            throw new BadRequestException("Failed to store the uploaded file");
        }
    }

    private void deletePhysicalFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete physical file at {}: {}", filePath, e.getMessage());
        }
    }

    private Candidate findCandidateByUserId(Long userId) {
        return candidateRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate profile not found for user id: " + userId));
    }

    private ResumeResponse toResponse(Resume r) {
        return ResumeResponse.builder()
                .id(r.getId())
                .candidateId(r.getCandidate().getId())
                .originalFileName(r.getOriginalFileName())
                .contentType(r.getContentType())
                .fileSizeBytes(r.getFileSizeBytes())
                .storageBackend(r.getStorageBackend())
                .downloadUrl("/api/v1/resumes/candidate/" + r.getCandidate().getId() + "/download")
                .uploadedAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
