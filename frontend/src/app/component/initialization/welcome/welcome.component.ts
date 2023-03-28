import { ChangeDetectionStrategy, Component, EventEmitter, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { InitializationState } from "../../../model/initialization-state";
import { firstValueFrom } from "rxjs";
import { StateService } from "../../../service/state.service";

@Component({
  selector: 'www-welcome',
  templateUrl: './welcome.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WelcomeComponent {
  @Output() initializationStateEmitter = new EventEmitter<InitializationState>()

  constructor(private gameService: GameService,
              private stateService: StateService) {
  }

  /**
   * Requests new game from backend and emits new game to parent container
   */
  async requestNewGame() {
    let newGame = await firstValueFrom(this.gameService.requestNewGame()).then(
      (value) => value,
      () => undefined
    );
    if (newGame !== undefined) {
      this.stateService.setGameId(newGame.id);
      this.initializationStateEmitter.emit(InitializationState.JOIN_GAME);
    }
  }

  goToJoinPage() {
    this.initializationStateEmitter.emit(InitializationState.JOIN_GAME);
  }
}
