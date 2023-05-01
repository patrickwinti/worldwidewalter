import { Component, Input } from '@angular/core';

@Component({
  selector: 'www-prompt-display',
  templateUrl: './prompt-display.component.html'
})
export class PromptDisplayComponent {
  @Input() prompt: String;
}
