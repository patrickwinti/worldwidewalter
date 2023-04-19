import { TestBed } from '@angular/core/testing';

import { StateService } from './state.service';
import { GameState } from "../model/game-state";

describe('StateService', () => {
  let service: StateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('goToNextState should go to correct state', () => {
    // assert initial condition
    expect(service.getCurrentState()).toBe(GameState.ENTERING_ROUND);

    service.goToNextState();
    expect(service.getCurrentState()).toBe(GameState.ENTER_PROPOSITION);

    service.goToNextState();
    expect(service.getCurrentState()).toBe(GameState.SELECT_PROPOSITION);

    service.goToNextState();
    expect(service.getCurrentState()).toBe(GameState.SHOW_RANKING);
  });
});
