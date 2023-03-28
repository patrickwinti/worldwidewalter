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
    expect(service.getCurrentState()).toBe(GameState.WAITING_FOR_PLAYERS);

    service.goToNextState();
    expect(service.getCurrentState()).toBe(GameState.WAITING_FOR_ALL_PROPOSITIONS);

    service.goToNextState();
    expect(service.getCurrentState()).toBe(GameState.WAITING_FOR_ALL_SELECTIONS);

    service.goToNextState();
    expect(service.getCurrentState()).toBe(GameState.WAITING_FOR_PLAYERS);
  });
});
