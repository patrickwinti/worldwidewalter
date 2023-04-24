import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { RoundDto } from "../../../../dto/round-dto";
import { PropositionSelectionDto } from "../../../../dto/proposition-selection-dto";
import { PropositionDto } from "../../../../dto/proposition-dto";


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
  @Output() goToResultsPageEmitter = new EventEmitter<void>();

  selectProposition(id: string | undefined): void {
    if (id !== undefined) {
      this.selectedPropositionEmitter.emit(id);
    } else {
      this.selectedPropositionEmitter.emit('no selection');
    }
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
    if (this.isSphinx()) {
      this.goToResultsPageEmitter.emit();
    } else {
      this.selectedPropositionEmitter.emit(undefined);
    }
  }
}
