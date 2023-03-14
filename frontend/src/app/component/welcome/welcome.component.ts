import {ChangeDetectionStrategy, Component, EventEmitter, Output} from '@angular/core';
import {Session} from "../../model/session";
import {GameService} from "../../service/game.service";

@Component({
  selector: 'www-welcome',
  templateUrl: './welcome.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WelcomeComponent {
  @Output() newGameSession = new EventEmitter<Session>()

  constructor(private gameService: GameService) {
  }

  /**
   * Requests new game from backend and emits new game session object to parent container
   */
  async createGame() {
    await this.gameService.requestNewGame().then(
      (value) => this.newGameSession.emit(value),
      () => this.newGameSession.emit(undefined)
    );
  }
}
