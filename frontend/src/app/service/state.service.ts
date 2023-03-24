import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { GameState } from "../model/game-state";

@Injectable({
  providedIn: 'root'
})
export class StateService {
  private state = new BehaviorSubject<GameState>(GameState.WAITING_FOR_PLAYERS);
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
      case GameState.WAITING_FOR_PLAYERS:
        return GameState.WAITING_FOR_ALL_PROPOSITIONS;
      case GameState.WAITING_FOR_ALL_PROPOSITIONS:
        return GameState.WAITING_FOR_ALL_SELECTIONS;
      case GameState.WAITING_FOR_ALL_SELECTIONS:
        return GameState.WAITING_FOR_PLAYERS;
    }
  }
}
