import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from '../../../environments/environment';

interface InvitationInfo {
  token: string; orgName: string; inviterName: string; role: string; email: string;
}

@Component({
  selector: 'bp-accept-invite',
  standalone: true,
  imports: [TranslateModule, MatButtonModule, MatProgressSpinnerModule, MatIconModule],
  template: `
<div class="invite-wrapper">
  <div class="invite-card mat-elevation-z4">
    @if (loading()) {
      <mat-spinner diameter="48"></mat-spinner>
    } @else if (error()) {
      <mat-icon class="status-icon error-icon">error_outline</mat-icon>
      <h2>{{ 'invite.error_title' | translate }}</h2>
      <p class="msg">{{ error() }}</p>
      <button mat-flat-button color="primary" routerLink="/auth/login">{{ 'invite.go_login' | translate }}</button>
    } @else if (accepted()) {
      <mat-icon class="status-icon success-icon">check_circle</mat-icon>
      <h2>{{ 'invite.accepted_title' | translate }}</h2>
      <p class="msg">{{ 'invite.accepted_msg' | translate }}</p>
      <button mat-flat-button color="primary" (click)="goHome()">{{ 'invite.go_home' | translate }}</button>
    } @else if (info()) {
      <div class="org-name">{{ info()!.orgName }}</div>
      <h2>{{ 'invite.title' | translate }}</h2>
      <p class="msg">
        {{ info()!.inviterName }} {{ 'invite.invited_you' | translate }}
        <strong>{{ info()!.email }}</strong> {{ 'invite.as_role' | translate }}
        <span class="role-badge">{{ info()!.role }}</span>
      </p>
      <button mat-flat-button color="primary" [disabled]="accepting()" (click)="accept()">
        @if (accepting()) { <mat-spinner diameter="20"></mat-spinner> }
        @else { {{ 'invite.accept_btn' | translate }} }
      </button>
      <p class="note">{{ 'invite.must_be_logged' | translate }}</p>
    }
  </div>
</div>

<style>
  .invite-wrapper { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f5f5f5; }
  .invite-card { background: white; border-radius: 16px; padding: 48px 40px; max-width: 440px; width: 100%; text-align: center; }
  .status-icon { font-size: 56px; width: 56px; height: 56px; margin-bottom: 16px; }
  .error-icon { color: #d32f2f; }
  .success-icon { color: #2e7d32; }
  .org-name { font-size: 13px; color: #9e9e9e; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
  h2 { font-size: 22px; font-weight: 600; margin: 0 0 12px; }
  .msg { font-size: 15px; color: #616161; margin-bottom: 24px; }
  .role-badge { background: #e3f2fd; color: #1565c0; padding: 2px 10px; border-radius: 12px; font-size: 13px; font-weight: 500; }
  .note { font-size: 12px; color: #9e9e9e; margin-top: 16px; }
  button { width: 100%; }
</style>
  `
})
export class AcceptInviteComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  readonly loading = signal(true);
  readonly info = signal<InvitationInfo | null>(null);
  readonly accepting = signal(false);
  readonly accepted = signal(false);
  readonly error = signal<string | null>(null);

  private token = '';

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.error.set('Token manquant.');
      this.loading.set(false);
      return;
    }
    this.http.get<InvitationInfo>(`${environment.apiUrl}/team/invite/${this.token}`).subscribe({
      next: info => { this.info.set(info); this.loading.set(false); },
      error: () => { this.error.set('Invitation introuvable ou expirée.'); this.loading.set(false); }
    });
  }

  accept(): void {
    this.accepting.set(true);
    this.http.post(`${environment.apiUrl}/team/invite/accept?token=${this.token}`, {}).subscribe({
      next: (resp: any) => {
        if (resp?.accessToken) {
          localStorage.setItem('access_token', resp.accessToken);
          if (resp.refreshToken) localStorage.setItem('refresh_token', resp.refreshToken);
        }
        this.accepted.set(true);
        this.accepting.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Impossible d\'accepter l\'invitation.');
        this.accepting.set(false);
      }
    });
  }

  goHome(): void { this.router.navigate(['/dashboard']); }
}
