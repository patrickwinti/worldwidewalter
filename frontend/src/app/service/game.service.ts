import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { filter, firstValueFrom, Observable, retry, switchMap, take, timer } from "rxjs";
import { AppConfigService } from "./app-config.service";
import { GameDto } from "../dto/game-dto";
import { PlayerJoinRequestDto } from "../dto/player-join-request-dto";
import { PlayerDto } from "../dto/player-dto";
import { GameState } from "../model/game-state";

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

  joinGame(playerJoinRequestDto: PlayerJoinRequestDto, gameId: string,): Promise<PlayerDto> {
    return firstValueFrom(this.http.post<PlayerDto>(this.appConfigService.getBaseUrl() + '/api/games/' + gameId + '/players', playerJoinRequestDto));
  }

  getGame(gameId: string,): Promise<GameDto> {
    return firstValueFrom(this.http.get<GameDto>(this.appConfigService.getBaseUrl() + '/api/games/' + gameId));
  }

  getGameAsSoonAsInGivenState(gameId: string, gameState: GameState, retries: number, interval: number): Observable<GameDto> {
    return timer(1, interval).pipe(
      switchMap(() => this.getGame(gameId)),
      filter((game: GameDto) => {
        return game.state === gameState;
      }),
      retry(retries),
      take(1)
    );
  }

}
