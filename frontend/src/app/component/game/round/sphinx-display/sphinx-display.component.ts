import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { PlayerDto } from "../../../../dto/player-dto";

@Component({
  selector: 'www-sphinx-display',
  templateUrl: './sphinx-display.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class SphinxDisplayComponent {
  @Input() sphinx: PlayerDto;
  @Input() currentPlayerId: string;

  get isCurrentPlayer(): boolean {
    return this.sphinx?.id === this.currentPlayerId;
  }

  get badgeText(): string {
    return this.isCurrentPlayer ? 'Du bist die Sphinx' : 'Sphinx: ' + this.sphinx?.playerName;
  }

  get questionText(): string {
    return this.isCurrentPlayer
      ? 'Mit was würdest du „Walter“ ersetzen?'
      : 'Mit was würde ' + this.sphinx?.playerName + ' „Walter“ ersetzen?';
  }
}

