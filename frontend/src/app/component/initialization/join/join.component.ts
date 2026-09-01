import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { PlayerDto, PlayerJoinRequestDto } from "@api";
import { InitializationState } from "../../../model/initialization-state";
import { firstValueFrom } from "rxjs";
import { StateService } from "../../../service/state.service";
import { HttpErrorResponse, HttpStatusCode } from "@angular/common/http";
import { isNonEmptyString } from "../../../shared/util";
import { GAME_ID_LENGTH, MAX_INPUT_LENGTH } from "../../../shared/settings";
import { CookieService } from "../../../service/cookie.service";
import { WsService } from "../../../service/ws.service";

@Component({
  selector: 'www-join',
  templateUrl: './join.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
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
              private cd: ChangeDetectorRef,
              private cookieService: CookieService,
              private wsService: WsService) {
  }

  get gameId(): string {
    return this.stateService.getGameId();
  }

  get canJoinGame(): boolean {
    return isNonEmptyString(this.playerName) && isNonEmptyString(this.joinGameId) && this.joinGameId.length === GAME_ID_LENGTH;
  }

  ngOnInit(): void {
    this.playerName = this.stateService.getPlayerName();
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
    this.joinGameId = this.joinGameId.trim().toUpperCase();

    if (isNonEmptyString(this.playerName)) {
      await firstValueFrom(this.gameService.joinGame({
          playerName: this.playerName
        } as PlayerJoinRequestDto,
        this.joinGameId))
        .then(
          (player: PlayerDto) => {
            this.stateService.setPlayerId(player.id);
            this.stateService.setGameId(this.joinGameId);
            this.stateService.setPlayerName(player.playerName);
            this.cookieService.set('playerId', player.id);
            this.cookieService.set('gameId', this.joinGameId);
            this.cookieService.set('playerName', player.playerName);
            this.wsService.connect(this.joinGameId, player.id);
            this.initializationStateEmitter.emit(InitializationState.LOBBY);
            console.log('joining game: ' + this.joinGameId + 'with username: ' + this.playerName);
          },
          (error: HttpErrorResponse) => {
            this.error = true;
            if (error.status === HttpStatusCode.NotFound) {
              this.joinGameId = '';
              this.gameIdIsReadOnly = false;
              this.errorText = 'Spiel nicht gefunden.';
            } else {
              this.errorText = 'Unbekannter Fehler';
            }
            this.cd.markForCheck();
          }
        );
    }
  }

  returnToStart() {
    this.stateService.setGameId('');
    this.initializationStateEmitter.emit(InitializationState.WELCOME_PAGE);
  }

  getTextToBeCopied(): string {
    return window.location.origin + '?gameId=' + this.gameId;
  }

  copyLink(): void {
    navigator.clipboard.writeText(this.getTextToBeCopied());
  }

  protected readonly MAX_INPUT_LENGTH = MAX_INPUT_LENGTH;
  protected readonly GAME_ID_LENGTH = GAME_ID_LENGTH;
}
