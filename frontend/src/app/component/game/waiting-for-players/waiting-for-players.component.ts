import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { GameDto } from "../../../dto/game-dto";
import { Observable, Observer, Subscription } from "rxjs";
import { RoundDto } from "../../../dto/round-dto";
import { GameService } from "../../../service/game.service";
import { HttpErrorResponse } from "@angular/common/http";
import { StateService } from "../../../service/state.service";

@Component({
  selector: 'www-waiting-for-players',
  templateUrl: './waiting-for-players.component.html'
})
export class WaitingForPlayersComponent implements OnInit, OnDestroy {
  @Output() gameStartEmitter = new EventEmitter<GameDto>();
  private gameSubscription = new Subscription;
  private round$: Observable<RoundDto>;

  constructor(private gameService: GameService,
              private stateService: StateService) {
  }

  get gameId(): string {
    return this.stateService.getGameId();
  }

  ngOnInit(): void {
    this.round$ = this.gameService.getRound(this.gameId);
    this.startPollingForRound();
  };

  ngOnDestroy() {
    this.stopPolling();
  }

  stopPolling() {
    this.gameSubscription.unsubscribe();
  }

  restartPolling() {
    this.startPollingForRound();
  }

  private startPollingForRound() {
    this.gameSubscription = this.round$.subscribe({
      next: (nextRound) => {
        this.stateService.goToNextState();
        this.stateService.setRound(nextRound);
        console.log('round: ' + nextRound.id + ' prompt: ' + nextRound.prompt);
      },
      error: (err: HttpErrorResponse) => {
        console.log('error status: ' + err.status);
        console.log('error: ' + err.error);
        console.log(err);
      }
    } as Observer<RoundDto>)
  }
}
