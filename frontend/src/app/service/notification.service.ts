import { Injectable } from '@angular/core';
import { Observable, Subject } from "rxjs";

/**
 * What went wrong. The kind lets the view decide how to present it; the message is a plain
 * fallback text.
 */
export interface GameNotification {
  kind: 'proposition-not-submitted' | 'selection-not-submitted';
  message: string;
}

/**
 * Carries problems that must not pass silently but that do not stop the game either - most
 * importantly a submission that never reached the backend, where the player would otherwise
 * believe their answer counted.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly notifications = new Subject<GameNotification>();

  notify(notification: GameNotification): void {
    this.notifications.next(notification);
  }

  getNotifications(): Observable<GameNotification> {
    return this.notifications.asObservable();
  }
}
