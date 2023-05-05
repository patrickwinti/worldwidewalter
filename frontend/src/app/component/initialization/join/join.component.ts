import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { PlayerJoinRequestDto } from "../../../dto/player-join-request-dto";
import { InitializationState } from "../../../model/initialization-state";
import { firstValueFrom } from "rxjs";
import { StateService } from "../../../service/state.service";
import { PlayerDto } from "../../../dto/player-dto";
import { HttpErrorResponse, HttpStatusCode } from "@angular/common/http";
import { isNonEmptyString } from "../../../shared/util";

@Component({
  selector: 'www-join',
  templateUrl: './join.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JoinComponent implements OnInit {
  @Output() initializationStateEmitter = new EventEmitter<InitializationState>();
  gameIdIsReadOnly: boolean;
  playerName: string;
  joinGameId: string;
  error = false;
  errorText = '';
  isCopyButtonVisible: boolean;

  constructor(private gameService: GameService,
              private stateService: StateService,
              private cd: ChangeDetectorRef) {
  }

  get gameId(): string {
    return this.stateService.getGameId();
  }

  get canJoinGame(): boolean {
    return isNonEmptyString(this.playerName) && isNonEmptyString(this.joinGameId);
  }

  ngOnInit(): void {
    if (this.gameId !== '') {
      this.gameIdIsReadOnly = true;
      this.joinGameId = this.gameId;
      this.isCopyButtonVisible = true;

    } else {
      this.gameIdIsReadOnly = false;
      this.joinGameId = '';
      this.isCopyButtonVisible = false;
    }
  }

  async joinGame() {
    this.playerName = this.playerName.trim();
    this.joinGameId = this.joinGameId.trim();

    if (isNonEmptyString(this.playerName)) {
      await firstValueFrom(this.gameService.joinGame({
          playerName: this.playerName
        } as PlayerJoinRequestDto,
        this.joinGameId))
        .then(
          (player: PlayerDto) => {
            this.stateService.setPlayerId(player.id);
            this.stateService.setGameId(this.joinGameId);
            this.stateService.setPlayerName(player.playerName)
            this.initializationStateEmitter.emit(InitializationState.DONE);
            console.log('joining game: ' + this.joinGameId + 'with username: ' + this.playerName);
          },
          (error: HttpErrorResponse) => {
            this.error = true;
            if (error.status === HttpStatusCode.NotFound) {
              this.joinGameId = '';
              this.gameIdIsReadOnly = false;
              this.errorText = 'game not found';
            } else {
              this.errorText = 'unknown error. Try again';
            }
            this.cd.markForCheck();
          }
        );
    }
  }

  returnToStart() {
    this.initializationStateEmitter.emit(InitializationState.WELCOME_PAGE);
  }

  getTextToBeCopied(): string {
    return window.location.origin + '?gameId=' + this.gameId;
  }
}
