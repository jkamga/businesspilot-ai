import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type ProspectStatus = 'LEAD' | 'CONTACTED' | 'QUALIFIED' | 'PROPOSAL' | 'CLOSED_WON' | 'CLOSED_LOST';
export type InteractionType = 'EMAIL' | 'CALL' | 'MEETING' | 'NOTE' | 'OTHER';

export interface Prospect {
  id: string;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  phone: string | null;
  company: string | null;
  status: ProspectStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProspectPage {
  content: Prospect[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface Interaction {
  id: string;
  prospectId: string;
  type: InteractionType;
  content: string;
  createdAt: string;
}

export interface ProspectDetail {
  prospect: Prospect;
  interactions: Interaction[];
}

export interface ProspectRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  company?: string;
  notes?: string;
}

@Injectable({ providedIn: 'root' })
export class ProspectsService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/prospects`;

  list(page = 0, size = 20, status?: ProspectStatus, q?: string): Observable<ProspectPage> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    if (q) params = params.set('q', q);
    return this.http.get<ProspectPage>(this.api, { params });
  }

  getById(id: string): Observable<ProspectDetail> {
    return this.http.get<ProspectDetail>(`${this.api}/${id}`);
  }

  create(data: ProspectRequest): Observable<Prospect> {
    return this.http.post<Prospect>(this.api, data);
  }

  update(id: string, data: ProspectRequest): Observable<Prospect> {
    return this.http.put<Prospect>(`${this.api}/${id}`, data);
  }

  updateStatus(id: string, status: ProspectStatus): Observable<Prospect> {
    return this.http.patch<Prospect>(`${this.api}/${id}/status`, { status });
  }

  addInteraction(id: string, type: InteractionType, content: string): Observable<Interaction> {
    return this.http.post<Interaction>(`${this.api}/${id}/interactions`, { type, content });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
