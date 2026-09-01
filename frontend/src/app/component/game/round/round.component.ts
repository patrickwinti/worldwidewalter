import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { GameService } from "../../../service/game.service";
import { RoundDto } from "../../../dto/round-dto";
import { GameState } from "../../../model/game-state";
import { filter, firstValueFrom, Observable, Subject, take, takeUntil } from "rxjs";
import { PropositionSelectionDto } from "../../../dto/proposition-selection-dto";
import { PropositionSubmissionDto } from "../../../dto/proposition-submission-dto";
import { containsNonEmptyString, isNonEmptyString } from "../../../shared/util";
import { NotificationService } from "../../../service/notification.service";

@Component({
  selector: 'www-round',
  templateUrl: './round.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class RoundComponent implements OnInit, OnDestroy {

  @Input() round: RoundDto;
  GameState = GameState;

  propositionSelectionDto$: Observable<PropositionSelectionDto>;
  stateObservable$: Observable<GameState>;

  private readonly destroy$ = new Subject<void>();

  constructor(private stateService: StateService,
              private gameService: GameService,
              private notificationService: NotificationService) {
  }

  ngOnInit(): void {
    this.stateObservable$ = this.stateService.getStateObservable();
    this.propositionSelectionDto$ = this.gameService.getAllPropositions(this.round.id);
    this.stateService.setPrompt(this.round.prompt);

    // The sphinx cannot select a proposition, so advance them to the results automatically
    // once every active player has selected (instead of requiring a manual "Weiter" click).
    if (this.isSphinx()) {
      this.stateObservable$.pipe(
        filter(state => state === GameState.SELECT_PROPOSITION),
        take(1),
        takeUntil(this.destroy$)
      ).subscribe(() => this.advanceSphinxWhenSelectionsComplete());
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  async submitSelection(selectedPropositionId: string): Promise<void> {
    if (isNonEmptyString(selectedPropositionId)) {
      try {
        await firstValueFrom(this.gameService.submitPropositionSelection(this.round.id, selectedPropositionId));
      } catch {
        // The round moves on either way, but the player must not be left believing their vote
        // counted when it never reached the backend.
        this.notificationService.notify({
          kind: 'selection-not-submitted',
          message: 'Deine Auswahl konnte nicht gesendet werden.'
        });
      }
    }
    this.stateService.setState(GameState.SHOW_RESULTS_AND_RANKING);
  }

  async sendProposition(gapReplacements: string[]): Promise<void> {
    if (containsNonEmptyString(gapReplacements)) {
      try {
        await firstValueFrom(this.gameService.submitProposition(this.round.id, {
          gaps: gapReplacements
        } as PropositionSubmissionDto));
      } catch {
        this.notificationService.notify({
          kind: 'proposition-not-submitted',
          message: 'Dein Vorschlag konnte nicht gesendet werden.'
        });
      }
    }
    this.stateService.setState(GameState.SELECT_PROPOSITION);
  }

  getCurrentPlayerId(): string {
    return this.stateService.getPlayerId();
  }

  private isSphinx(): boolean {
    return this.round.sphinx?.id === this.getCurrentPlayerId();
  }

  /**
   * Polls the results endpoint, which only succeeds once every active player has selected
   * (the polling interceptor retries while it returns 425 Too Early), then moves the sphinx
   * to the results view. Cancelled if the round component is destroyed first (e.g. the
   * selection timer advances the round).
   */
  private advanceSphinxWhenSelectionsComplete(): void {
    // Poll silently (no loading overlay) so the sphinx can read the answers while waiting.
    this.gameService.getResultsForRound(this.round.id, true).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => this.stateService.setState(GameState.SHOW_RESULTS_AND_RANKING),
      error: () => { /* leave the sphinx on the page; the selection timer is the backstop */ }
    });
  }
}
