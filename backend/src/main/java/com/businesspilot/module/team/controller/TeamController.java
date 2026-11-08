package com.businesspilot.module.team.controller;

import com.businesspilot.module.auth.dto.AuthResponse;
import com.businesspilot.module.team.dto.*;
import com.businesspilot.module.team.service.TeamService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/api/team/invite/{token}")
    public InvitationInfoResponse getInvitationInfo(@PathVariable UUID token) {
        return teamService.getInvitationInfo(token);
    }

    @PostMapping("/api/team/invite/accept")
    @SecurityRequirement(name = "Bearer")
    public AuthResponse acceptInvitation(@RequestParam UUID token) {
        return teamService.acceptInvitation(token);
    }

    @GetMapping("/api/team")
    @SecurityRequirement(name = "Bearer")
    public List<TeamMemberResponse> listMembers() {
        return teamService.listMembers();
    }

    @GetMapping("/api/team/invitations")
    @SecurityRequirement(name = "Bearer")
    public List<PendingInvitationResponse> listPendingInvitations() {
        return teamService.listPendingInvitations();
    }

    @PostMapping("/api/team/invite")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "Bearer")
    public void invite(@Valid @RequestBody TeamInviteRequest request) {
        teamService.invite(request);
    }

    @DeleteMapping("/api/team/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "Bearer")
    public void removeMember(@PathVariable UUID userId) {
        teamService.removeMember(userId);
    }

    @DeleteMapping("/api/team/invitations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "Bearer")
    public void cancelInvitation(@PathVariable UUID id) {
        teamService.cancelInvitation(id);
    }
}
