import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type ConversationStatus = 'OPEN' | 'RESOLVED' | 'ARCHIVED';
export type MessageRole = 'USER' | 'ASSISTANT' | 'AGENT';

export interface Conversation {
  id: string;
  prospectName: string | null;
  prospectEmail: string | null;
  status: ConversationStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ConversationPage {
  content: Conversation[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface Message {
  id: string;
  conversationId: string;
  role: MessageRole;
  content: string;
  feedback: 'POSITIVE' | 'NEGATIVE' | null;
  createdAt: string;
}

export interface ConversationDetail {
  conversation: Conversation;
  messages: Message[];
}

@Injectable({ providedIn: 'root' })
export class ConversationsService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/conversations`;

  list(page = 0, size = 20, status?: ConversationStatus): Observable<ConversationPage> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<ConversationPage>(this.api, { params });
  }

  getById(id: string): Observable<ConversationDetail> {
    return this.http.get<ConversationDetail>(`${this.api}/${id}`);
  }

  create(prospectName?: string, prospectEmail?: string): Observable<Conversation> {
    return this.http.post<Conversation>(this.api, { prospectName, prospectEmail });
  }

  sendMessage(conversationId: string, content: string): Observable<Message[]> {
    return this.http.post<Message[]>(`${this.api}/${conversationId}/messages`, { content });
  }

  resolve(id: string): Observable<Conversation> {
    return this.http.post<Conversation>(`${this.api}/${id}/resolve`, {});
  }

  addFeedback(conversationId: string, messageId: string, feedback: 'POSITIVE' | 'NEGATIVE' | null): Observable<Message> {
    return this.http.put<Message>(`${this.api}/${conversationId}/messages/${messageId}/feedback`, { feedback });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
