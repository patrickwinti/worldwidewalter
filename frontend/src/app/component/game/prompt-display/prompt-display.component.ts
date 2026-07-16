import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'www-prompt-display',
  templateUrl: './prompt-display.component.html',
  standalone: false,
})
export class PromptDisplayComponent implements OnInit {
  @Input() prompt: string;
  @Input() proposition: string[];
  splitPrompt: string[];
  private readonly MARKER: string = '<<walter>>';

  ngOnInit(): void {
    this.splitPrompt = this.prompt.split(this.MARKER);
  }
}
