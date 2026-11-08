import { Routes } from '@angular/router';

export const KNOWLEDGE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./knowledge-list/knowledge-list.component').then(m => m.KnowledgeListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./knowledge-form/knowledge-form.component').then(m => m.KnowledgeFormComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./knowledge-form/knowledge-form.component').then(m => m.KnowledgeFormComponent)
  }
];
