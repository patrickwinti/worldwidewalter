import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { GameDto } from "../../../dto/game-dto";
import { Observable, Subscription } from "rxjs";
import { GameState } from "../../../model/game-state";

@Component({
  selector: 'www-waiting-page',
  templateUrl: './waiting-page.component.html'
})
export class WaitingPageComponent implements OnInit, OnDestroy {
  @Input() game: GameDto;
  @Output() gameStartEmitter = new EventEmitter<GameDto>();
  private gameSubscription = new Subscription;
  private game$: Observable<GameDto>;

  constructor(private gameService: GameService) {
  }

  ngOnInit(): void {
    this.game$ = this.gameService.getGameAsSoonAsInGivenState(this.game.id, GameState.READY, 3, 1000);
    this.startPollingGame();
  };

  private startPollingGame() {
    this.gameSubscription = this.game$.subscribe({
      next: (val) => {
        console.log('game: ' + val.id + ' state: ' + val.state);
      },
      complete: () => {
        console.log('completed');
      },
      error: () => {
        console.log('error');
      }
    })
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  stopPolling() {
    this.gameSubscription.unsubscribe();
  }

  restartPolling() {
    this.startPollingGame();
  }
}
