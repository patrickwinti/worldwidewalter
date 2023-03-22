import { ChangeDetectionStrategy, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { GameDto } from "../../dto/game-dto";
import { PlayerDto } from "../../dto/player-dto";
import { InitializationState } from "../../model/initialization-state";

@Component({
  selector: 'www-initialization-container',
  templateUrl: './initialization-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InitializationContainerComponent implements OnInit {
  @Output() startGameEmitter = new EventEmitter<GameDto>();
  state: InitializationState;
  presetGameId: string;
  game: GameDto;
  player: PlayerDto;
  InitializationState = InitializationState;

  ngOnInit(): void {
    this.state = InitializationState.WELCOME_PAGE;
  }

  setGame(gameDto: GameDto) {
    this.game = gameDto;
    this.presetGameId = this.game.id;
  }

  setState(state: InitializationState) {
    this.state = state;
  }

  setPlayer(playerDto: PlayerDto) {
    this.player = playerDto;
  }

  startGame(game: GameDto) {
    this.startGameEmitter.emit(game);
  }
}
