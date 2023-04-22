import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { GameService } from "../../../service/game.service";
import { RoundDto } from "../../../dto/round-dto";
import { GameState } from "../../../model/game-state";
import { firstValueFrom, Observable } from "rxjs";
import { PropositionSelectionDto } from "../../../dto/proposition-selection-dto";
import { PropositionSubmissionDto } from "../../../dto/proposition-submission-dto";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
  selector: 'www-round',
  templateUrl: './round.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoundComponent implements OnInit {

  @Input() round: RoundDto;
  GameState = GameState;

  propositionSelectionDto$: Observable<PropositionSelectionDto>;
  stateObservable$: Observable<GameState>;

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.stateService.setState(GameState.ENTER_PROPOSITION);
    this.stateObservable$ = this.stateService.getStateObservable();
    this.propositionSelectionDto$ = this.gameService.getAllPropositions(this.round.id);
  }

  submitSelection(selectedPropositionId: string): void {
    firstValueFrom(this.gameService.submitPropositionSelection(this.round.id, selectedPropositionId)).then(
      () => {
        console.log('selection submission successful');
        this.stateService.setState(GameState.SHOW_RESULTS_AND_RANKING);
      },
      (error: HttpErrorResponse) => {
        console.log('an error occurred: ' + error.status);
      });
  }

  sendProposition(gapReplacements: string[]) {
    firstValueFrom(this.gameService.submitProposition(this.round.id, {
      gaps: gapReplacements
    } as PropositionSubmissionDto))
      .then(
        () => {
          console.log('submission successful');
          this.stateService.setState(GameState.SELECT_PROPOSITION);
        },
        (error: HttpErrorResponse) => {
          console.log('an error occurred: ' + error.status);
        }
      );
  }

  getCurrentPlayerId(): String {
    return this.stateService.getPlayerId();
  }
}
