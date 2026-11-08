import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AdminStats {
  totalOrganizations: number;
  activeOrganizations: number;
  totalUsers: number;
  subscriptionsByPlan: Record<string, number>;
}

export interface AdminOrg {
  id: string;
  name: string;
  slug: string;
  email: string;
  countryCode: string;
  active: boolean;
  createdAt: string;
  planName: string;
  planSlug: string;
  subscriptionStatus: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private base = '/api/admin';

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.base}/stats`);
  }

  getOrganizations(page = 0, size = 20): Observable<PageResponse<AdminOrg>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminOrg>>(`${this.base}/organizations`, { params });
  }
}
