import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class AppConfigService {
  private baseUrl: string = environment.apiUrl;
  private wsUrl: string = environment.wsUrl;

  getBaseUrl(): string {
    return this.baseUrl;
  }

  getWsUrl(): string {
    return this.wsUrl;
  }
}
