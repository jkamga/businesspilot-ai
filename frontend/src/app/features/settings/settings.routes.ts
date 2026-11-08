import { Routes } from '@angular/router';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./widget-settings/widget-settings.component').then(m => m.WidgetSettingsComponent)
  }
];
