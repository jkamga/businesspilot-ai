package com.businesspilot.module.admin.controller;

import com.businesspilot.module.admin.dto.AdminOrgResponse;
import com.businesspilot.module.admin.dto.AdminStatsResponse;
import com.businesspilot.module.admin.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "Bearer")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminService.getStats();
    }

    @GetMapping("/organizations")
    public Page<AdminOrgResponse> listOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.listOrganizations(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }
}
