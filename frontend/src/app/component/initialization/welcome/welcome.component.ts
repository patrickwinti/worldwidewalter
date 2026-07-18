import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { InitializationState } from "../../../model/initialization-state";
import { firstValueFrom } from "rxjs";
import { StateService } from "../../../service/state.service";
import { HttpErrorResponse } from "@angular/common/http";
import { isNonEmptyString } from "../../../shared/util";
import { MAX_INPUT_LENGTH } from "../../../shared/settings";
import { CookieService } from "../../../service/cookie.service";
import { WsService } from "../../../service/ws.service";

@Component({
  selector: 'www-welcome',
  templateUrl: './welcome.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class WelcomeComponent {
  @Output() initializationStateEmitter = new EventEmitter<InitializationState>()
  playerName = '';
  error = false;

  constructor(private gameService: GameService,
              private stateService: StateService,
              private cookieService: CookieService,
              private wsService: WsService,
              private cd: ChangeDetectorRef) {
  }

  get canCreateGame(): boolean {
    return isNonEmptyString(this.playerName);
  }

  /**
   * Creates a new game with the entered name as the host, opens the WebSocket connection and
   * moves to the lobby.
   */
  async requestNewGame() {
    const name = this.playerName.trim();
    if (!isNonEmptyString(name)) {
      return;
    }
    await firstValueFrom(this.gameService.requestNewGame(name)).then(
      (created) => {
        this.stateService.setGameId(created.gameId);
        this.stateService.setPlayerId(created.host.id);
        this.stateService.setPlayerName(created.host.playerName);
        this.cookieService.set('gameId', created.gameId);
        this.cookieService.set('playerId', created.host.id);
        this.cookieService.set('playerName', created.host.playerName);
        this.wsService.connect(created.gameId, created.host.id);
        this.initializationStateEmitter.emit(InitializationState.LOBBY);
      },
      (error: HttpErrorResponse) => {
        this.error = true;
        this.cd.markForCheck();
        console.log('error occurred: ' + error.message);
      }
    );
  }

  goToJoinPage() {
    // Carry the entered name over to the join screen so it is pre-filled there.
    this.stateService.setPlayerName(this.playerName.trim());
    this.initializationStateEmitter.emit(InitializationState.JOIN_GAME);
  }

  protected readonly MAX_INPUT_LENGTH = MAX_INPUT_LENGTH;
}
