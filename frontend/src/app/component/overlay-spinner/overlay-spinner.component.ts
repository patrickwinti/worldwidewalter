import { ChangeDetectionStrategy, Component } from '@angular/core';
import { LoadingService } from "../../service/loading.service";
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

  get gameId(): string {
    return this.stateService.getGameId();
  }

  constructor(private loadingService: LoadingService,
              private stateService: StateService) {
  }

  isJoiningGame(): boolean {
    return this.stateService.getCurrentState() == GameState.REQUEST_NEW_ROUND;
  }
}
