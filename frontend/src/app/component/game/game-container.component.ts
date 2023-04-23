import { Component, OnInit } from '@angular/core';
import { GameState } from "../../model/game-state";
import { StateService } from "../../service/state.service";
import { Observable, switchMap } from "rxjs";
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
    this.subscribeToGameState();
    this.getRoundObservable();
  }

  private subscribeToGameState() {
    this.stateService.getStateObservable()
      .subscribe(next => {
        this.gameState = next;
      });
  }

  private getRoundObservable(): void {
    this.round$ = this.gameService.enterRound(this.stateService.getGameId()).pipe(
      switchMap(() => {
        return this.gameService.getRound(this.stateService.getGameId());
      })
    )
  }

  isEnteringOrEnteredRound(): boolean {
    return this.stateService.isInRound() || this.stateService.isEnteringRound();
  }
}
