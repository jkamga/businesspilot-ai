import { Injectable, signal, computed } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type SupportedLang = 'fr' | 'en';

const LANG_KEY = 'bp_lang';

@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly _lang = signal<SupportedLang>(this.loadSaved());

  readonly lang = this._lang.asReadonly();
  readonly isFrench = computed(() => this._lang() === 'fr');

  constructor(private readonly translate: TranslateService) {
    translate.addLangs(['fr', 'en']);
    translate.setDefaultLang('fr');
    translate.use(this._lang());
  }

  setLang(lang: SupportedLang): void {
    this.translate.use(lang);
    this._lang.set(lang);
    try { localStorage.setItem(LANG_KEY, lang); } catch { /* noop */ }
  }

  toggle(): void {
    this.setLang(this._lang() === 'fr' ? 'en' : 'fr');
  }

  private loadSaved(): SupportedLang {
    try {
      const saved = localStorage.getItem(LANG_KEY);
      if (saved === 'en' || saved === 'fr') return saved;
      const browser = navigator.language.startsWith('fr') ? 'fr' : 'en';
      return browser;
    } catch {
      return 'fr';
    }
  }
}
