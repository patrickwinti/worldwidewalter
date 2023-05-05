import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { RoundDto } from "../../../../dto/round-dto";
import { WalterReplacement } from "../../../../model/walterReplacement";
import { containsNonEmptyString } from "../../../../shared/util";

@Component({
  selector: 'www-enter-proposition',
  templateUrl: './enter-proposition.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EnterPropositionComponent implements OnInit {
  @Input() round: RoundDto;
  @Output() propositionEmitter = new EventEmitter<string[]>();

  proposition: Array<WalterReplacement>;

  get numberOfPlaceholders(): number {
    return this.round.numberOfPlaceholders;
  }

  get canEmitProposition(): boolean {
    return containsNonEmptyString(this.proposition.map(value => value.text));
  }

  ngOnInit(): void {
    this.proposition = new Array<WalterReplacement>();
    for (let i = 0; i < this.numberOfPlaceholders; i++) {
      this.proposition.push({text: ''} as WalterReplacement);
    }
  }

  emitProposition() {
    this.propositionEmitter.emit(
      this.proposition.map(value => value.text)
    )
  }
}
