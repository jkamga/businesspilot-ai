import { Component, OnInit, inject, signal, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { DatePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ConversationsService, ConversationDetail, Message } from '../conversations.service';

@Component({
  selector: 'bp-conversation-detail',
  standalone: true,
  imports: [
    RouterLink, DatePipe, ReactiveFormsModule, TranslateModule,
    MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule,
    MatProgressSpinnerModule, MatChipsModule
  ],
  templateUrl: './conversation-detail.component.html'
})
export class ConversationDetailComponent implements OnInit, AfterViewChecked {
  private readonly service = inject(ConversationsService);
  private readonly route = inject(ActivatedRoute);

  @ViewChild('messagesEnd') messagesEnd!: ElementRef;

  readonly detail = signal<ConversationDetail | null>(null);
  readonly loading = signal(true);
  readonly sending = signal(false);
  readonly messageCtrl = new FormControl('', [Validators.required, Validators.maxLength(2000)]);

  private conversationId!: string;
  private shouldScroll = false;

  ngOnInit(): void {
    this.conversationId = this.route.snapshot.paramMap.get('id')!;
    this.loadDetail();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  private loadDetail(): void {
    this.loading.set(true);
    this.service.getById(this.conversationId).subscribe({
      next: d => { this.detail.set(d); this.loading.set(false); this.shouldScroll = true; },
      error: () => this.loading.set(false)
    });
  }

  sendMessage(): void {
    const content = this.messageCtrl.value?.trim();
    if (!content || this.sending()) return;
    this.sending.set(true);
    this.messageCtrl.disable();

    this.service.sendMessage(this.conversationId, content).subscribe({
      next: newMessages => {
        const d = this.detail();
        if (d) {
          this.detail.set({ ...d, messages: [...d.messages, ...newMessages] });
        }
        this.messageCtrl.reset();
        this.messageCtrl.enable();
        this.sending.set(false);
        this.shouldScroll = true;
      },
      error: () => {
        this.messageCtrl.enable();
        this.sending.set(false);
      }
    });
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom(): void {
    this.messagesEnd?.nativeElement.scrollIntoView({ behavior: 'smooth' });
  }

  isUser(msg: Message): boolean { return msg.role === 'USER'; }
}
