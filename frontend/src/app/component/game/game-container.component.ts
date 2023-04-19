import { Component, OnInit } from '@angular/core';
import { GameState } from "../../model/game-state";
import { StateService } from "../../service/state.service";
import { firstValueFrom, Observable } from "rxjs";
import { RoundDto } from "../../dto/round-dto";
import { GameService } from "../../service/game.service";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
  selector: 'www-game-container',
  templateUrl: './game-container.component.html'
})
export class GameContainerComponent implements OnInit {
  gameState: GameState;
  GameState = GameState;
  round$: Observable<RoundDto>;
  enteredRound: boolean;

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.stateService.getStateObservable()
      .subscribe(next => {
        this.gameState = next;
        if (this.gameState == GameState.ENTERING_ROUND) {
          this.enterAndGetRound();
        }
      });
  }

  private enterAndGetRound() {
    firstValueFrom(this.gameService.enterRound(this.stateService.getGameId())).then(
      () => {
        this.enteredRound = true;
        this.getRound();
        this.stateService.goToNextState();
      },
      (err: HttpErrorResponse) => console.log('error: ' + err.status));
  }

  private getRound(): void {
    this.round$ = this.gameService.getRound(this.stateService.getGameId());
  }

  isInRoundComponent(): boolean {
    return (
      this.gameState === GameState.ENTERING_ROUND ||
      this.gameState === GameState.ENTER_PROPOSITION ||
      this.gameState === GameState.SELECT_PROPOSITION
    )
  }
}
