import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { GameDto } from "../../../dto/game-dto";
import { Observable, Observer, Subscription } from "rxjs";
import { RoundDto } from "../../../dto/round-dto";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
  selector: 'www-waiting-page',
  templateUrl: './waiting-page.component.html'
})
export class WaitingPageComponent implements OnInit, OnDestroy {
  @Input() game: GameDto;
  @Output() gameStartEmitter = new EventEmitter<GameDto>();
  private gameSubscription = new Subscription;
  private round$: Observable<RoundDto>;

  constructor(private gameService: GameService) {
  }

  ngOnInit(): void {
    this.round$ = this.gameService.getRound(this.game.id);
    this.startPollingForRound();
  };

  private startPollingForRound() {
    this.gameSubscription = this.round$.subscribe({
      next: (val) => {
        console.log('round: ' + val.id + ' prompt: ' + val.prompt);
      },
      error: (err: HttpErrorResponse) => {
        console.log('error status: ' + err.status);
        console.log('error: ' + err.error);
        console.log(err);
      }
    } as Observer<RoundDto>)
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  stopPolling() {
    this.gameSubscription.unsubscribe();
  }

  restartPolling() {
    this.startPollingForRound();
  }
}
