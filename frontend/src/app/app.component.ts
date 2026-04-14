import { Component, HostListener, OnInit } from '@angular/core';
import { StateService } from "./service/state.service";
import { GameState } from "./model/game-state";
import { GameService } from "./service/game.service";
import { isNonEmptyString } from "./shared/util";

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
              private gameService: GameService) {
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

  ngOnInit(): void {
    let gameId = this.getParameterByName('gameId') ?? '';
    if (gameId !== '') {
      const id = gameId ?? '';
      this.joinWithId = true;
      this.stateService.setGameId(id);
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
