import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { PlayerJoinRequestDto } from "../../../dto/player-join-request-dto";
import { PlayerDto } from "../../../dto/player-dto";
import { InitializationState } from "../../../model/initialization-state";

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
  userName: string;

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
    if (this.userName != undefined && this.userName != '') {
      const playerDto = await this.gameService.joinGame({
          playerName: this.userName
        } as PlayerJoinRequestDto,
        this.gameId)
        .then(
          (value) => {
            this.playerEmitter.emit(value);
            this.initializationStateEmitter.emit(InitializationState.WAITING_ROOM);
          },
          () => undefined // error joining the game. How do we handle errors in frontend? how to display?
        );
    }

    console.log('joining game: ' + this.gameId + 'with username: ' + this.userName);
  }
}
