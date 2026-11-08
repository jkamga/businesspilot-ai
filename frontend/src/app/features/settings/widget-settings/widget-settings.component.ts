import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';

interface WidgetSettings {
  widgetToken: string;
  orgName: string;
}

@Component({
  selector: 'bp-widget-settings',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, TranslateModule],
  template: `
    <div class="settings-page">
      <div class="page-header">
        <h1>{{ 'settings.widget.title' | translate }}</h1>
        <span class="subtitle">{{ 'settings.widget.subtitle' | translate }}</span>
      </div>

      @if (loading()) {
        <p>{{ 'common.loading' | translate }}</p>
      } @else if (settings()) {
        <div class="card mat-elevation-z1">
          <h3>{{ 'settings.widget.embed_code' | translate }}</h3>
          <p class="hint">{{ 'settings.widget.embed_hint' | translate }}</p>
          <div class="code-block">
            <pre>{{ embedCode() }}</pre>
            <button mat-icon-button (click)="copyCode()" class="copy-btn" title="Copier">
              <mat-icon>content_copy</mat-icon>
            </button>
          </div>
        </div>

        <div class="card mat-elevation-z1">
          <h3>{{ 'settings.widget.token_label' | translate }}</h3>
          <div class="token-row">
            <code class="token">{{ settings()!.widgetToken }}</code>
            <button mat-icon-button (click)="copyToken()" title="Copier le token">
              <mat-icon>content_copy</mat-icon>
            </button>
          </div>
        </div>
      }
    </div>

    <style>
      .settings-page { padding: 24px; max-width: 800px; margin: 0 auto; }
      .page-header { margin-bottom: 28px; }
      .page-header h1 { margin: 0 0 4px; }
      .subtitle { color: #757575; font-size: 14px; }
      .card { padding: 24px; border-radius: 12px; margin-bottom: 20px; }
      .card h3 { margin: 0 0 8px; font-size: 15px; font-weight: 600; }
      .hint { color: #757575; font-size: 13px; margin: 0 0 16px; }
      .code-block { position: relative; background: #f5f5f5; border-radius: 8px; padding: 16px; }
      .code-block pre { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; font-family: monospace; }
      .copy-btn { position: absolute; top: 8px; right: 8px; }
      .token-row { display: flex; align-items: center; gap: 8px; }
      .token { background: #f5f5f5; padding: 8px 12px; border-radius: 6px; font-size: 13px; word-break: break-all; }

      @media (prefers-color-scheme: dark) {
        .code-block, .token { background: #2a2a2a; }
      }
    </style>
  `
})
export class WidgetSettingsComponent implements OnInit {
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar);

  loading = signal(true);
  settings = signal<WidgetSettings | null>(null);

  ngOnInit() {
    this.http.get<WidgetSettings>('/api/settings/widget').subscribe({
      next: s => { this.settings.set(s); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  embedCode(): string {
    const s = this.settings();
    if (!s) return '';
    return `<script src="${window.location.origin}/widget.js" data-token="${s.widgetToken}"></script>`;
  }

  copyCode() {
    navigator.clipboard.writeText(this.embedCode()).then(() =>
      this.snackBar.open('Code copié !', '', { duration: 2000 })
    );
  }

  copyToken() {
    const s = this.settings();
    if (!s) return;
    navigator.clipboard.writeText(s.widgetToken).then(() =>
      this.snackBar.open('Token copié !', '', { duration: 2000 })
    );
  }
}
