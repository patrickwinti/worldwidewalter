import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { StateService } from "../../../service/state.service";
import { GameService } from "../../../service/game.service";
import { RoundDto } from "../../../dto/round-dto";
import { PropositionSubmissionDto } from "../../../dto/proposition-submission-dto";
import { firstValueFrom } from "rxjs";
import { Proposition } from "../../../model/proposition";
import { HttpErrorResponse } from "@angular/common/http";

@Component({
  selector: 'www-enter-proposition',
  templateUrl: './enter-proposition.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EnterPropositionComponent implements OnInit {

  propositionsForGaps: Array<Proposition>;

  constructor(private stateService: StateService,
              private gameService: GameService) {
  }

  get numberOfGaps(): number {
    // return this.round.numberOfGaps
    return 4;
  }

  get prompt(): string {
    return this.round.prompt;
  }

  get round(): RoundDto {
    return this.stateService.getRound();
  }

  async sendProposition() {
    await firstValueFrom(this.gameService.submitProposition(this.round.id, {
      gaps: this.propositionsForGaps.map((value) => value.text)
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

  ngOnInit(): void {
    this.propositionsForGaps = new Array<Proposition>();
    for (let i = 0; i < this.numberOfGaps; i++) {
      this.propositionsForGaps.push({text: ''} as Proposition);
    }
  }
}
