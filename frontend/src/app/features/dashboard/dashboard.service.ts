import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ProspectStats { total: number; byStatus: Record<string, number>; }
export interface ConversationStats { total: number; open: number; resolved: number; }
export interface KnowledgeStats { total: number; active: number; draft: number; }
export interface RecentConversation { id: string; prospectName: string | null; prospectEmail: string | null; status: string; updatedAt: string; }
export interface RecentProspect { id: string; firstName: string | null; lastName: string | null; company: string | null; status: string; updatedAt: string; }

export interface DashboardData {
  prospects: ProspectStats;
  conversations: ConversationStats;
  knowledge: KnowledgeStats;
  recentConversations: RecentConversation[];
  recentProspects: RecentProspect[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  getStats(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${environment.apiUrl}/dashboard`);
  }
}
