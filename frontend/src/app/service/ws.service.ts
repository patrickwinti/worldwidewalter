import { Injectable, OnDestroy } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { AppConfigService } from './app-config.service';

@Injectable({
  providedIn: 'root'
})
export class WsService implements OnDestroy {

  private client: Client | null = null;

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
      }
    });
    this.client.activate();
  }

  disconnect(): void {
    if (this.client?.active) {
      this.client.deactivate();
    }
    this.client = null;
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
