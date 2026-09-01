import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { GameState } from "../../model/game-state";
import { StateService } from "../../service/state.service";
import { catchError, Observable, switchMap, tap, throwError } from "rxjs";
import { RoundDto } from "../../dto/round-dto";
import { GameService } from "../../service/game.service";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
  selector: 'www-game-container',
  templateUrl: './game-container.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
})
export class GameContainerComponent implements OnInit {
  gameState: GameState;
  GameState = GameState;
  round$: Observable<RoundDto>;
  httpErr: HttpErrorResponse | null = null;

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
        if(next == GameState.REQUEST_NEW_ROUND) {
          this.getRound();
        }
        this.gameState = next;
      });
  }

  private getRoundObservable(): void {
    this.round$ = this.gameService.enterRound(this.stateService.getGameId()).pipe(
      switchMap(() => {
        return this.gameService.getRound(this.stateService.getGameId()).pipe(
          tap(() => this.stateService.setState(GameState.ENTER_PROPOSITION))
        );
      }),
      catchError((err: HttpErrorResponse) => {
        this.httpErr = err;
        return throwError(() => new Error(err.message));
      })
    )
  }

  showRoundComponent(): boolean {
    return this.stateService.isInRound();
  }

  getRound() {
    this.getRoundObservable();
  }

  get playerName(): string {
    return this.stateService.getPlayerName();
  }

  get gameId(): string {
    return this.stateService.getGameId();
  }

  gameInfoAvailable(): boolean {
    return !(this.playerName === '' || this.gameId === '');
  }
}
