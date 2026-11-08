import { Routes } from '@angular/router';

export const PROSPECTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./prospect-list/prospect-list.component').then(m => m.ProspectListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./prospect-form/prospect-form.component').then(m => m.ProspectFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./prospect-detail/prospect-detail.component').then(m => m.ProspectDetailComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./prospect-form/prospect-form.component').then(m => m.ProspectFormComponent)
  }
];
