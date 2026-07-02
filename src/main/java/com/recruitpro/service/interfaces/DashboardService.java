package com.recruitpro.service.interfaces;

import com.recruitpro.dto.response.DashboardResponse;

public interface DashboardService {

    /** Global stats — Admin view */
    DashboardResponse getGlobalDashboard();

    /** Company-scoped stats — HR view */
    DashboardResponse getCompanyDashboard(Long companyId);
}
