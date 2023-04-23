import { Component, HostListener } from '@angular/core';
import { StateService } from "./service/state.service";
import { GameState } from "./model/game-state";
import { GameService } from "./service/game.service";

@Component({
  selector: 'www-root',
  templateUrl: './app.component.html',
})
export class AppComponent {
  title = 'www-ui';

  @HostListener('window:beforeunload', ['event'])
  beforeUnloadHandler() {
    this.leaveGame();
  }

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  startGame() {
    this.stateService.setState(GameState.ENTERING_ROUND);
  }

  gameIsInitializing(): boolean {
    return this.stateService.isInitializing();
  }

  private leaveGame() {
    if (this.stateService.getPlayerId() !== undefined && this.stateService.getGameId() !== undefined) {
      this.gameService.leaveGameAfterDestruction(this.stateService.getPlayerId(), this.stateService.getGameId());
    }
  }
}
