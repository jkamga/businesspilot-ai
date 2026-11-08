import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
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
import { TranslateModule } from '@ngx-translate/core';
import { ConversationsService, Conversation, ConversationPage, ConversationStatus } from '../conversations.service';

@Component({
  selector: 'bp-conversation-list',
  standalone: true,
  imports: [
    RouterLink, DatePipe, TranslateModule,
    MatTableModule, MatButtonModule, MatIconModule, MatChipsModule,
    MatPaginatorModule, MatTooltipModule, MatProgressSpinnerModule,
    MatSelectModule, MatFormFieldModule
  ],
  templateUrl: './conversation-list.component.html'
})
export class ConversationListComponent implements OnInit {
  private readonly service = inject(ConversationsService);
  private readonly router = inject(Router);

  readonly columns = ['prospect', 'status', 'updatedAt', 'actions'];
  readonly page = signal<ConversationPage | null>(null);
  readonly loading = signal(true);
  readonly statusFilter = signal<ConversationStatus | undefined>(undefined);

  pageIndex = 0;
  pageSize = 20;

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.service.list(this.pageIndex, this.pageSize, this.statusFilter()).subscribe({
      next: p => { this.page.set(p); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  newConversation(): void {
    this.service.create().subscribe(c => this.router.navigate(['/conversations', c.id]));
  }

  onPage(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    this.load();
  }

  resolve(conv: Conversation, event: Event): void {
    event.stopPropagation();
    this.service.resolve(conv.id).subscribe(() => this.load());
  }

  delete(conv: Conversation, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Supprimer la conversation avec "${conv.prospectName ?? 'Anonyme'}" ?`)) return;
    this.service.delete(conv.id).subscribe(() => this.load());
  }

  statusColor(s: ConversationStatus): string {
    return { OPEN: 'primary', RESOLVED: 'accent', ARCHIVED: 'warn' }[s] ?? 'default';
  }
}
