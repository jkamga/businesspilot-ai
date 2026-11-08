import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'conversations',
    canActivate: [authGuard],
    loadChildren: () => import('./features/conversations/conversations.routes').then(m => m.CONVERSATIONS_ROUTES)
  },
  {
    path: 'prospects',
    canActivate: [authGuard],
    loadChildren: () => import('./features/prospects/prospects.routes').then(m => m.PROSPECTS_ROUTES)
  },
  {
    path: 'knowledge',
    canActivate: [authGuard],
    loadChildren: () => import('./features/knowledge/knowledge.routes').then(m => m.KNOWLEDGE_ROUTES)
  },
  {
    path: 'subscriptions',
    canActivate: [authGuard],
    loadChildren: () => import('./features/subscriptions/subscriptions.routes').then(m => m.SUBSCRIPTIONS_ROUTES)
  },
  {
    path: 'settings',
    canActivate: [authGuard],
    loadChildren: () => import('./features/settings/settings.routes').then(m => m.SETTINGS_ROUTES)
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
