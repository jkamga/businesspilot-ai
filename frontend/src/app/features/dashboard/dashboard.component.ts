import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DatePipe } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { DashboardService, DashboardData } from './dashboard.service';

@Component({
  selector: 'bp-dashboard',
  standalone: true,
  imports: [RouterLink, DatePipe, TranslateModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private readonly service = inject(DashboardService);

  readonly data = signal<DashboardData | null>(null);
  readonly loading = signal(true);

  readonly funnelSteps = [
    { key: 'LEAD' },
    { key: 'CONTACTED' },
    { key: 'QUALIFIED' },
    { key: 'PROPOSAL' },
    { key: 'CLOSED_WON' },
    { key: 'CLOSED_LOST' },
  ];

  ngOnInit(): void {
    this.service.getStats().subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  funnelMax(): number {
    const byStatus = this.data()?.prospects.byStatus ?? {};
    return Math.max(1, ...Object.values(byStatus));
  }

  funnelWidth(key: string): number {
    const val = this.data()?.prospects.byStatus[key] ?? 0;
    return Math.round((val / this.funnelMax()) * 100);
  }

  prospectName(p: { firstName: string | null; lastName: string | null }): string {
    return [p.firstName, p.lastName].filter(Boolean).join(' ') || '—';
  }

  convName(c: { prospectName: string | null; prospectEmail: string | null }): string {
    return c.prospectName ?? c.prospectEmail ?? 'Anonyme';
  }
}
