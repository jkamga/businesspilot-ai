import { Injectable, signal, computed, inject } from '@angular/core';
import { I18nService } from '../i18n/i18n.service';

export interface AuthUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string;
  role: string;
  superAdmin?: boolean;
  preferredLocale?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly i18n = inject(I18nService);

  private readonly _token = signal<string | null>(
    localStorage.getItem('bp_token')
  );
  private readonly _user = signal<AuthUser | null>(
    this.loadUser()
  );

  readonly token = this._token.asReadonly();
  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => !!this._token());
  readonly isSuperAdmin = computed(() => this._user()?.superAdmin ?? false);

  setAuth(token: string, user: AuthUser): void {
    localStorage.setItem('bp_token', token);
    localStorage.setItem('bp_user', JSON.stringify(user));
    this._token.set(token);
    this._user.set(user);
    if (user.preferredLocale === 'en' || user.preferredLocale === 'fr') {
      this.i18n.setLang(user.preferredLocale);
    }
  }

  clearAuth(): void {
    localStorage.removeItem('bp_token');
    localStorage.removeItem('bp_user');
    this._token.set(null);
    this._user.set(null);
  }

  private loadUser(): AuthUser | null {
    try {
      const raw = localStorage.getItem('bp_user');
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
