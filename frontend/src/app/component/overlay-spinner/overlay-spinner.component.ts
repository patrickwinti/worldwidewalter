import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from "rxjs";
import { LoadingService } from "../../service/loading.service";
import { StateService } from "../../service/state.service";
import { GameState } from "../../model/game-state";

@Component({
  selector: 'www-overlay-spinner',
  templateUrl: './overlay-spinner.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})

export class OverlaySpinnerComponent implements OnInit, OnDestroy {
  isLoading$ = this.loadingService.getIsLoadingObservable();
  displayText = LOADING_TEXT;

  private subscription = new Subscription();

  get gameId(): string {
    return this.stateService.getGameId();
  }

  constructor(private loadingService: LoadingService,
              private stateService: StateService,
              private cd: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    // Only say "waiting for the others" when that is actually what is happening; an ordinary
    // request in flight is not the other players' fault.
    this.subscription = this.loadingService.getIsWaitingForPlayersObservable().subscribe(waiting => {
      this.displayText = waiting ? WAITING_FOR_PLAYERS_TEXT : LOADING_TEXT;
      this.cd.markForCheck();
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  isJoiningGame(): boolean {
    return this.stateService.getCurrentState() == GameState.REQUEST_NEW_ROUND;
  }
}

const LOADING_TEXT = 'Einen Moment…';
const WAITING_FOR_PLAYERS_TEXT = 'Warten auf andere Spieler:innen';
