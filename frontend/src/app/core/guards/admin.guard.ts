import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStore } from '../auth/auth.store';

export const adminGuard = () => {
  const store = inject(AuthStore);
  const router = inject(Router);

  if (store.isSuperAdmin()) {
    return true;
  }
  return router.createUrlTree(['/dashboard']);
};
