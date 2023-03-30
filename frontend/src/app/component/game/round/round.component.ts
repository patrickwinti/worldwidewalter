import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { GameService } from "../../../service/game.service";
import { RoundDto } from "../../../dto/round-dto";
import { GameState } from "../../../model/game-state";
import { firstValueFrom, Observable } from "rxjs";
import { AllPropositionsDto } from "../../../dto/all-propositions-dto";
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

  allPropositions$: Observable<AllPropositionsDto>;
  stateObservable$: Observable<GameState>;

  constructor(public stateService: StateService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.stateObservable$ = this.stateService.getStateObservable();
    this.allPropositions$ = this.gameService.getAllPropositions(this.round.id);
  }

  submitSelection(selectedProposition: string) {
    firstValueFrom(this.gameService.submitSelection(selectedProposition)).then(
      () => {
        console.log('selection submission successful');
        this.stateService.goToNextState();
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
          this.stateService.goToNextState();
        },
        (error: HttpErrorResponse) => {
          console.log('an error occurred: ' + error.status);
        }
      );
  }
}
