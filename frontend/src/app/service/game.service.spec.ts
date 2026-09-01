import { TestBed } from '@angular/core/testing';
import { GameService } from './game.service';
import { of } from "rxjs";
import { getStateServiceMock } from "../testing/mock-services";
import { GameControllerService, RoundControllerService } from "@api";
import { StateService } from "./state.service";

describe('GameService', () => {
  let service: GameService;
  const gameApi = jasmine.createSpyObj<GameControllerService>('GameControllerService', ['createGame']);
  const roundApi = jasmine.createSpyObj<RoundControllerService>('RoundControllerService', ['getRoundResults']);
  const stateService = getStateServiceMock();

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: GameControllerService, useValue: gameApi },
        { provide: RoundControllerService, useValue: roundApi },
        { provide: StateService, useValue: stateService }
      ]
    }).compileComponents();

    service = TestBed.inject(GameService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should delegate requestNewGame to the generated GameControllerService', () => {
    gameApi.createGame.and.returnValue(of(undefined) as any);

    service.requestNewGame('Host');

    expect(gameApi.createGame).toHaveBeenCalledWith({ playerName: 'Host' });
  });
});
