import { Injectable } from '@angular/core';
import { HttpContext } from "@angular/common/http";
import { SKIP_LOADING } from "./http-polling.interceptor";
import { Observable } from "rxjs";
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
  RoundDto
} from "@api";

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
 *   <li>{@code leaveGameAfterDestruction} using the Beacon API on page unload, which cannot
 *       go through {@code HttpClient}.</li>
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
    return this.roundApi.submitProposition(roundId, this.playerId, proposition);
  }

  getAllPropositions(roundId: string): Observable<PropositionSelectionDto> {
    return this.roundApi.getAllPropositionForRound(roundId, this.playerId);
  }

  submitPropositionSelection(roundId: string, id: string): Observable<void> {
    return this.roundApi.selectProposition(roundId, id, this.playerId);
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

  leaveGameAfterDestruction(playerId: string, gameId: string): boolean {
    return navigator.sendBeacon(
      this.appConfigService.getBaseUrl() + '/games/' + gameId + '/players/' + playerId);
  }

  rejoinGame(gameId: string, playerId: string): Observable<PlayerDto> {
    return this.gameApi.rejoinGame(gameId, playerId);
  }
}
