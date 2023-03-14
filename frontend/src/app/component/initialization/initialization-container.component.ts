import {ChangeDetectionStrategy, Component} from '@angular/core';
import {GameDto} from "../../dto/GameDto";

@Component({
  selector: 'www-initialization-container',
  templateUrl: './initialization-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InitializationContainerComponent {
  private _game: GameDto;

  setGame(gameDto: GameDto) {
    this._game = gameDto;
  }

  get game(): GameDto {
    return this._game;
  }
}
