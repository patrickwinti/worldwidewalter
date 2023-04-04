import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class AppConfigService {
  private baseUrl: string = environment.apiUrl;

  getBaseUrl(): string {
    return this.baseUrl;
  }
}
