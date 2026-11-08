import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PlanResponse {
  id: string;
  name: string;
  slug: string;
  priceMonthly: number | null;
  priceYearly: number | null;
  maxProspects: number;
  maxConversations: number;
  maxKnowledgeEntries: number;
  maxUsers: number;
  features: string[];
  sortOrder: number;
}

export interface SubscriptionResponse {
  id: string;
  plan: PlanResponse;
  status: 'ACTIVE' | 'PAST_DUE' | 'CANCELLED';
  billingCycle: 'MONTHLY' | 'YEARLY';
  currentPeriodStart: string;
  currentPeriodEnd: string | null;
  createdAt: string;
}

export interface UpgradeRequest {
  planSlug: string;
  billingCycle: 'MONTHLY' | 'YEARLY';
}

export interface CheckoutSessionResponse {
  checkoutUrl: string;
}

@Injectable({ providedIn: 'root' })
export class SubscriptionsService {
  private http = inject(HttpClient);
  private base = '/api';

  getPlans(): Observable<PlanResponse[]> {
    return this.http.get<PlanResponse[]>(`${this.base}/plans`);
  }

  getMySubscription(): Observable<SubscriptionResponse> {
    return this.http.get<SubscriptionResponse>(`${this.base}/subscriptions/my`);
  }

  upgrade(request: UpgradeRequest): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${this.base}/subscriptions/upgrade`, request);
  }

  createCheckoutSession(request: UpgradeRequest): Observable<CheckoutSessionResponse> {
    return this.http.post<CheckoutSessionResponse>(`${this.base}/subscriptions/checkout`, request);
  }
}
