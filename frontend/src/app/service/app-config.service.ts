import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AppConfigService {
  // private baseUrl: string = 'http://localhost:8080';
  private baseUrl: string = 'http://myfilebox.synology.me:8080';

  getBaseUrl(): string {
    return this.baseUrl;
  }
}
