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
  }
];
