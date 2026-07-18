import { Injectable, OnDestroy } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { AppConfigService } from './app-config.service';

@Injectable({
  providedIn: 'root'
})
export class WsService implements OnDestroy {

  private client: Client | null = null;
  // Desired subscriptions, kept so they can be (re)established on every (re)connect.
  private subscriptions = new Map<string, (body: unknown) => void>();

  constructor(private appConfigService: AppConfigService) {}

  connect(gameId: string, playerId: string): void {
    this.disconnect();
    this.client = new Client({
      brokerURL: this.appConfigService.getWsUrl(),
      reconnectDelay: 5000,
      onConnect: () => {
        this.client?.publish({
          destination: '/app/presence/register',
          headers: { gameId, playerId }
        });
        this.subscriptions.forEach((callback, topic) => this.activateSubscription(topic, callback));
      }
    });
    this.client.activate();
  }

  /**
   * Subscribes to a STOMP topic. The subscription is remembered and re-established
   * automatically after a reconnect. Parsed JSON message bodies are passed to the callback.
   */
  subscribe<T>(topic: string, callback: (body: T) => void): void {
    this.subscriptions.set(topic, callback as (body: unknown) => void);
    this.activateSubscription(topic, callback as (body: unknown) => void);
  }

  unsubscribe(topic: string): void {
    this.subscriptions.delete(topic);
  }

  private activateSubscription(topic: string, callback: (body: unknown) => void): void {
    if (this.client?.connected) {
      this.client.subscribe(topic, message => callback(JSON.parse(message.body)));
    }
  }

  disconnect(): void {
    this.subscriptions.clear();
    if (this.client?.active) {
      this.client.deactivate();
    }
    this.client = null;
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
