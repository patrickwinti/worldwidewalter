import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {GameDto} from "../../dto/GameDto";
import {AppState} from "../../app-state";

@Component({
  selector: 'www-initialization-container',
  templateUrl: './initialization-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InitializationContainerComponent implements OnInit{
  state: AppState;
  presetGameId: string;
  private _game: GameDto;

  get game(): GameDto {
    return this._game;
  }

  get AppState() {
    return AppState;
  }

  ngOnInit(): void {
    this.state = AppState.WELCOME_PAGE;
  }

  setGame(gameDto: GameDto) {
    this._game = gameDto;
    this.presetGameId = this._game.id;
  }

  setState(appState: AppState) {
    this.state = appState;
  }
}
