import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PropositionSelectionDto } from "../../../../dto/proposition-selection-dto";
import { PropositionDto } from "../../../../dto/proposition-dto";


@Component({
  selector: 'www-select-proposition',
  templateUrl: './select-proposition.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelectPropositionComponent {
  @Input() propositionSelectionDto: PropositionSelectionDto;
  @Output() selectedPropositionEmitter = new EventEmitter<string>();

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

  continue() {
    this.selectedPropositionEmitter.emit(undefined);
  }
}
