import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatBadgeModule } from '@angular/material/badge';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService, AdminStats, AdminOrg } from '../admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    MatBadgeModule,
    TranslateModule,
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss',
})
export class AdminDashboardComponent implements OnInit {
  private svc = inject(AdminService);

  stats = signal<AdminStats | null>(null);
  orgs = signal<AdminOrg[]>([]);
  totalOrgs = signal(0);
  pageSize = signal(20);
  loading = signal(true);

  displayedColumns = ['name', 'email', 'countryCode', 'plan', 'status', 'createdAt'];

  get planEntries(): [string, number][] {
    const s = this.stats();
    if (!s) return [];
    return Object.entries(s.subscriptionsByPlan).sort((a, b) => b[1] - a[1]);
  }

  ngOnInit() {
    this.svc.getStats().subscribe((s) => this.stats.set(s));
    this.loadOrgs(0);
  }

  loadOrgs(page: number) {
    this.loading.set(true);
    this.svc.getOrganizations(page, this.pageSize()).subscribe({
      next: (res) => {
        this.orgs.set(res.content);
        this.totalOrgs.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onPage(event: PageEvent) {
    this.pageSize.set(event.pageSize);
    this.loadOrgs(event.pageIndex);
  }

  statusColor(status: string): string {
    return status === 'ACTIVE' ? 'primary' : 'warn';
  }

  planColor(slug: string): string {
    const colors: Record<string, string> = {
      free: '',
      starter: 'accent',
      professional: 'primary',
      enterprise: 'warn',
    };
    return colors[slug] ?? '';
  }
}
