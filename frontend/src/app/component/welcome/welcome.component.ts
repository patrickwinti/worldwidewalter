import {ChangeDetectionStrategy, Component, EventEmitter, Output} from '@angular/core';
import {GameService} from "../../service/game.service";
import {GameDto} from "../../dto/GameDto";

@Component({
  selector: 'www-welcome',
  templateUrl: './welcome.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WelcomeComponent {
  @Output() newGameEmitter = new EventEmitter<GameDto>()

  constructor(private gameService: GameService) {
  }

  /**
   * Requests new game from backend and emits new game to parent container
   */
  async requestNewGame() {
    await this.gameService.requestNewGame().then(
      (value) => this.newGameEmitter.emit(value),
      () => this.newGameEmitter.emit(undefined)
    );
  }
}
