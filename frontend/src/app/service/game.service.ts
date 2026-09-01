import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from "@angular/common/http";
import { SKIP_LOADING } from "./http-polling.interceptor";
import { Observable, retry } from "rxjs";
import { AppConfigService } from "./app-config.service";
import { GameDto } from "../dto/game-dto";
import { PlayerJoinRequestDto } from "../dto/player-join-request-dto";
import { PlayerDto } from "../dto/player-dto";
import { RoundDto } from "../dto/round-dto";
import { PropositionSubmissionDto } from "../dto/proposition-submission-dto";
import { PropositionSelectionDto } from "../dto/proposition-selection-dto";
import { ResultDto } from "../dto/result-dto";
import { GameCreatedDto } from "../dto/game-created-dto";
import { LobbyDto } from "../dto/lobby-dto";
import { RoundStatusDto } from "../dto/round-status-dto";

/** A player's answer is theirs alone, so a hiccup on the way to the server is retried. */
const SUBMIT_RETRIES = 2;
const SUBMIT_RETRY_DELAY_MS = 500;

@Injectable({
  providedIn: 'root'
})
export class GameService {

  private readonly BASE_URL = this.appConfigService.getBaseUrl();

  constructor(private http: HttpClient,
              private appConfigService: AppConfigService) {
  }

  requestNewGame(playerName: string): Observable<GameCreatedDto> {
    return this.http.post<GameCreatedDto>(this.BASE_URL + '/games', { playerName });
  }

  startGame(gameId: string): Observable<void> {
    return this.http.post<void>(this.BASE_URL + '/games/' + gameId + '/start', null);
  }

  getLobby(gameId: string): Observable<LobbyDto> {
    return this.http.get<LobbyDto>(this.BASE_URL + '/games/' + gameId + '/lobby');
  }

  joinGame(playerJoinRequestDto: PlayerJoinRequestDto, gameId: string): Observable<PlayerDto> {
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
      proposition).pipe(retry({ count: SUBMIT_RETRIES, delay: SUBMIT_RETRY_DELAY_MS }));
  }

  getAllPropositions(roundId: string): Observable<PropositionSelectionDto> {
    return this.http.get<PropositionSelectionDto>(this.BASE_URL + '/rounds/' + roundId + '/propositions');
  }

  submitPropositionSelection(roundId: string, id: string): Observable<void> {
    return this.http.post<void>(this.BASE_URL + '/rounds/' + roundId + '/propositions/' + id, null)
      .pipe(retry({ count: SUBMIT_RETRIES, delay: SUBMIT_RETRY_DELAY_MS }));
  }

  getResultsForRound(roundId: string, skipLoading = false): Observable<ResultDto> {
    return this.http.get<ResultDto>(this.BASE_URL + '/rounds/' + roundId + '/results',
      { context: new HttpContext().set(SKIP_LOADING, skipLoading) })
  }

  getResults(gameId: string): Observable<ResultDto> {
    return this.http.get<ResultDto>(this.BASE_URL + '/games/' + gameId + '/results')
  }

  leaveGame(playerId: string, gameId: string): Observable<void> {
    return this.http.delete<void>(this.BASE_URL + '/games/' + gameId + '/players/' + playerId)
  }

  /**
   * Tells the backend that this client is going away (tab closed or reloaded). This only marks
   * the player as absent: they keep their seat and their points, so a reload can rejoin the
   * running game instead of losing it.
   */
  markAbsentAfterDestruction(playerId: string, gameId: string): boolean {
    return navigator.sendBeacon(this.BASE_URL + '/games/' + gameId + '/players/' + playerId + '/disconnect')
  }

  endGame(gameId: string): Observable<void> {
    return this.http.post<void>(this.BASE_URL + '/games/' + gameId + '/end', null);
  }

  restartGame(gameId: string): Observable<void> {
    return this.http.post<void>(this.BASE_URL + '/games/' + gameId + '/restart', null);
  }

  /**
   * What the current round is waiting for (phase, how many players are done, who is missing).
   * The same payload is pushed on the /topic/games/{gameId}/round WebSocket topic.
   */
  getRoundStatus(gameId: string): Observable<RoundStatusDto> {
    return this.http.get<RoundStatusDto>(this.BASE_URL + '/games/' + gameId + '/rounds/status',
      { context: new HttpContext().set(SKIP_LOADING, true) });
  }

  rejoinGame(gameId: string, playerId: string): Observable<PlayerDto> {
    return this.http.post<PlayerDto>(this.BASE_URL + '/games/' + gameId + '/players/' + playerId + '/rejoin', null);
  }
}
