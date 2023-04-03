import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { RoundDto } from "../../../dto/round-dto";
import { PropositionSelectionDto } from "../../../dto/proposition-selection-dto";

@Component({
  selector: 'www-select-proposition',
  templateUrl: './select-proposition.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelectPropositionComponent {

  @Input() round: RoundDto;
  @Input() propositionSelectionDto: PropositionSelectionDto;
  @Output() selectedPropositionEmitter = new EventEmitter<string>();

  selectProposition(id: string | undefined): void {
    if (id !== undefined) {
      this.selectedPropositionEmitter.emit(id);
    } else {
      this.selectedPropositionEmitter.emit('no selection');
    }
  }
}
