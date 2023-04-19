import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { GameState } from "../model/game-state";

@Injectable({
  providedIn: 'root'
})
export class StateService {
  private state = new BehaviorSubject<GameState>(GameState.ENTERING_ROUND);
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

  goToNextState() {
    this.state.next(this.getNextState(this.state.getValue()));
  }

  getCurrentState(): GameState {
    return this.state.getValue();
  }

  getStateObservable(): Observable<GameState> {
    return this.state.asObservable();
  }

  private getNextState(currentState: GameState): GameState {
    switch (currentState) {
      case GameState.ENTERING_ROUND:
        return GameState.ENTER_PROPOSITION;
      case GameState.ENTER_PROPOSITION:
        return GameState.SELECT_PROPOSITION;
      case GameState.SELECT_PROPOSITION:
        return GameState.SHOW_RANKING;
      case GameState.SHOW_RANKING:
        return GameState.ENTERING_ROUND;
    }
  }
}
