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

  @HostListener('window:beforeunload')
  beforeUnloadHandler() {
    return false;
  }

  @HostListener('window:unload')
  unloadHandler() {
    this.leaveGame();
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

  private leaveGame() {
    if (isNonEmptyString(this.stateService.getPlayerId()) && isNonEmptyString(this.stateService.getGameId())) {
      this.gameService.leaveGameAfterDestruction(this.stateService.getPlayerId(), this.stateService.getGameId());
    }
  }

  async ngOnInit(): Promise<void> {
    const urlGameId = this.getParameterByName('gameId') ?? '';
    if (urlGameId !== '') {
      this.joinWithId = true;
      this.stateService.setGameId(urlGameId);
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
        this.stateService.setState(GameState.REQUEST_NEW_ROUND);
      } catch {
        this.cookieService.delete('gameId');
        this.cookieService.delete('playerId');
        this.cookieService.delete('playerName');
      }
    }
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
