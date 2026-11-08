import { Routes } from '@angular/router';

export const SUBSCRIPTIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./subscription-page/subscription-page.component').then(
        (m) => m.SubscriptionPageComponent
      ),
  },
];
