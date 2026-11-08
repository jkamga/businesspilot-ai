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
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
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
    TranslateModule,
  ],
  templateUrl: './subscription-page.component.html',
  styleUrl: './subscription-page.component.scss',
})
export class SubscriptionPageComponent implements OnInit {
  private svc = inject(SubscriptionsService);
  private snack = inject(MatSnackBar);
  private translate = inject(TranslateService);
  private route = inject(ActivatedRoute);

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
    this.route.queryParamMap.subscribe((params) => {
      if (params.get('success') === 'true') {
        this.snack.open(
          this.translate.instant('subscriptions.payment_success'),
          this.translate.instant('common.close'),
          { duration: 5000, panelClass: ['snack-success'] }
        );
        this.svc.getMySubscription().subscribe((s) => this.currentSub.set(s));
      } else if (params.get('cancelled') === 'true') {
        this.snack.open(
          this.translate.instant('subscriptions.payment_cancelled'),
          this.translate.instant('common.close'),
          { duration: 4000 }
        );
      }
    });
  }

  toggleBilling(yearly: boolean) {
    this.yearlyBilling.set(yearly);
  }

  getPrice(plan: PlanResponse): string {
    if (plan.priceMonthly === null) return this.translate.instant('subscriptions.on_quote');
    const price = this.yearlyBilling() ? plan.priceYearly : plan.priceMonthly;
    if (!price) return this.translate.instant('subscriptions.free');
    return `${price} $`;
  }

  getPeriodLabel(): string {
    return this.yearlyBilling()
      ? this.translate.instant('subscriptions.per_year')
      : this.translate.instant('subscriptions.per_month');
  }

  isCurrentPlan(plan: PlanResponse): boolean {
    return plan.slug === this.currentPlanSlug();
  }

  formatLimit(value: number): string {
    return value === -1 ? this.translate.instant('subscriptions.unlimited') : value.toString();
  }

  selectPlan(plan: PlanResponse) {
    if (this.isCurrentPlan(plan)) return;

    const billingCycle = this.yearlyBilling() ? 'YEARLY' : 'MONTHLY';

    // Plan gratuit — upgrade direct sans paiement
    if (plan.priceMonthly === null || plan.priceMonthly === 0) {
      this.upgradeDirect(plan, billingCycle);
      return;
    }

    // Plan enterprise — contact commercial
    if (plan.slug === 'enterprise') {
      window.location.href = 'mailto:sales@businesspilot.ai';
      return;
    }

    // Plan payant — redirection Stripe Checkout
    this.upgrading.set(plan.slug);
    this.svc.createCheckoutSession({ planSlug: plan.slug, billingCycle }).subscribe({
      next: (resp) => {
        this.upgrading.set(null);
        this.snack.open(
          this.translate.instant('subscriptions.redirecting_to_stripe'),
          '',
          { duration: 2000 }
        );
        setTimeout(() => { window.location.href = resp.checkoutUrl; }, 1500);
      },
      error: () => {
        this.upgrading.set(null);
        this.snack.open(
          this.translate.instant('subscriptions.upgrade_error'),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
      },
    });
  }

  private upgradeDirect(plan: PlanResponse, billingCycle: 'MONTHLY' | 'YEARLY') {
    this.upgrading.set(plan.slug);
    this.svc.upgrade({ planSlug: plan.slug, billingCycle }).subscribe({
      next: (sub) => {
        this.currentSub.set(sub);
        this.upgrading.set(null);
        this.snack.open(
          this.translate.instant('subscriptions.plan_activated', { name: plan.name }),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
      },
      error: () => {
        this.upgrading.set(null);
        this.snack.open(
          this.translate.instant('subscriptions.upgrade_error'),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
      },
    });
  }
}
