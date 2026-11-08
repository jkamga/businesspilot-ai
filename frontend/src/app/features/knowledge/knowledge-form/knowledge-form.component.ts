import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { KnowledgeService } from '../knowledge.service';

@Component({
  selector: 'bp-knowledge-form',
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink,
    MatButtonModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatIconModule, MatProgressSpinnerModule
  ],
  templateUrl: './knowledge-form.component.html'
})
export class KnowledgeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(KnowledgeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    content: ['', Validators.required],
    type: ['TEXT', Validators.required]
  });

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly isEdit = signal(false);
  private entryId?: string;

  ngOnInit(): void {
    this.entryId = this.route.snapshot.paramMap.get('id') ?? undefined;
    if (this.entryId) {
      this.isEdit.set(true);
      this.loading.set(true);
      this.service.getById(this.entryId).subscribe({
        next: e => { this.form.patchValue(e); this.loading.set(false); },
        error: () => this.router.navigate(['/knowledge'])
      });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set('');
    const data = this.form.getRawValue() as any;
    const op = this.isEdit()
      ? this.service.update(this.entryId!, data)
      : this.service.create(data);

    op.subscribe({
      next: () => this.router.navigate(['/knowledge']),
      error: err => {
        this.error.set(err.error?.detail ?? 'Erreur lors de la sauvegarde');
        this.saving.set(false);
      }
    });
  }
}
