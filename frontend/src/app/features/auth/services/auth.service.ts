import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { environment } from '../../../../environments/environment';

export interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  organizationName: string;
}

export interface LoginData {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string;
  role: string;
  onboardingRequired: boolean;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly api = environment.apiUrl;

  register(data: RegisterData): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/auth/register`, data).pipe(
      tap(res => this.storeAuth(res))
    );
  }

  login(data: LoginData): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/auth/login`, data).pipe(
      tap(res => this.storeAuth(res))
    );
  }

  refresh(): Observable<AuthResponse> {
    const token = localStorage.getItem('bp_refresh');
    return this.http.post<AuthResponse>(`${this.api}/auth/refresh`, { refreshToken: token }).pipe(
      tap(res => this.storeAuth(res))
    );
  }

  logout(): void {
    const token = localStorage.getItem('bp_refresh');
    if (token) {
      this.http.post(`${this.api}/auth/logout`, { refreshToken: token }).subscribe();
    }
    localStorage.removeItem('bp_refresh');
    this.authStore.clearAuth();
    this.router.navigate(['/auth/login']);
  }

  loginWithGoogle(): void {
    window.location.href = '/oauth2/authorization/google';
  }

  handleOAuth2Callback(accessToken: string, refreshToken: string): void {
    localStorage.setItem('bp_refresh', refreshToken);
    // Decode JWT to get user info
    const payload = JSON.parse(atob(accessToken.split('.')[1]));
    this.authStore.setAuth(accessToken, {
      id: payload.userId,
      email: payload.sub,
      firstName: '',
      lastName: '',
      organizationId: payload.orgId ?? '',
      role: payload.role ?? ''
    });
  }

  private storeAuth(res: AuthResponse): void {
    localStorage.setItem('bp_refresh', res.refreshToken);
    this.authStore.setAuth(res.accessToken, {
      id: res.userId,
      email: res.email,
      firstName: res.firstName,
      lastName: res.lastName,
      organizationId: res.organizationId ?? '',
      role: res.role
    });
  }
}
