import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { PlayerJoinRequestDto } from "../../../dto/player-join-request-dto";
import { PlayerDto } from "../../../dto/player-dto";
import { InitializationState } from "../../../model/initialization-state";
import { firstValueFrom } from "rxjs";

@Component({
  selector: 'www-join',
  templateUrl: './join.component.html'
})
export class JoinComponent implements OnInit {
  @Input() presetGameId: string;
  @Output() playerEmitter = new EventEmitter<PlayerDto>();
  @Output() initializationStateEmitter = new EventEmitter<InitializationState>();
  gameIdIsReadOnly: boolean;
  gameId: string;
  playerName: string;

  constructor(private gameService: GameService) {
  }

  ngOnInit(): void {
    if (this.presetGameId !== undefined) {
      this.gameId = this.presetGameId;
      this.gameIdIsReadOnly = true;
    } else {
      this.gameId = '';
      this.gameIdIsReadOnly = false;
    }
  }

  async joinGame() {
    if (this.playerName != undefined && this.playerName != '') {
      const playerDto = await firstValueFrom(this.gameService.joinGame({
          playerName: this.playerName
        } as PlayerJoinRequestDto,
        this.gameId))
        .then(
          (value) => {
            this.playerEmitter.emit(value);
            this.initializationStateEmitter.emit(InitializationState.WAITING_ROOM);
          },
          () => undefined // TODO: error handling when joining the game. How do we handle errors in frontend? how to display?
        );
    }

    console.log('joining game: ' + this.gameId + 'with username: ' + this.playerName);
  }
}
