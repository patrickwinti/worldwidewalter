import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PropositionSelectionDto } from "../../../../dto/proposition-selection-dto";
import { PropositionDto } from "../../../../dto/proposition-dto";
import { RoundDto } from "../../../../dto/round-dto";


@Component({
  selector: 'www-select-proposition',
  templateUrl: './select-proposition.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class SelectPropositionComponent {
  @Input() round: RoundDto;
  @Input() propositionSelectionDto: PropositionSelectionDto;
  @Input() currentPlayerId: string;
  @Output() selectedPropositionEmitter = new EventEmitter<string>();

  selectedId: string | undefined;

  selectProposition(id: string | undefined): void {
    this.selectedPropositionEmitter.emit(id);
  }

  /** Mark a proposition as the (not yet submitted) choice; submission happens on confirm. */
  pick(proposition: PropositionDto): void {
    if (!proposition.readOnly) {
      this.selectedId = proposition.id;
    }
  }

  confirmSelection(): void {
    if (this.selectedId !== undefined) {
      this.selectedPropositionEmitter.emit(this.selectedId);
    }
  }

  isSphinx(): boolean {
    return this.round.sphinx?.id === this.currentPlayerId;
  }

  showContinueButton(): boolean {
    // The sphinx no longer needs a manual button — they advance automatically once everyone
    // has selected. The button remains only for the edge case where a non-sphinx player has
    // no selectable proposition.
    return !this.isSphinx() && this.allPropositionsReadOnly();
  }

  continue() {
    this.selectedPropositionEmitter.emit(undefined);
  }

  private allPropositionsReadOnly(): boolean {
    return this.propositionSelectionDto.propositions.every(proposition => proposition.readOnly);
  }
}
