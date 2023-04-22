import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { AppConfigService } from "./app-config.service";
import { GameDto } from "../dto/game-dto";
import { PlayerJoinRequestDto } from "../dto/player-join-request-dto";
import { PlayerDto } from "../dto/player-dto";
import { RoundDto } from "../dto/round-dto";
import { PropositionSubmissionDto } from "../dto/proposition-submission-dto";
import { PropositionSelectionDto } from "../dto/proposition-selection-dto";
import { ResultDto } from "../dto/result-dto";

@Injectable({
  providedIn: 'root'
})
export class GameService {

  private readonly BASE_URL = this.appConfigService.getBaseUrl();

  constructor(private http: HttpClient,
              private appConfigService: AppConfigService) {
  }

  requestNewGame(): Observable<GameDto> {
    return this.http.post<GameDto>(this.BASE_URL + '/games', null);
  }

  joinGame(playerJoinRequestDto: PlayerJoinRequestDto, gameId: string,): Observable<PlayerDto> {
    return this.http.post<PlayerDto>(this.BASE_URL + '/games/' + gameId + '/players', playerJoinRequestDto);
  }


  getGame(gameId: string): Observable<GameDto> {
    return this.http.get<GameDto>(this.BASE_URL + '/games/' + gameId);
  }

  enterRound(gameId: string): Observable<void> {
    return this.http.put<void>(this.BASE_URL + '/games/' + gameId + '/rounds', null);
  }

  getRound(gameId: string): Observable<RoundDto> {
    return this.http.get<RoundDto>(this.BASE_URL + '/games/' + gameId + '/rounds');
  }

  submitProposition(roundId: string, proposition: PropositionSubmissionDto): Observable<void> {
    return this.http.post<void>(this.BASE_URL + '/rounds/' + roundId + '/propositions',
      proposition);
  }

  getAllPropositions(roundId: string): Observable<PropositionSelectionDto> {
    return this.http.get<PropositionSelectionDto>(this.BASE_URL + '/rounds/' + roundId + '/propositions');
  }

  submitPropositionSelection(roundId: string, id: string): Observable<void> {
    return this.http.post<void>(this.BASE_URL + '/rounds/' + roundId + '/propositions/' + id, null);
  }

  getResults(gameId: string): Observable<ResultDto> {
    return this.http.get<ResultDto>(this.BASE_URL + '/games/' + gameId + '/results')
  }

}
