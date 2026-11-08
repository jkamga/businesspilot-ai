import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { DatePipe } from '@angular/common';
import { ProspectsService, ProspectDetail, ProspectStatus, InteractionType } from '../prospects.service';

@Component({
  selector: 'bp-prospect-detail',
  standalone: true,
  imports: [
    RouterLink, DatePipe, ReactiveFormsModule,
    MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatProgressSpinnerModule, MatChipsModule, MatDividerModule
  ],
  templateUrl: './prospect-detail.component.html'
})
export class ProspectDetailComponent implements OnInit {
  private readonly service = inject(ProspectsService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  readonly detail = signal<ProspectDetail | null>(null);
  readonly loading = signal(true);
  readonly savingStatus = signal(false);
  readonly addingInteraction = signal(false);

  readonly interactionForm = this.fb.group({
    type: ['NOTE' as InteractionType, Validators.required],
    content: ['', [Validators.required, Validators.maxLength(2000)]]
  });

  readonly statuses: ProspectStatus[] = ['LEAD', 'CONTACTED', 'QUALIFIED', 'PROPOSAL', 'CLOSED_WON', 'CLOSED_LOST'];
  readonly interactionTypes: InteractionType[] = ['EMAIL', 'CALL', 'MEETING', 'NOTE', 'OTHER'];

  private prospectId!: string;

  ngOnInit(): void {
    this.prospectId = this.route.snapshot.paramMap.get('id')!;
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.service.getById(this.prospectId).subscribe({
      next: d => { this.detail.set(d); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  changeStatus(status: ProspectStatus): void {
    this.savingStatus.set(true);
    this.service.updateStatus(this.prospectId, status).subscribe({
      next: p => { this.detail.update(d => d ? { ...d, prospect: p } : d); this.savingStatus.set(false); },
      error: () => this.savingStatus.set(false)
    });
  }

  submitInteraction(): void {
    if (this.interactionForm.invalid) return;
    this.addingInteraction.set(true);
    const { type, content } = this.interactionForm.getRawValue();
    this.service.addInteraction(this.prospectId, type as InteractionType, content!).subscribe({
      next: i => {
        this.detail.update(d => d ? { ...d, interactions: [i, ...d.interactions] } : d);
        this.interactionForm.reset({ type: 'NOTE' });
        this.addingInteraction.set(false);
      },
      error: () => this.addingInteraction.set(false)
    });
  }

  statusLabel(s: ProspectStatus): string {
    const labels: Record<ProspectStatus, string> = {
      LEAD: 'Lead', CONTACTED: 'Contacté', QUALIFIED: 'Qualifié',
      PROPOSAL: 'Proposition', CLOSED_WON: 'Gagné', CLOSED_LOST: 'Perdu'
    };
    return labels[s] ?? s;
  }

  typeIcon(t: InteractionType): string {
    return { EMAIL: 'email', CALL: 'call', MEETING: 'groups', NOTE: 'sticky_note_2', OTHER: 'more_horiz' }[t] ?? 'circle';
  }
}
