import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { ResultDto } from "../../../../dto/results-dto";

@Component({
  selector: 'www-ranking-display',
  templateUrl: './ranking-display.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RankingDisplayComponent {
  @Input() sortedResults: ResultDto[];
}
