import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { firstValueFrom } from "rxjs";
import { AppConfigService } from "./app-config.service";
import { GameDto } from "../dto/GameDto";

@Injectable({
  providedIn: 'root'
})
export class GameService {

  constructor(private http: HttpClient,
              private appConfigService: AppConfigService) {
  }

  requestNewGame(): Promise<GameDto> {
    return firstValueFrom(this.http.post<GameDto>(this.appConfigService.getBaseUrl() + '/api/games', {}));
  }
}
