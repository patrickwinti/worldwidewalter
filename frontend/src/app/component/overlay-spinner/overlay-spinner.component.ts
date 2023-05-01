import { ChangeDetectionStrategy, Component } from '@angular/core';
import { LoadingService } from "../../service/loading.service";
import { HttpCancelService } from "../../service/http-cancel.service";
import { StateService } from "../../service/state.service";
import { GameState } from "../../model/game-state";

@Component({
  selector: 'www-overlay-spinner',
  templateUrl: './overlay-spinner.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class OverlaySpinnerComponent {
  isLoading$ = this.loadingService.getIsLoadingObservable();
  displayText = 'warten auf andere Spieler';

  get gameId(): string{
    return this.stateService.getGameId();
  }

  constructor(private loadingService: LoadingService,
              private httpCancelService:HttpCancelService,
              private stateService: StateService) {
  }

  abort() {
    this.httpCancelService.cancelPendingRequests();
  }

  isJoiningGame(): boolean {
    return this.stateService.getCurrentState() == GameState.ENTERING_ROUND;
  }
}
