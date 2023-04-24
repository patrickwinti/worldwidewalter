import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { GameState } from "../model/game-state";

@Injectable({
  providedIn: 'root'
})
export class StateService {
  private gameState = new BehaviorSubject<GameState>(GameState.INITIALIZATION);
  private gameId: string;
  private playerId: string;

  getGameId(): string {
    return this.gameId ?? '';
  }

  setGameId(value: string) {
    this.gameId = value;
  }

  getPlayerId(): string {
    return this.playerId ?? '';
  }

  setPlayerId(value: string) {
    this.playerId = value;
  }

  getCurrentState(): GameState {
    return this.gameState.getValue();
  }

  getStateObservable(): Observable<GameState> {
    return this.gameState.asObservable();
  }

  setState(state: GameState): void {
    this.gameState.next(state);
  }

  leaveGame() {
    this.setState(GameState.INITIALIZATION);
  }

  isEnteringRound(): boolean {
    return GameState.ENTERING_ROUND === this.getCurrentState();
  }

  isInRound(): boolean {
    return (
      [
        GameState.ENTER_PROPOSITION,
        GameState.SELECT_PROPOSITION
      ].includes(this.getCurrentState())
    )
  }

  isInitializing(): boolean {
    return this.getCurrentState() === GameState.INITIALIZATION;
  }
}
