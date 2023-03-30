import { Component, OnInit } from '@angular/core';
import { GameState } from "../../model/game-state";
import { StateService } from "../../service/state.service";
import { Observable } from "rxjs";
import { RoundDto } from "../../dto/round-dto";
import { GameService } from "../../service/game.service";

@Component({
  selector: 'www-game-container',
  templateUrl: './game-container.component.html'
})
export class GameContainerComponent implements OnInit {
  gameState: GameState;
  GameState = GameState;
  round$: Observable<RoundDto>;

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.round$ = this.gameService.getRound(this.stateService.getGameId());
    this.stateService.getStateObservable()
      .subscribe(next => {
        this.gameState = next;
      });
  }
}
