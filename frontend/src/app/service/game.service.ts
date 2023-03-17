import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {firstValueFrom} from "rxjs";
import {AppConfigService} from "./app-config.service";
import {GameDto} from "../dto/game-dto";
import {PlayerJoinRequestDto} from "../dto/player-join-request-dto";
import {PlayerDto} from "../dto/player-dto";

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

  joinGame(playerJoinRequestDto: PlayerJoinRequestDto, gameId: string, ): Promise<PlayerDto> {
    return firstValueFrom(this.http.post<PlayerDto>(this.appConfigService.getBaseUrl() + '/api/games/' + gameId + '/players', playerJoinRequestDto));
  }

  getGameStatus(gameId: string, ): Promise<GameDto> {
    return firstValueFrom(this.http.get<GameDto>(this.appConfigService.getBaseUrl() + '/api/games/' + gameId));
  }

}
