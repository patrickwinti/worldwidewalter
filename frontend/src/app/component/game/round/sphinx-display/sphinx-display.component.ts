import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { PlayerDto } from "../../../../dto/player-dto";

@Component({
  selector: 'www-sphinx-display',
  templateUrl: './sphinx-display.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SphinxDisplayComponent {
  @Input() sphinx: PlayerDto;
  @Input() currentPlayerId: string;

  getDisplayText(): string {
    if (this.sphinx.id === this.currentPlayerId) {
      return 'Du bist die Sphinx!';
    } else {
      return 'Die Sphinx ist: ' + this.sphinx.playerName;
    }
  }
}
