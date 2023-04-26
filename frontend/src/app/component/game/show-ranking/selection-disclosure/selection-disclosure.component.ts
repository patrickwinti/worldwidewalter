import { Component, Input } from '@angular/core';
import { SelectionDto } from "../../../../dto/result-dto";

@Component({
  selector: 'www-selection-disclosure',
  templateUrl: './selection-disclosure.component.html'
})
export class SelectionDisclosureComponent {
  @Input() selections: SelectionDto[];

  concatStrings(strings: String[]) {
    return strings.join(', ');
  }
}
