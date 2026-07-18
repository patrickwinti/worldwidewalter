import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';

import { WelcomeComponent } from './welcome.component';
import { getGameServiceMock, getStateServiceMock } from "src/app/testing/mock-services"
import { of } from "rxjs";
import { GameService } from "../../../service/game.service";
import { GameCreatedDto } from "../../../dto/game-created-dto";
import { StateService } from "../../../service/state.service";
import { CookieService } from "../../../service/cookie.service";
import { WsService } from "../../../service/ws.service";

describe('TestComponent', () => {
  let component: WelcomeComponent;
  let fixture: ComponentFixture<WelcomeComponent>;
  let gameService = getGameServiceMock();
  let stateService = getStateServiceMock();
  let cookieService = jasmine.createSpyObj('CookieService', ['set', 'get', 'delete']);
  let wsService = jasmine.createSpyObj('WsService', ['connect', 'subscribe', 'unsubscribe', 'disconnect']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WelcomeComponent],
      providers: [
        {provide: GameService, useValue: gameService},
        {provide: StateService, useValue: stateService},
        {provide: CookieService, useValue: cookieService},
        {provide: WsService, useValue: wsService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(WelcomeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('createGame should call gameService, store host identity and connect the websocket', fakeAsync(() => {
    // arrange
    gameService.requestNewGame.and.returnValue(of({
      gameId: 'gameId',
      host: {id: 'hostId', playerName: 'Host'}
    } as GameCreatedDto));
    component.playerName = 'Host';

    // act
    component.requestNewGame();
    tick();

    // assert
    expect(gameService.requestNewGame).toHaveBeenCalledWith('Host');
    expect(stateService.setGameId).toHaveBeenCalledOnceWith('gameId');
    expect(stateService.setPlayerId).toHaveBeenCalledOnceWith('hostId');
    expect(wsService.connect).toHaveBeenCalledWith('gameId', 'hostId');
  }))
});
