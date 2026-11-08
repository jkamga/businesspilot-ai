package com.businesspilot.module.team.service;

import com.businesspilot.common.exception.BusinessException;
import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.security.UserPrincipal;
import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.auth.domain.UserOrganizationRole;
import com.businesspilot.module.auth.domain.UserRole;
import com.businesspilot.module.auth.repository.OrganizationRepository;
import com.businesspilot.module.auth.repository.UserOrganizationRoleRepository;
import com.businesspilot.module.auth.repository.UserRepository;
import com.businesspilot.module.auth.service.AuthService;
import com.businesspilot.module.team.domain.TeamInvitation;
import com.businesspilot.module.team.dto.*;
import com.businesspilot.module.team.repository.TeamInvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamInvitationRepository invitationRepo;
    private final UserOrganizationRoleRepository roleRepo;
    private final UserRepository userRepo;
    private final OrganizationRepository orgRepo;
    private final AuthService authService;
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.mail.from:noreply@businesspilot.app}")
    private String mailFrom;

    @Transactional
    public void invite(TeamInviteRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        UUID orgUuid = UUID.fromString(orgId);
        String email = request.email().toLowerCase();
        String role = request.role() != null ? request.role() : "MEMBER";

        if (invitationRepo.existsByOrganizationIdAndEmailAndAcceptedAtIsNull(orgUuid, email)) {
            throw new BusinessException("Une invitation est déjà en attente pour cet email");
        }

        boolean alreadyMember = roleRepo.findByOrganizationIdAndRoleIn(orgUuid, List.of(
            UserRole.OWNER, UserRole.ADMIN, UserRole.MEMBER, UserRole.VIEWER))
            .stream()
            .anyMatch(r -> userRepo.findById(r.getUserId())
                .map(u -> u.getEmail().equals(email)).orElse(false));
        if (alreadyMember) {
            throw new BusinessException("Cet utilisateur est déjà membre de l'organisation");
        }

        UUID currentUserId = currentUserId();
        String inviterName = userRepo.findById(currentUserId)
            .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Un collègue");
        String orgName = orgRepo.findById(orgUuid).map(o -> o.getName()).orElse("votre organisation");

        TeamInvitation inv = new TeamInvitation();
        inv.setOrganizationId(orgUuid);
        inv.setEmail(email);
        inv.setRole(role);
        inv.setToken(UUID.randomUUID());
        inv.setInvitedBy(currentUserId);
        inv.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invitationRepo.save(inv);

        sendInvitationEmail(email, inviterName, orgName, inv.getToken(), role);
    }

    @Transactional(readOnly = true)
    public InvitationInfoResponse getInvitationInfo(UUID token) {
        TeamInvitation inv = invitationRepo.findByToken(token)
            .orElseThrow(() -> new NotFoundException("Invitation introuvable ou expirée"));

        if (inv.getAcceptedAt() != null) {
            throw new BusinessException("Cette invitation a déjà été acceptée");
        }
        if (inv.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Cette invitation a expiré");
        }

        String orgName = orgRepo.findById(inv.getOrganizationId()).map(o -> o.getName()).orElse("?");
        String inviterName = userRepo.findById(inv.getInvitedBy())
            .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Un collègue");

        return new InvitationInfoResponse(token, orgName, inviterName, inv.getRole(), inv.getEmail());
    }

    @Transactional
    public com.businesspilot.module.auth.dto.AuthResponse acceptInvitation(UUID token) {
        TeamInvitation inv = invitationRepo.findByToken(token)
            .orElseThrow(() -> new NotFoundException("Invitation introuvable ou expirée"));

        if (inv.getAcceptedAt() != null) {
            throw new BusinessException("Cette invitation a déjà été acceptée");
        }
        if (inv.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Cette invitation a expiré");
        }

        UUID currentUserId = currentUserId();
        com.businesspilot.module.auth.domain.User currentUser = userRepo.findById(currentUserId)
            .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        if (!currentUser.getEmail().equalsIgnoreCase(inv.getEmail())) {
            throw new BusinessException("Cette invitation ne correspond pas à votre adresse email");
        }

        boolean alreadyMember = roleRepo.findByUserIdAndOrganizationId(currentUserId, inv.getOrganizationId()).isPresent();
        if (!alreadyMember) {
            UserOrganizationRole role = UserOrganizationRole.builder()
                .userId(currentUserId)
                .organizationId(inv.getOrganizationId())
                .role(UserRole.valueOf(inv.getRole()))
                .build();
            roleRepo.save(role);
        }

        inv.setAcceptedAt(Instant.now());
        invitationRepo.save(inv);

        return authService.issueTokens(currentUser, inv.getOrganizationId(),
            UserRole.valueOf(inv.getRole()), false);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> listMembers() {
        String orgId = TenantContext.getCurrentTenant();
        UUID orgUuid = UUID.fromString(orgId);
        return roleRepo.findByOrganizationIdAndRoleIn(orgUuid,
            List.of(UserRole.OWNER, UserRole.ADMIN, UserRole.MEMBER, UserRole.VIEWER))
            .stream()
            .flatMap(r -> userRepo.findById(r.getUserId()).stream()
                .map(u -> new TeamMemberResponse(u.getId(), u.getEmail(),
                    u.getFirstName(), u.getLastName(), r.getRole().name(), r.getCreatedAt())))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PendingInvitationResponse> listPendingInvitations() {
        String orgId = TenantContext.getCurrentTenant();
        return invitationRepo.findAllByOrganizationIdAndAcceptedAtIsNull(UUID.fromString(orgId))
            .stream()
            .filter(inv -> inv.getExpiresAt().isAfter(Instant.now()))
            .map(inv -> new PendingInvitationResponse(inv.getId(), inv.getEmail(),
                inv.getRole(), inv.getExpiresAt(), inv.getCreatedAt()))
            .toList();
    }

    @Transactional
    public void removeMember(UUID userId) {
        String orgId = TenantContext.getCurrentTenant();
        UUID orgUuid = UUID.fromString(orgId);
        UUID currentUserId = currentUserId();
        if (userId.equals(currentUserId)) {
            throw new BusinessException("Vous ne pouvez pas vous retirer vous-même");
        }
        UserOrganizationRole role = roleRepo.findByUserIdAndOrganizationId(userId, orgUuid)
            .orElseThrow(() -> new NotFoundException("Membre introuvable"));
        if (role.getRole() == UserRole.OWNER) {
            throw new BusinessException("Impossible de retirer le propriétaire de l'organisation");
        }
        roleRepo.delete(role);
    }

    @Transactional
    public void cancelInvitation(UUID invitationId) {
        String orgId = TenantContext.getCurrentTenant();
        TeamInvitation inv = invitationRepo.findById(invitationId)
            .orElseThrow(() -> new NotFoundException("Invitation introuvable"));
        if (!inv.getOrganizationId().equals(UUID.fromString(orgId))) {
            throw new BusinessException("Accès refusé");
        }
        invitationRepo.delete(inv);
    }

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.businesspilot.common.security.UserPrincipal p) {
            return p.getUserId();
        }
        throw new BusinessException("Utilisateur non authentifié");
    }

    private void sendInvitationEmail(String toEmail, String inviterName, String orgName, UUID token, String role) {
        try {
            String link = frontendUrl + "/accept-invite?token=" + token;
            String subject = inviterName + " vous invite à rejoindre " + orgName + " sur BusinessPilot";
            String body = "Bonjour,\n\n" +
                inviterName + " vous invite à rejoindre l'organisation \"" + orgName +
                "\" sur BusinessPilot AI en tant que " + role.toLowerCase() + ".\n\n" +
                "Cliquez sur le lien ci-dessous pour accepter l'invitation :\n" + link +
                "\n\nCe lien expire dans 7 jours.\n\n-- L'équipe BusinessPilot";

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(toEmail);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (MailException e) {
            log.error("[INVITE] Échec envoi email vers {}: {}", toEmail, e.getMessage());
        }
    }
}
