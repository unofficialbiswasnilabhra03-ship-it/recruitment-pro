package com.recruitpro.service.impl;

import com.recruitpro.dto.response.DashboardResponse;
import com.recruitpro.enums.ApplicationStatus;
import com.recruitpro.enums.InterviewStatus;
import com.recruitpro.enums.RoleName;
import com.recruitpro.repository.*;
import com.recruitpro.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final JobRepository            jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final CandidateRepository      candidateRepository;
    private final InterviewRepository      interviewRepository;
    private final UserRepository           userRepository;

    // ── Global (Admin) ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getGlobalDashboard() {

        Map<String, Long> byStatus = buildStatusBreakdown(null);

        return DashboardResponse.builder()
                // Jobs
                .totalJobs(jobRepository.countActive())
                .openJobs(jobRepository.countOpen())
                .closedJobs(applicationRepository.countByStatus(ApplicationStatus.REJECTED))   // proxy
                .draftJobs(0L)  // no dedicated count query needed; derive if required

                // Applications
                .totalApplications(applicationRepository.count())
                .newApplications(applicationRepository.countByStatus(ApplicationStatus.APPLIED))
                .shortlistedApplications(applicationRepository.countByStatus(ApplicationStatus.SHORTLISTED))
                .hiredCandidates(applicationRepository.countByStatus(ApplicationStatus.HIRED))
                .rejectedApplications(applicationRepository.countByStatus(ApplicationStatus.REJECTED))

                // Candidates
                .totalCandidates(candidateRepository.countActive())

                // Interviews
                .totalInterviews(interviewRepository.count())
                .scheduledInterviews(interviewRepository.countByStatus(InterviewStatus.SCHEDULED))
                .completedInterviews(interviewRepository.countByStatus(InterviewStatus.COMPLETED))

                // People
                .totalHrUsers(userRepository.countByRoleName(RoleName.ROLE_HR))
                .totalInterviewers(userRepository.countByRoleName(RoleName.ROLE_INTERVIEWER))

                // Breakdown maps
                .applicationsByStatus(byStatus)
                .monthlyHiringTrend(new LinkedHashMap<>()) // populated via future analytics query
                .build();
    }

    // ── Company-scoped (HR) ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getCompanyDashboard(Long companyId) {

        Map<String, Long> byStatus = buildStatusBreakdown(companyId);

        return DashboardResponse.builder()
                // Jobs
                .totalJobs(jobRepository.countOpenByCompany(companyId))
                .openJobs(jobRepository.countOpenByCompany(companyId))

                // Applications
                .totalApplications(applicationRepository.countByCompanyId(companyId))
                .newApplications(applicationRepository.countByCompanyIdAndStatus(
                        companyId, ApplicationStatus.APPLIED))
                .shortlistedApplications(applicationRepository.countByCompanyIdAndStatus(
                        companyId, ApplicationStatus.SHORTLISTED))
                .hiredCandidates(applicationRepository.countByCompanyIdAndStatus(
                        companyId, ApplicationStatus.HIRED))
                .rejectedApplications(applicationRepository.countByCompanyIdAndStatus(
                        companyId, ApplicationStatus.REJECTED))

                // Candidates (distinct applicants to this company)
                .totalCandidates(applicationRepository.countDistinctCandidatesByCompany(companyId))

                // Interviews
                .totalInterviews(interviewRepository.countByCompanyId(companyId))
                .scheduledInterviews(interviewRepository.countByCompanyIdAndStatus(
                        companyId, InterviewStatus.SCHEDULED))
                .completedInterviews(interviewRepository.countByCompanyIdAndStatus(
                        companyId, InterviewStatus.COMPLETED))

                .applicationsByStatus(byStatus)
                .monthlyHiringTrend(new LinkedHashMap<>())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Long> buildStatusBreakdown(Long companyId) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = companyId != null
                    ? applicationRepository.countByCompanyIdAndStatus(companyId, status)
                    : applicationRepository.countByStatus(status);
            map.put(status.name(), count);
        }
        return map;
    }
}
