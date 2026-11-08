import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { TranslateModule } from '@ngx-translate/core';
import { AuthStore } from '../auth/auth.store';
import { AuthService } from '../../features/auth/services/auth.service';
import { LanguageSwitcherComponent } from '../components/language-switcher/language-switcher.component';

@Component({
  selector: 'bp-shell',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatSidenavModule, MatListModule,
    MatIconModule, MatButtonModule, MatMenuModule, MatDividerModule,
    TranslateModule, LanguageSwitcherComponent
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  readonly authStore = inject(AuthStore);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly navOpen = signal(true);

  readonly navLinks = [
    { path: '/dashboard',      icon: 'dashboard',      key: 'nav.dashboard' },
    { path: '/prospects',      icon: 'people',          key: 'nav.prospects' },
    { path: '/conversations',  icon: 'chat',            key: 'nav.conversations' },
    { path: '/knowledge',      icon: 'library_books',   key: 'nav.knowledge' },
    { path: '/subscriptions',  icon: 'workspace_premium', key: 'nav.subscriptions' },
  ];

  logout(): void {
    this.authService.logout();
  }

  get userInitials(): string {
    const u = this.authStore.user();
    if (!u) return '?';
    return ((u.firstName?.[0] ?? '') + (u.lastName?.[0] ?? '')).toUpperCase() || u.email[0].toUpperCase();
  }
}
