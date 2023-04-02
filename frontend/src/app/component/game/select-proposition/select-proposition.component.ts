import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PropositionDto } from "../../../dto/proposition-dto";
import { RoundDto } from "../../../dto/round-dto";

@Component({
  selector: 'www-select-proposition',
  templateUrl: './select-proposition.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelectPropositionComponent {

  @Input() round: RoundDto;
  @Input() propositions: PropositionDto[];
  @Output() selectedPropositionEmitter = new EventEmitter<string>();

  selectProposition(i: number | undefined): void {
    if (i !== undefined) {
      this.selectedPropositionEmitter.emit(this.propositions[i].id);
    } else {
      this.selectedPropositionEmitter.emit('no selection');
    }
  }
}
