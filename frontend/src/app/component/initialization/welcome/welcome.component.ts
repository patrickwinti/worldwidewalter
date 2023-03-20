import { ChangeDetectionStrategy, Component, EventEmitter, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { GameDto } from "../../../dto/GameDto";
import { AppState } from "../../../app-state";

@Component({
  selector: 'www-welcome',
  templateUrl: './welcome.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WelcomeComponent {
  @Output() newGameEmitter = new EventEmitter<GameDto>()
  @Output() appStateEmitter = new EventEmitter<AppState>()

  constructor(private gameService: GameService) {
  }

  /**
   * Requests new game from backend and emits new game to parent container
   */
  async requestNewGame() {
    let newGame = await this.gameService.requestNewGame().then(
      (value) => value,
      () => undefined
    );
    if (newGame !== undefined) {
      this.newGameEmitter.emit(newGame);
      this.appStateEmitter.emit(AppState.JOIN_GAME);
    }
  }

  goToJoinPage() {
    this.appStateEmitter.emit(AppState.JOIN_GAME);
  }
}
