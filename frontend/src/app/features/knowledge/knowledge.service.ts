import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type EntryType = 'TEXT' | 'URL' | 'FAQ';
export type EntryStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED';

export interface KnowledgeEntry {
  id: string;
  title: string;
  content: string;
  type: EntryType;
  status: EntryStatus;
  wordCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgePage {
  content: KnowledgeEntry[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface KnowledgeEntryRequest {
  title: string;
  content: string;
  type: EntryType;
}

@Injectable({ providedIn: 'root' })
export class KnowledgeService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/knowledge`;

  list(page = 0, size = 20, status?: EntryStatus, type?: EntryType): Observable<KnowledgePage> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    if (type) params = params.set('type', type);
    return this.http.get<KnowledgePage>(this.api, { params });
  }

  getById(id: string): Observable<KnowledgeEntry> {
    return this.http.get<KnowledgeEntry>(`${this.api}/${id}`);
  }

  create(data: KnowledgeEntryRequest): Observable<KnowledgeEntry> {
    return this.http.post<KnowledgeEntry>(this.api, data);
  }

  update(id: string, data: KnowledgeEntryRequest): Observable<KnowledgeEntry> {
    return this.http.put<KnowledgeEntry>(`${this.api}/${id}`, data);
  }

  publish(id: string): Observable<KnowledgeEntry> {
    return this.http.post<KnowledgeEntry>(`${this.api}/${id}/publish`, {});
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  search(q: string, limit = 5): Observable<KnowledgeEntry[]> {
    return this.http.get<KnowledgeEntry[]>(`${this.api}/search`, {
      params: { q, limit }
    });
  }
}
