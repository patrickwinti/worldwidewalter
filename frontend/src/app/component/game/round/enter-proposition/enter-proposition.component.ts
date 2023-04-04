import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { RoundDto } from "../../../../dto/round-dto";
import { Proposition } from "../../../../model/proposition";

@Component({
  selector: 'www-enter-proposition',
  templateUrl: './enter-proposition.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EnterPropositionComponent implements OnInit {
  @Input() round: RoundDto;
  @Output() propositionEmitter = new EventEmitter<string[]>();

  propositionsForGaps: Array<Proposition>;

  get numberOfGaps(): number {
    // return this.round.numberOfGaps
    return 4;
  }

  ngOnInit(): void {
    this.propositionsForGaps = new Array<Proposition>();
    for (let i = 0; i < this.numberOfGaps; i++) {
      this.propositionsForGaps.push({text: ''} as Proposition);
    }
  }

  emitProposition() {
    this.propositionEmitter.emit(
      this.propositionsForGaps.map(value => value.text)
    )
  }
}
