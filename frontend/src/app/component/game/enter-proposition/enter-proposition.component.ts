import { Component } from '@angular/core';
import { StateService } from "../../../service/state.service";

@Component({
  selector: 'www-enter-proposition',
  templateUrl: './enter-proposition.component.html'
})
export class EnterPropositionComponent {

  constructor(private stateService: StateService) {
  }

  sendProposition() {
    this.stateService.goToNextState();
  }
}
