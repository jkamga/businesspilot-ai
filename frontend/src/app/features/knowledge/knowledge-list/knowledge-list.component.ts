import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { DatePipe } from '@angular/common';
import { KnowledgeService, KnowledgeEntry, KnowledgePage, EntryStatus } from '../knowledge.service';

@Component({
  selector: 'bp-knowledge-list',
  standalone: true,
  imports: [
    RouterLink, DatePipe,
    MatTableModule, MatButtonModule, MatIconModule, MatChipsModule,
    MatPaginatorModule, MatTooltipModule, MatProgressSpinnerModule,
    MatSelectModule, MatFormFieldModule
  ],
  templateUrl: './knowledge-list.component.html'
})
export class KnowledgeListComponent implements OnInit {
  private readonly service = inject(KnowledgeService);

  readonly columns = ['title', 'type', 'status', 'wordCount', 'updatedAt', 'actions'];
  readonly page = signal<KnowledgePage | null>(null);
  readonly loading = signal(true);
  readonly statusFilter = signal<EntryStatus | undefined>(undefined);

  pageIndex = 0;
  pageSize = 20;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.service.list(this.pageIndex, this.pageSize, this.statusFilter()).subscribe({
      next: p => { this.page.set(p); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  onPage(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    this.load();
  }

  publish(entry: KnowledgeEntry): void {
    this.service.publish(entry.id).subscribe(() => this.load());
  }

  delete(entry: KnowledgeEntry): void {
    if (!confirm(`Supprimer "${entry.title}" ?`)) return;
    this.service.delete(entry.id).subscribe(() => this.load());
  }

  statusColor(status: EntryStatus): string {
    return { DRAFT: 'default', ACTIVE: 'primary', ARCHIVED: 'warn' }[status] ?? 'default';
  }
}
