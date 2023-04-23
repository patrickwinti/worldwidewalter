import { Component } from '@angular/core';
import { StateService } from "./service/state.service";
import { GameState } from "./model/game-state";

@Component({
  selector: 'www-root',
  templateUrl: './app.component.html',
})
export class AppComponent {
  title = 'www-ui';

  constructor(private stateService: StateService) {
  }

  startGame() {
    this.stateService.setState(GameState.ENTERING_ROUND);
  }

  gameIsInitializing(): boolean {
    return this.stateService.isInitializing();
  }
}
