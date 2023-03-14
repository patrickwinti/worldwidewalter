import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {firstValueFrom} from "rxjs";
import {Session} from "../model/session";
import {AppConfigService} from "./app-config.service";

@Injectable({
  providedIn: 'root'
})
export class GameService {

  constructor(private http: HttpClient,
              private appConfigService: AppConfigService) {
  }

  requestNewGame(): Promise<Session> {
    return firstValueFrom(this.http.post<Session>(this.appConfigService.getBaseUrl() + '/api/games', {}));
  }
}
