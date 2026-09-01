import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RankingDto } from "@api";
import { avatarHue, initials } from "../../../../shared/avatar";

@Component({
  selector: 'www-ranking-display',
  templateUrl: './ranking-display.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class RankingDisplayComponent {
  @Input() sortedResults: RankingDto[];

  readonly initials = initials;
  readonly avatarHue = avatarHue;

  /** Top three, reordered 2 – 1 – 3 so the winner sits centre on the podium. */
  get podium(): RankingDto[] {
    const top = (this.sortedResults ?? []).slice(0, 3);
    if (top.length === 3) {
      return [top[1], top[0], top[2]];
    }
    return top;
  }

  get rest(): RankingDto[] {
    return (this.sortedResults ?? []).slice(3);
  }

  rankOf(entry: RankingDto): number {
    return (this.sortedResults ?? []).indexOf(entry) + 1;
  }
}
