import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { ProspectsService } from '../prospects.service';

@Component({
  selector: 'bp-prospect-form',
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink, TranslateModule,
    MatButtonModule, MatFormFieldModule, MatInputModule, MatIconModule, MatProgressSpinnerModule
  ],
  templateUrl: './prospect-form.component.html'
})
export class ProspectFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProspectsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly form = this.fb.group({
    firstName: ['', Validators.maxLength(100)],
    lastName: ['', Validators.maxLength(100)],
    email: ['', [Validators.email, Validators.maxLength(255)]],
    phone: ['', Validators.maxLength(50)],
    company: ['', Validators.maxLength(255)],
    notes: ['']
  });

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly isEdit = signal(false);
  private prospectId?: string;

  ngOnInit(): void {
    this.prospectId = this.route.snapshot.paramMap.get('id') ?? undefined;
    if (this.prospectId) {
      this.isEdit.set(true);
      this.loading.set(true);
      this.service.getById(this.prospectId).subscribe({
        next: d => { this.form.patchValue(d.prospect as any); this.loading.set(false); },
        error: () => this.router.navigate(['/prospects'])
      });
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set('');
    const data = this.form.getRawValue() as any;
    const op = this.isEdit()
      ? this.service.update(this.prospectId!, data)
      : this.service.create(data);

    op.subscribe({
      next: p => this.router.navigate(['/prospects', p.id]),
      error: err => { this.error.set(err.error?.detail ?? 'Erreur'); this.saving.set(false); }
    });
  }
}
