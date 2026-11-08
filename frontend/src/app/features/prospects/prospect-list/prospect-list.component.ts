import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { DatePipe } from '@angular/common';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ProspectsService, Prospect, ProspectPage, ProspectStatus } from '../prospects.service';

@Component({
  selector: 'bp-prospect-list',
  standalone: true,
  imports: [
    RouterLink, DatePipe, ReactiveFormsModule, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatChipsModule,
    MatPaginatorModule, MatTooltipModule, MatProgressSpinnerModule,
    MatSelectModule, MatFormFieldModule, MatInputModule
  ],
  templateUrl: './prospect-list.component.html'
})
export class ProspectListComponent implements OnInit {
  private readonly service = inject(ProspectsService);

  readonly columns = ['name', 'company', 'email', 'status', 'updatedAt', 'actions'];
  readonly page = signal<ProspectPage | null>(null);
  readonly loading = signal(true);
  readonly searchCtrl = new FormControl('');
  statusFilter: ProspectStatus | undefined;

  pageIndex = 0;
  pageSize = 20;

  ngOnInit(): void {
    this.load();
    this.searchCtrl.valueChanges.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => { this.pageIndex = 0; this.load(); });
  }

  load(): void {
    this.loading.set(true);
    this.service.list(this.pageIndex, this.pageSize, this.statusFilter, this.searchCtrl.value ?? undefined)
      .subscribe({ next: p => { this.page.set(p); this.loading.set(false); }, error: () => this.loading.set(false) });
  }

  onPage(e: PageEvent): void { this.pageIndex = e.pageIndex; this.pageSize = e.pageSize; this.load(); }

  delete(p: Prospect, event: Event): void {
    event.stopPropagation();
    const name = [p.firstName, p.lastName].filter(Boolean).join(' ') || p.email || 'ce prospect';
    if (!confirm(`Supprimer ${name} ?`)) return;
    this.service.delete(p.id).subscribe(() => this.load());
  }

  fullName(p: Prospect): string {
    return [p.firstName, p.lastName].filter(Boolean).join(' ') || '—';
  }

  statusColor(s: ProspectStatus): string {
    const map: Record<ProspectStatus, string> = {
      LEAD: 'default', CONTACTED: 'accent', QUALIFIED: 'primary',
      PROPOSAL: 'primary', CLOSED_WON: 'primary', CLOSED_LOST: 'warn'
    };
    return map[s] ?? 'default';
  }

  statusLabel(s: ProspectStatus): string {
    const labels: Record<ProspectStatus, string> = {
      LEAD: 'Lead', CONTACTED: 'Contacté', QUALIFIED: 'Qualifié',
      PROPOSAL: 'Proposition', CLOSED_WON: 'Gagné', CLOSED_LOST: 'Perdu'
    };
    return labels[s] ?? s;
  }
}
