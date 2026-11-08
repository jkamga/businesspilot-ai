import { Component, signal, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'bp-ai-settings',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, TranslateModule],
  template: `
    <div class="settings-page">
      <div class="page-header">
        <h1>{{ 'settings.ai.title' | translate }}</h1>
        <span class="subtitle">{{ 'settings.ai.subtitle' | translate }}</span>
      </div>

      <div class="card mat-elevation-z1">
        <h3>{{ 'settings.ai.system_prompt_label' | translate }}</h3>
        <p class="hint">{{ 'settings.ai.system_prompt_hint' | translate }}</p>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>{{ 'settings.ai.system_prompt_label' | translate }}</mat-label>
          <textarea matInput [(ngModel)]="systemPrompt" rows="8"
            [placeholder]="'settings.ai.system_prompt_placeholder' | translate"
            maxlength="2000"></textarea>
          <mat-hint align="end">{{ systemPrompt().length }}/2000</mat-hint>
        </mat-form-field>

        <div class="actions">
          <button mat-flat-button color="primary" (click)="save()" [disabled]="saving()">
            {{ saving() ? ('common.loading' | translate) : ('common.save' | translate) }}
          </button>
        </div>
      </div>
    </div>

    <style>
      .settings-page { padding: 24px; max-width: 800px; margin: 0 auto; }
      .page-header { margin-bottom: 28px; }
      .page-header h1 { margin: 0 0 4px; }
      .subtitle { color: #757575; font-size: 14px; }
      .card { padding: 24px; border-radius: 12px; margin-bottom: 20px; }
      .card h3 { margin: 0 0 8px; font-size: 15px; font-weight: 600; }
      .hint { color: #757575; font-size: 13px; margin: 0 0 16px; }
      .full-width { width: 100%; }
      .actions { margin-top: 16px; display: flex; justify-content: flex-end; }
    </style>
  `
})
export class AiSettingsComponent implements OnInit {
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar);

  systemPrompt = signal('');
  saving = signal(false);

  ngOnInit() {
    this.http.get<{ systemPrompt: string }>('/api/settings/ai').subscribe({
      next: s => this.systemPrompt.set(s.systemPrompt ?? '')
    });
  }

  save() {
    this.saving.set(true);
    this.http.put('/api/settings/ai', { systemPrompt: this.systemPrompt() }).subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open('Paramètres IA sauvegardés', '', { duration: 2000 });
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Erreur lors de la sauvegarde', '', { duration: 3000 });
      }
    });
  }
}
