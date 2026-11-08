import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { SubscriptionsService, PlanResponse, SubscriptionResponse } from '../subscriptions.service';

@Component({
  selector: 'app-subscription-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatBadgeModule,
  ],
  templateUrl: './subscription-page.component.html',
  styleUrl: './subscription-page.component.scss',
})
export class SubscriptionPageComponent implements OnInit {
  private svc = inject(SubscriptionsService);
  private snack = inject(MatSnackBar);

  plans = signal<PlanResponse[]>([]);
  currentSub = signal<SubscriptionResponse | null>(null);
  yearlyBilling = signal(false);
  loading = signal(true);
  upgrading = signal<string | null>(null);

  currentPlanSlug = computed(() => this.currentSub()?.plan?.slug ?? 'free');

  ngOnInit() {
    this.svc.getPlans().subscribe((p) => this.plans.set(p));
    this.svc.getMySubscription().subscribe({
      next: (s) => { this.currentSub.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  toggleBilling(yearly: boolean) {
    this.yearlyBilling.set(yearly);
  }

  getPrice(plan: PlanResponse): string {
    if (plan.priceMonthly === null) return 'Sur devis';
    const price = this.yearlyBilling() ? plan.priceYearly : plan.priceMonthly;
    if (!price) return 'Gratuit';
    return `${price} $`;
  }

  getPeriodLabel(): string {
    return this.yearlyBilling() ? '/ an' : '/ mois';
  }

  isCurrentPlan(plan: PlanResponse): boolean {
    return plan.slug === this.currentPlanSlug();
  }

  formatLimit(value: number): string {
    return value === -1 ? 'Illimité' : value.toString();
  }

  selectPlan(plan: PlanResponse) {
    if (this.isCurrentPlan(plan)) return;
    this.upgrading.set(plan.slug);
    this.svc.upgrade({
      planSlug: plan.slug,
      billingCycle: this.yearlyBilling() ? 'YEARLY' : 'MONTHLY',
    }).subscribe({
      next: (sub) => {
        this.currentSub.set(sub);
        this.upgrading.set(null);
        this.snack.open(`Plan ${plan.name} activé !`, 'Fermer', { duration: 3000 });
      },
      error: () => {
        this.upgrading.set(null);
        this.snack.open('Erreur lors du changement de plan', 'Fermer', { duration: 3000 });
      },
    });
  }
}
