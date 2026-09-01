import { Injectable } from '@angular/core';
import { HttpContext } from "@angular/common/http";
import { SKIP_LOADING } from "./http-polling.interceptor";
import { Observable, retry } from "rxjs";
import { AppConfigService } from "./app-config.service";
import { StateService } from "./state.service";
import {
  GameControllerService,
  GameCreatedDto,
  LobbyDto,
  PlayerDto,
  PlayerJoinRequestDto,
  PropositionSelectionDto,
  PropositionSubmissionDto,
  ResultDto,
  RoundControllerService,
  RoundDto,
  RoundStatusDto
} from "@api";

/** A player's answer is theirs alone, so a hiccup on the way to the server is retried. */
const SUBMIT_RETRIES = 2;
const SUBMIT_RETRY_DELAY_MS = 500;

/**
 * Thin facade over the OpenAPI-generated {@link GameControllerService} /
 * {@link RoundControllerService} (generated from {@code ../openapi.json}).
 *
 * It keeps the call sites in the components unchanged, injects the current player id for the
 * {@code X-PLAYER-ID} header parameter, and preserves the two behaviours the generated code
 * does not know about:
 * <ul>
 *   <li>the 425 "Too Early" polling retry &mdash; handled by {@code HttpPollingInterceptor}
 *       which still wraps every generated request;</li>
 *   <li>{@code markAbsentAfterDestruction} using the Beacon API on page unload, which cannot
 *       go through {@code HttpClient};</li>
 *   <li>the retry around a player's own submission, so a single failed request does not
 *       silently drop their answer.</li>
 * </ul>
 */
@Injectable({
  providedIn: 'root'
})
export class GameService {

  constructor(private gameApi: GameControllerService,
              private roundApi: RoundControllerService,
              private stateService: StateService,
              private appConfigService: AppConfigService) {
  }

  private get playerId(): string {
    return this.stateService.getPlayerId();
  }

  requestNewGame(playerName: string): Observable<GameCreatedDto> {
    return this.gameApi.createGame({ playerName });
  }

  startGame(gameId: string): Observable<void> {
    return this.gameApi.startGame(gameId, this.playerId);
  }

  getLobby(gameId: string): Observable<LobbyDto> {
    return this.gameApi.getLobby(gameId);
  }

  joinGame(playerJoinRequestDto: PlayerJoinRequestDto, gameId: string): Observable<PlayerDto> {
    return this.gameApi.enterGame(gameId, playerJoinRequestDto);
  }

  enterRound(gameId: string): Observable<void> {
    return this.gameApi.enterRound(gameId, this.playerId);
  }

  getRound(gameId: string): Observable<RoundDto> {
    return this.gameApi.getRound(gameId, this.playerId);
  }

  submitProposition(roundId: string, proposition: PropositionSubmissionDto): Observable<void> {
    return this.roundApi.submitProposition(roundId, this.playerId, proposition)
      .pipe(retry({ count: SUBMIT_RETRIES, delay: SUBMIT_RETRY_DELAY_MS }));
  }

  getAllPropositions(roundId: string): Observable<PropositionSelectionDto> {
    return this.roundApi.getAllPropositionForRound(roundId, this.playerId);
  }

  submitPropositionSelection(roundId: string, id: string): Observable<void> {
    return this.roundApi.selectProposition(roundId, id, this.playerId)
      .pipe(retry({ count: SUBMIT_RETRIES, delay: SUBMIT_RETRY_DELAY_MS }));
  }

  getResultsForRound(roundId: string, skipLoading = false): Observable<ResultDto> {
    return this.roundApi.getRoundResults(roundId, this.playerId, 'body', false,
      { context: new HttpContext().set(SKIP_LOADING, skipLoading) });
  }

  getResults(gameId: string): Observable<ResultDto> {
    return this.gameApi.getGameResults(gameId);
  }

  leaveGame(playerId: string, gameId: string): Observable<void> {
    return this.gameApi.leaveGame(gameId, playerId);
  }

  /**
   * Tells the backend that this client is going away (tab closed or reloaded). This only marks
   * the player as absent: they keep their seat and their points, so a reload can rejoin the
   * running game instead of losing it.
   */
  markAbsentAfterDestruction(playerId: string, gameId: string): boolean {
    return navigator.sendBeacon(
      this.appConfigService.getBaseUrl() + '/games/' + gameId + '/players/' + playerId + '/disconnect');
  }

  endGame(gameId: string): Observable<void> {
    return this.gameApi.endGame(gameId, this.playerId);
  }

  restartGame(gameId: string): Observable<void> {
    return this.gameApi.restartGame(gameId, this.playerId);
  }

  /**
   * What the current round is waiting for (phase, how many players are done, who is missing).
   * The same payload is pushed on the /topic/games/{gameId}/round WebSocket topic.
   */
  getRoundStatus(gameId: string): Observable<RoundStatusDto> {
    return this.gameApi.getRoundStatus(gameId, 'body', false,
      { context: new HttpContext().set(SKIP_LOADING, true) });
  }

  rejoinGame(gameId: string, playerId: string): Observable<PlayerDto> {
    return this.gameApi.rejoinGame(gameId, playerId);
  }
}
