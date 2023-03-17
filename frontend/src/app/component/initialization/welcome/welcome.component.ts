import {ChangeDetectionStrategy, Component, EventEmitter, Output} from '@angular/core';
import {GameService} from "../../../service/game.service";
import {GameDto} from "../../../dto/game-dto";
import {InitializationState} from "../../../model/initialization-state";

@Component({
  selector: 'www-welcome',
  templateUrl: './welcome.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WelcomeComponent {
  @Output() newGameEmitter = new EventEmitter<GameDto>()
  @Output() initializationStateEmitter = new EventEmitter<InitializationState>()

  constructor(private gameService: GameService) {
  }

  /**
   * Requests new game from backend and emits new game to parent container
   */
  async requestNewGame() {
    let newGame = await this.gameService.requestNewGame().then(
      (value) => {return value},
      () => {return undefined}
    );
    if (newGame !== undefined) {
      this.newGameEmitter.emit(newGame);
      this.initializationStateEmitter.emit(InitializationState.JOIN_GAME);
    }
  }

  goToJoinPage() {
    this.initializationStateEmitter.emit(InitializationState.JOIN_GAME);
  }
}
