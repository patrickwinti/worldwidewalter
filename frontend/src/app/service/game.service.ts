import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { AppConfigService } from "./app-config.service";
import { GameDto } from "../dto/game-dto";
import { PlayerJoinRequestDto } from "../dto/player-join-request-dto";
import { PlayerDto } from "../dto/player-dto";
import { RoundDto } from "../dto/round-dto";
import { PropositionSubmissionDto } from "../dto/proposition-submission-dto";

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

  getRound(gameId: string): Observable<RoundDto> {
    return this.http.get<RoundDto>(this.appConfigService.getBaseUrl() + '/api/games/' + gameId + '/rounds');
  }

  submitProposition(roundId: string, proposition: PropositionSubmissionDto): Observable<void> {
    return this.http.post<void>(this.appConfigService.getBaseUrl() + '/api/rounds/' + roundId + '/proposition',
      proposition);
  }
}
