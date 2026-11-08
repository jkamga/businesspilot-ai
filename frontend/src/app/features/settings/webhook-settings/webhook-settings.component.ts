import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { DatePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from '../../../../environments/environment';

interface WebhookEndpoint {
  id: string; url: string; secret: string; events: string[];
  active: boolean; createdAt: string;
}

const ALL_EVENTS = [
  'conversation.message.created',
  'prospect.created',
  'prospect.updated',
  'deal.stage_changed',
];

@Component({
  selector: 'bp-webhook-settings',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, TranslateModule,
    MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule,
    MatCheckboxModule, MatProgressSpinnerModule, MatDividerModule, MatSlideToggleModule],
  template: `
<div class="settings-page">
  <h2>{{ 'settings.webhooks.title' | translate }}</h2>
  <p class="subtitle">{{ 'settings.webhooks.subtitle' | translate }}</p>

  <!-- Add endpoint form -->
  <form [formGroup]="form" (ngSubmit)="addEndpoint()" class="add-form mat-elevation-z1">
    <mat-form-field appearance="outline" class="url-field">
      <mat-label>{{ 'settings.webhooks.url_label' | translate }}</mat-label>
      <input matInput formControlName="url" placeholder="https://example.com/webhook" autocomplete="off">
    </mat-form-field>

    <div class="events-label">{{ 'settings.webhooks.events_label' | translate }}</div>
    <div class="events-grid">
      @for (evt of allEvents; track evt) {
        <label class="event-check">
          <input type="checkbox" [value]="evt" (change)="toggleEvent(evt, $event)">
          <code>{{ evt }}</code>
        </label>
      }
    </div>

    <div class="form-footer">
      <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">
        @if (saving()) { <mat-spinner diameter="20"></mat-spinner> }
        @else { {{ 'settings.webhooks.add_btn' | translate }} }
      </button>
      @if (saved()) { <span class="success-msg">{{ 'settings.webhooks.saved' | translate }} ✓</span> }
    </div>
  </form>

  <mat-divider class="divider"/>

  <!-- Existing endpoints -->
  @if (endpoints().length === 0) {
    <p class="empty">{{ 'settings.webhooks.empty' | translate }}</p>
  } @else {
    <div class="endpoint-list">
      @for (ep of endpoints(); track ep.id) {
        <div class="endpoint-card mat-elevation-z1">
          <div class="ep-header">
            <code class="ep-url">{{ ep.url }}</code>
            <mat-slide-toggle [checked]="ep.active" (change)="toggleActive(ep, $event.checked)"></mat-slide-toggle>
            <button mat-icon-button color="warn" (click)="deleteEndpoint(ep.id)">
              <mat-icon>delete_outline</mat-icon>
            </button>
          </div>
          <div class="ep-meta">
            <span class="secret-row">
              <mat-icon class="meta-icon">key</mat-icon>
              <code class="secret">{{ showSecret[ep.id] ? ep.secret : '••••••••••••••••' }}</code>
              <button mat-icon-button (click)="toggleSecret(ep.id)" style="width:28px;height:28px;line-height:28px">
                <mat-icon style="font-size:16px">{{ showSecret[ep.id] ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
            </span>
          </div>
          @if (ep.events.length > 0) {
            <div class="ep-events">
              @for (e of ep.events; track e) { <code class="event-chip">{{ e }}</code> }
            </div>
          } @else {
            <div class="ep-events"><code class="event-chip all-events">{{ 'settings.webhooks.all_events' | translate }}</code></div>
          }
        </div>
      }
    </div>
  }
</div>

<style>
  .settings-page { max-width: 760px; padding: 24px; }
  .subtitle { color: #757575; font-size: 14px; margin-bottom: 24px; }
  .add-form { padding: 20px; border-radius: 12px; margin-bottom: 24px; }
  .url-field { width: 100%; }
  .events-label { font-size: 13px; color: #616161; margin: 8px 0 6px; }
  .events-grid { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
  .event-check { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 13px; }
  .event-check input { margin: 0; }
  .form-footer { display: flex; align-items: center; gap: 12px; }
  .success-msg { color: #2e7d32; font-size: 13px; }
  .divider { margin: 0 0 20px; }
  .empty { color: #9e9e9e; font-size: 13px; }
  .endpoint-list { display: flex; flex-direction: column; gap: 12px; }
  .endpoint-card { border-radius: 10px; padding: 14px 16px; }
  .ep-header { display: flex; align-items: center; gap: 10px; }
  .ep-url { flex: 1; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .ep-meta { display: flex; align-items: center; margin: 8px 0 4px; }
  .secret-row { display: flex; align-items: center; gap: 4px; }
  .meta-icon { font-size: 16px; color: #9e9e9e; }
  .secret { font-size: 12px; color: #616161; letter-spacing: 1px; }
  .ep-events { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
  .event-chip { font-size: 11px; padding: 2px 8px; border-radius: 10px; background: #e3f2fd; color: #1565c0; }
  .all-events { background: #f3e5f5; color: #7b1fa2; }
  code { font-family: monospace; }
</style>
  `
})
export class WebhookSettingsComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/settings/webhooks`;

  readonly allEvents = ALL_EVENTS;
  readonly endpoints = signal<WebhookEndpoint[]>([]);
  readonly saving = signal(false);
  readonly saved = signal(false);

  showSecret: Record<string, boolean> = {};
  selectedEvents: string[] = [];

  readonly form = new FormGroup({
    url: new FormControl('', [Validators.required, Validators.pattern(/^https?:\/\/.+/)])
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.http.get<WebhookEndpoint[]>(this.api).subscribe(e => this.endpoints.set(e));
  }

  toggleEvent(evt: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) { this.selectedEvents = [...this.selectedEvents, evt]; }
    else { this.selectedEvents = this.selectedEvents.filter(e => e !== evt); }
  }

  addEndpoint(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const payload = { url: this.form.value.url, events: this.selectedEvents, active: true };
    this.http.post<WebhookEndpoint>(this.api, payload).subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.set(true);
        this.form.reset();
        this.selectedEvents = [];
        setTimeout(() => this.saved.set(false), 3000);
        this.load();
      },
      error: () => this.saving.set(false)
    });
  }

  toggleActive(ep: WebhookEndpoint, active: boolean): void {
    this.http.put(`${this.api}/${ep.id}`, { url: ep.url, events: ep.events, active }).subscribe(() => this.load());
  }

  deleteEndpoint(id: string): void {
    this.http.delete(`${this.api}/${id}`).subscribe(() => this.load());
  }

  toggleSecret(id: string): void {
    this.showSecret[id] = !this.showSecret[id];
  }
}
