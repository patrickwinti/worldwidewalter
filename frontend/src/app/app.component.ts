import { Component, HostListener, OnInit } from '@angular/core';
import { StateService } from "./service/state.service";
import { GameState } from "./model/game-state";
import { GameService } from "./service/game.service";
import { CookieService } from "./service/cookie.service";
import { WsService } from "./service/ws.service";
import { isNonEmptyString } from "./shared/util";
import { firstValueFrom } from "rxjs";

@Component({
  selector: 'www-root',
  templateUrl: './app.component.html',
  standalone: false,
})
export class AppComponent implements OnInit {
  title = 'www-ui';
  joinWithId: boolean;
  resumeInLobby = false;
  ready = false;

  @HostListener('window:beforeunload')
  beforeUnloadHandler() {
    return false;
  }

  @HostListener('window:unload')
  unloadHandler() {
    this.markAbsent();
  }

  constructor(private stateService: StateService,
              private gameService: GameService,
              private cookieService: CookieService,
              private wsService: WsService) {
  }

  startGame() {
    this.stateService.setState(GameState.REQUEST_NEW_ROUND);
  }

  gameIsInitializing(): boolean {
    return this.stateService.isInitializing();
  }

  /**
   * Tells the backend that this client is going away. Deliberately not a "leave": leaving
   * removes the player and their points, which would make the reload below fail and silently
   * throw the player out of a running game. Marking them absent keeps their seat, and the
   * disconnect grace period stops them from blocking the round in the meantime.
   */
  private markAbsent() {
    if (isNonEmptyString(this.stateService.getPlayerId()) && isNonEmptyString(this.stateService.getGameId())) {
      this.gameService.markAbsentAfterDestruction(this.stateService.getPlayerId(), this.stateService.getGameId());
    }
  }

  async ngOnInit(): Promise<void> {
    const urlGameId = this.getParameterByName('gameId') ?? '';
    if (urlGameId !== '') {
      this.joinWithId = true;
      this.stateService.setGameId(urlGameId);
      this.ready = true;
      return;
    }

    const savedGameId = this.cookieService.get('gameId');
    const savedPlayerId = this.cookieService.get('playerId');
    const savedPlayerName = this.cookieService.get('playerName');

    if (isNonEmptyString(savedGameId) && isNonEmptyString(savedPlayerId)) {
      try {
        const player = await firstValueFrom(this.gameService.rejoinGame(savedGameId, savedPlayerId));
        this.stateService.setGameId(savedGameId);
        this.stateService.setPlayerId(player.id);
        this.stateService.setPlayerName(player.playerName ?? savedPlayerName);
        this.wsService.connect(savedGameId, player.id);

        // If the game has not been started yet, return to the lobby instead of the game.
        const lobby = await firstValueFrom(this.gameService.getLobby(savedGameId)).catch(() => null);
        if (lobby && !lobby.started) {
          this.resumeInLobby = true;
        } else {
          this.stateService.setState(GameState.REQUEST_NEW_ROUND);
        }
      } catch {
        this.cookieService.delete('gameId');
        this.cookieService.delete('playerId');
        this.cookieService.delete('playerName');
      }
    }
    this.ready = true;
  }

  getParameterByName(name: any) {
    let url = window.location.href;
    name = name.replace(/[[]]/g, "\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
      results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace('/+/g', " "));
  }
}
