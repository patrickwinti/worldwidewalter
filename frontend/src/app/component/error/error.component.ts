import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { StateService } from "../../service/state.service";
import { LoadingService } from "../../service/loading.service";
import { GameService } from "../../service/game.service";
import { Subscription } from "rxjs";
import { HttpErrorResponse, HttpStatusCode } from "@angular/common/http";

@Component({
  selector: 'www-error',
  templateUrl: './error.component.html'
})
export class ErrorComponent implements OnInit, OnDestroy {
  @Input() httpError: HttpErrorResponse | null;
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

  getErrorMessage(): string {
    if (this.httpError !== null) {
      switch (this.httpError.status) {
        case HttpStatusCode.Forbidden:
          return 'Das Spiel läuft bereits. Bitte zu einem anderen Zeitpunkt nochmals versuchen.';
        case HttpStatusCode.NotFound:
          return 'Das Spiel wurde nicht gefunden.';
        case HttpStatusCode.Conflict:
          return 'Das Spiel ist bereits voll. Bitte zu einem anderen Zeitpunkt nochmals versuchen.';
        case HttpStatusCode.TooEarly:
          return 'Es haben sich nicht genügend Spieler:innen in dem gegebenen Zeitfenster angemeldet. Versuch es noch einmal.';
      }
    }
    return 'Unbekannter Fehler';
  }
}
