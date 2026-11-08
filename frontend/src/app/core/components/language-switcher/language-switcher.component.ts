import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { I18nService } from '../../i18n/i18n.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'bp-lang-switcher',
  standalone: true,
  imports: [MatButtonModule, MatTooltipModule, TranslateModule],
  template: `
    <button mat-button
            [matTooltip]="(i18n.isFrench() ? 'lang.switch_to_en' : 'lang.switch_to_fr') | translate"
            (click)="i18n.toggle()"
            class="lang-btn">
      {{ i18n.isFrench() ? 'EN' : 'FR' }}
    </button>
  `,
  styles: [`
    .lang-btn {
      min-width: 40px;
      font-weight: 600;
      font-size: 12px;
      letter-spacing: 0.05em;
    }
  `]
})
export class LanguageSwitcherComponent {
  readonly i18n = inject(I18nService);
}
