import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PropositionSelectionDto } from "../../../../dto/proposition-selection-dto";
import { PropositionDto } from "../../../../dto/proposition-dto";
import { RoundDto } from "../../../../dto/round-dto";


@Component({
  selector: 'www-select-proposition',
  templateUrl: './select-proposition.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelectPropositionComponent {
  @Input() round: RoundDto;
  @Input() propositionSelectionDto: PropositionSelectionDto;
  @Input() currentPlayerId: string;
  @Output() selectedPropositionEmitter = new EventEmitter<string>();

  selectProposition(id: string | undefined): void {
    this.selectedPropositionEmitter.emit(id);
  }

  createDisplayTextForProposition(proposition: PropositionDto) {
    let displayText = proposition.gaps.join(', ');
    if (proposition.numberOfAuthors > 1) {
      displayText = displayText + ' (' + proposition.numberOfAuthors + ')'
    }
    return displayText;
  }

  isSphinx(): boolean {
    return this.round.sphinx.id === this.currentPlayerId;
  }

  continue() {
    this.selectedPropositionEmitter.emit(undefined);
  }
}
