import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { StateService } from "../../service/state.service";
import { LoadingService } from "../../service/loading.service";
import { GameService } from "../../service/game.service";
import { Subscription } from "rxjs";

@Component({
  selector: 'www-error',
  templateUrl: './error.component.html'
})
export class ErrorComponent implements OnInit, OnDestroy {
  @Output() retryEmitter = new EventEmitter<void>();
  isLoading: boolean = false;
  private subscription: Subscription = new Subscription();

  isVisible(): boolean {
    return !this.isLoading;
  }

  constructor(private stateService: StateService,
              private loadingService: LoadingService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.subscription = this.loadingService.getIsLoadingObservable().subscribe((value) => {
      this.isLoading = value;
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  leaveGame() {
    this.gameService.leaveGame(this.stateService.getPlayerId(), this.stateService.getGameId());
    this.stateService.leaveGame();
  }

  tryAgain() {
    this.retryEmitter.emit();
  }
}
