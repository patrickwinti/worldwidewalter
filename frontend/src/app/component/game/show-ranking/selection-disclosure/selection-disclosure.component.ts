import { Component, Input } from '@angular/core';
import { SelectionDto } from "@api";

@Component({
  selector: 'www-selection-disclosure',
  templateUrl: './selection-disclosure.component.html',
  standalone: false,
})
export class SelectionDisclosureComponent {
  @Input() selections: SelectionDto[];
  @Input() prompt: String;

  concatStrings(strings: String[]) {
    return strings.join(', ');
  }
}
