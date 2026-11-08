import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'bp-oauth2-success',
  standalone: true,
  imports: [MatProgressSpinnerModule],
  template: `
    <div style="display:flex;justify-content:center;align-items:center;height:100vh">
      <mat-spinner></mat-spinner>
    </div>
  `
})
export class OAuth2SuccessComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    const refresh = this.route.snapshot.queryParamMap.get('refresh');
    const onboarding = this.route.snapshot.queryParamMap.get('onboarding') === 'true';

    if (!token || !refresh) {
      this.router.navigate(['/auth/login'], { queryParams: { error: 'oauth2' } });
      return;
    }

    this.authService.handleOAuth2Callback(token, refresh);
    this.router.navigate([onboarding ? '/onboarding' : '/dashboard']);
  }
}
