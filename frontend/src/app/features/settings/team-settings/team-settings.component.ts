import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { DatePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from '../../../../environments/environment';

interface TeamMember { userId: string; email: string; firstName: string | null; lastName: string | null; role: string; joinedAt: string; }
interface PendingInvitation { id: string; email: string; role: string; expiresAt: string; }

@Component({
  selector: 'bp-team-settings',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, TranslateModule,
    MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatProgressSpinnerModule, MatDividerModule],
  template: `
<div class="settings-page">
  <h2>{{ 'settings.team.title' | translate }}</h2>
  <p class="subtitle">{{ 'settings.team.subtitle' | translate }}</p>

  <!-- Invite form -->
  <form [formGroup]="form" (ngSubmit)="sendInvite()" class="invite-form mat-elevation-z1">
    <mat-form-field appearance="outline">
      <mat-label>{{ 'settings.team.invite_email' | translate }}</mat-label>
      <input matInput formControlName="email" type="email" autocomplete="off">
    </mat-form-field>
    <mat-form-field appearance="outline">
      <mat-label>{{ 'settings.team.invite_role' | translate }}</mat-label>
      <mat-select formControlName="role">
        <mat-option value="ADMIN">{{ 'settings.team.role_ADMIN' | translate }}</mat-option>
        <mat-option value="MEMBER">{{ 'settings.team.role_MEMBER' | translate }}</mat-option>
        <mat-option value="VIEWER">{{ 'settings.team.role_VIEWER' | translate }}</mat-option>
      </mat-select>
    </mat-form-field>
    <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || sending()">
      @if (sending()) { <mat-spinner diameter="20"></mat-spinner> }
      @else { {{ 'settings.team.invite_send' | translate }} }
    </button>
    @if (inviteSent()) { <span class="success-msg">{{ 'settings.team.invite_success' | translate }} ✓</span> }
  </form>

  <mat-divider class="divider"/>

  <!-- Members -->
  <h3>{{ 'settings.team.members_title' | translate }}</h3>
  @if (members().length === 0) {
    <p class="empty">{{ 'settings.team.empty_members' | translate }}</p>
  } @else {
    <div class="member-list">
      @for (m of members(); track m.userId) {
        <div class="member-row">
          <div class="member-avatar">{{ initials(m) }}</div>
          <div class="member-info">
            <span class="member-name">{{ m.firstName }} {{ m.lastName }}</span>
            <span class="member-email">{{ m.email }}</span>
          </div>
          <span class="member-role">{{ 'settings.team.role_' + m.role | translate }}</span>
          @if (m.role !== 'OWNER') {
            <button mat-icon-button color="warn" (click)="removeMember(m.userId)" title="{{ 'settings.team.remove' | translate }}">
              <mat-icon>person_remove</mat-icon>
            </button>
          }
        </div>
      }
    </div>
  }

  <mat-divider class="divider"/>

  <!-- Pending invitations -->
  <h3>{{ 'settings.team.invitations_title' | translate }}</h3>
  @if (invitations().length === 0) {
    <p class="empty">{{ 'settings.team.empty_invites' | translate }}</p>
  } @else {
    <div class="member-list">
      @for (inv of invitations(); track inv.id) {
        <div class="member-row">
          <mat-icon class="inv-icon">email</mat-icon>
          <div class="member-info">
            <span class="member-name">{{ inv.email }}</span>
            <span class="member-email">{{ 'settings.team.role_' + inv.role | translate }} · {{ 'settings.team.expires' | translate }} {{ inv.expiresAt | date:'dd/MM/yyyy' }}</span>
          </div>
          <button mat-icon-button color="warn" (click)="cancelInvitation(inv.id)">
            <mat-icon>close</mat-icon>
          </button>
        </div>
      }
    </div>
  }
</div>

<style>
  .settings-page { max-width: 700px; padding: 24px; }
  .subtitle { color: #757575; font-size: 14px; margin-bottom: 24px; }
  .invite-form { display: flex; gap: 12px; align-items: flex-start; padding: 20px; border-radius: 12px; flex-wrap: wrap; margin-bottom: 24px; }
  .invite-form mat-form-field { flex: 1; min-width: 200px; }
  .success-msg { color: #2e7d32; font-size: 13px; align-self: center; }
  .divider { margin: 16px 0 20px; }
  h3 { font-size: 15px; font-weight: 600; margin: 0 0 12px; }
  .empty { color: #9e9e9e; font-size: 13px; }
  .member-list { display: flex; flex-direction: column; gap: 4px; }
  .member-row { display: flex; align-items: center; gap: 12px; padding: 10px 8px; border-radius: 8px; }
  .member-row:hover { background: rgba(0,0,0,0.03); }
  .member-avatar { width: 36px; height: 36px; border-radius: 50%; background: #1976d2; color: white; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; flex-shrink: 0; }
  .inv-icon { color: #9e9e9e; }
  .member-info { flex: 1; display: flex; flex-direction: column; }
  .member-name { font-size: 14px; font-weight: 500; }
  .member-email { font-size: 12px; color: #9e9e9e; }
  .member-role { font-size: 12px; color: #616161; padding: 2px 8px; background: #f5f5f5; border-radius: 12px; }
</style>
  `
})
export class TeamSettingsComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/team`;

  readonly members = signal<TeamMember[]>([]);
  readonly invitations = signal<PendingInvitation[]>([]);
  readonly sending = signal(false);
  readonly inviteSent = signal(false);

  readonly form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    role: new FormControl('MEMBER', Validators.required)
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.http.get<TeamMember[]>(this.api).subscribe(m => this.members.set(m));
    this.http.get<PendingInvitation[]>(`${this.api}/invitations`).subscribe(i => this.invitations.set(i));
  }

  sendInvite(): void {
    if (this.form.invalid) return;
    this.sending.set(true);
    this.http.post(`${this.api}/invite`, this.form.value).subscribe({
      next: () => {
        this.sending.set(false);
        this.inviteSent.set(true);
        this.form.reset({ role: 'MEMBER' });
        setTimeout(() => this.inviteSent.set(false), 3000);
        this.load();
      },
      error: () => this.sending.set(false)
    });
  }

  removeMember(userId: string): void {
    this.http.delete(`${this.api}/members/${userId}`).subscribe(() => this.load());
  }

  cancelInvitation(id: string): void {
    this.http.delete(`${this.api}/invitations/${id}`).subscribe(() => this.load());
  }

  initials(m: TeamMember): string {
    return ((m.firstName?.[0] ?? '') + (m.lastName?.[0] ?? '')).toUpperCase() || m.email[0].toUpperCase();
  }
}
