import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { concatMap, EMPTY, expand, Observable, timer } from "rxjs";
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

  requestNewGame(): Observable<GameDto> {
    return this.http.post<GameDto>(this.appConfigService.getBaseUrl() + '/api/games', {});
  }

  joinGame(playerJoinRequestDto: PlayerJoinRequestDto, gameId: string,): Observable<PlayerDto> {
    return this.http.post<PlayerDto>(this.appConfigService.getBaseUrl() + '/api/games/' + gameId + '/players', playerJoinRequestDto);
  }

  getGame(gameId: string): Observable<GameDto> {
    return this.http.get<GameDto>(this.appConfigService.getBaseUrl() + '/api/games/' + gameId);
  }

  getGameAsSoonAsInGivenState(gameId: string, gameState: GameState, retries: number, interval: number): Observable<GameDto> {
    let remainingRetries = retries;

    return this.getGame(gameId).pipe(
      expand((result) => {
        if (result.state !== gameState && remainingRetries > 0) {
          remainingRetries--;
          return timer(interval).pipe(
            concatMap(() => this.getGame(gameId)),
          )
        } else {
          return EMPTY
        }
      })
    );
  }

}
