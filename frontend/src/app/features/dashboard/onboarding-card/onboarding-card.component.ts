import { Component, input, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardData } from '../dashboard.service';

const DISMISSED_KEY = 'bp_onboarding_dismissed';

@Component({
  selector: 'bp-onboarding-card',
  standalone: true,
  imports: [RouterLink, TranslateModule, MatIconModule, MatButtonModule],
  template: `
    @if (visible()) {
      <div class="onboarding-card mat-elevation-z1">
        <div class="card-header">
          <div class="header-text">
            <mat-icon class="rocket-icon">rocket_launch</mat-icon>
            <div>
              <h2>{{ 'onboarding.title' | translate }}</h2>
              <p>{{ 'onboarding.subtitle' | translate }}</p>
            </div>
          </div>
          <button mat-icon-button (click)="dismiss()" class="dismiss-btn"
                  [attr.aria-label]="'onboarding.dismiss' | translate">
            <mat-icon>close</mat-icon>
          </button>
        </div>

        <div class="steps">
          @for (step of steps(); track step.key) {
            <div class="step" [class.done]="step.done">
              <div class="step-icon-wrap" [class.done]="step.done">
                <mat-icon>{{ step.done ? 'check_circle' : step.icon }}</mat-icon>
              </div>
              <div class="step-body">
                <div class="step-title">{{ step.title | translate }}</div>
                <div class="step-desc">{{ step.desc | translate }}</div>
              </div>
              @if (!step.done) {
                <a mat-stroked-button color="primary" [routerLink]="step.link" class="step-cta">
                  {{ step.cta | translate }}
                </a>
              } @else {
                <span class="step-done-label">{{ 'onboarding.done' | translate }}</span>
              }
            </div>
          }
        </div>

        <div class="progress-bar">
          <div class="progress-fill" [style.width.%]="progressPct()"></div>
        </div>
        <p class="progress-label">{{ doneCount() }}/3 {{ 'onboarding.steps_done' | translate }}</p>
      </div>
    }
  `,
  styles: [`
    .onboarding-card {
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
      background: linear-gradient(135deg, #e3f2fd 0%, #fff 60%);
      border-left: 4px solid #1976d2;
    }
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 20px;
    }
    .header-text {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .rocket-icon {
      font-size: 32px;
      height: 32px;
      width: 32px;
      color: #1976d2;
    }
    .header-text h2 { margin: 0 0 4px; font-size: 18px; font-weight: 600; }
    .header-text p { margin: 0; color: #616161; font-size: 13px; }
    .dismiss-btn { color: #9e9e9e; }
    .steps { display: flex; flex-direction: column; gap: 12px; }
    .step {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 14px 16px;
      border-radius: 8px;
      background: rgba(255,255,255,0.8);
      border: 1px solid #e0e0e0;
      transition: opacity 0.2s;
    }
    .step.done { opacity: 0.65; }
    .step-icon-wrap {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #e3f2fd;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      color: #1976d2;
    }
    .step-icon-wrap.done { background: #e8f5e9; color: #2e7d32; }
    .step-body { flex: 1; }
    .step-title { font-size: 14px; font-weight: 600; }
    .step-desc { font-size: 12px; color: #757575; margin-top: 2px; }
    .step-cta { flex-shrink: 0; }
    .step-done-label { font-size: 12px; color: #2e7d32; font-weight: 500; flex-shrink: 0; }
    .progress-bar {
      height: 4px;
      background: #e0e0e0;
      border-radius: 2px;
      margin-top: 20px;
      overflow: hidden;
    }
    .progress-fill {
      height: 100%;
      background: #1976d2;
      border-radius: 2px;
      transition: width 0.4s ease;
    }
    .progress-label { font-size: 12px; color: #9e9e9e; margin: 6px 0 0; }
  `]
})
export class OnboardingCardComponent {
  readonly data = input.required<DashboardData>();

  private readonly dismissed = signal(this.loadDismissed());

  readonly steps = computed(() => {
    const d = this.data();
    return [
      {
        key: 'kb',
        icon: 'library_books',
        title: 'onboarding.step1_title',
        desc: 'onboarding.step1_desc',
        cta: 'onboarding.step1_cta',
        link: '/knowledge/new',
        done: d.knowledge.total > 0
      },
      {
        key: 'conversation',
        icon: 'chat',
        title: 'onboarding.step2_title',
        desc: 'onboarding.step2_desc',
        cta: 'onboarding.step2_cta',
        link: '/conversations',
        done: d.conversations.total > 0
      },
      {
        key: 'prospect',
        icon: 'person_add',
        title: 'onboarding.step3_title',
        desc: 'onboarding.step3_desc',
        cta: 'onboarding.step3_cta',
        link: '/prospects/new',
        done: d.prospects.total > 0
      }
    ];
  });

  readonly doneCount = computed(() => this.steps().filter(s => s.done).length);
  readonly progressPct = computed(() => Math.round((this.doneCount() / 3) * 100));
  readonly allDone = computed(() => this.doneCount() === 3);
  readonly visible = computed(() => !this.dismissed() && !this.allDone());

  dismiss(): void {
    try { localStorage.setItem(DISMISSED_KEY, '1'); } catch { /* ignore */ }
    this.dismissed.set(true);
  }

  private loadDismissed(): boolean {
    try { return localStorage.getItem(DISMISSED_KEY) === '1'; } catch { return false; }
  }
}
