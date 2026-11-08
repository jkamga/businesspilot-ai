import { Routes } from '@angular/router';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'ai',
    pathMatch: 'full'
  },
  {
    path: 'ai',
    loadComponent: () => import('./ai-settings/ai-settings.component').then(m => m.AiSettingsComponent)
  },
  {
    path: 'widget',
    loadComponent: () => import('./widget-settings/widget-settings.component').then(m => m.WidgetSettingsComponent)
  },
  {
    path: 'team',
    loadComponent: () => import('./team-settings/team-settings.component').then(m => m.TeamSettingsComponent)
  },
  {
    path: 'webhooks',
    loadComponent: () => import('./webhook-settings/webhook-settings.component').then(m => m.WebhookSettingsComponent)
  }
];
