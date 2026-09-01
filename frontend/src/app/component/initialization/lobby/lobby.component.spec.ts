import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';

import { LobbyComponent } from './lobby.component';
import { getGameServiceMock, getStateServiceMock } from "../../../testing/mock-services";
import { of } from "rxjs";
import { GameService } from "../../../service/game.service";
import { StateService } from "../../../service/state.service";
import { WsService } from "../../../service/ws.service";
import { LobbyDto } from "@api";
import { InitializationState } from "../../../model/initialization-state";

describe('LobbyComponent', () => {
  let component: LobbyComponent;
  let fixture: ComponentFixture<LobbyComponent>;
  let gameService = getGameServiceMock();
  let stateService = getStateServiceMock();
  let wsService = jasmine.createSpyObj('WsService', ['subscribe', 'unsubscribe', 'connect', 'disconnect']);

  const lobby = (over: Partial<LobbyDto>): LobbyDto =>
    ({players: [], hostId: 'host-1', started: false, minimumPlayers: 4, ended: false, ...over});

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LobbyComponent],
      providers: [
        {provide: GameService, useValue: gameService},
        {provide: StateService, useValue: stateService},
        {provide: WsService, useValue: wsService}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LobbyComponent);
    component = fixture.componentInstance;
    stateService.getGameId.and.returnValue('WXYZ');
    stateService.getPlayerId.and.returnValue('host-1');
    gameService.getLobby.and.returnValue(of(lobby({})));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('subscribes to the lobby topic and loads the initial snapshot', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(wsService.subscribe).toHaveBeenCalledWith('/topic/games/WXYZ/lobby', jasmine.any(Function));
    expect(gameService.getLobby).toHaveBeenCalledWith('WXYZ');
    expect(component.isHost).toBeTrue();
  }));

  it('enables start only for the host once the minimum players joined', () => {
    component['onLobbyUpdate'](lobby({players: [{id: 'host-1', playerName: 'A'}]}));
    expect(component.canStart).toBeFalse();

    component['onLobbyUpdate'](lobby({
      players: [{id: 'host-1', playerName: 'A'}, {id: '2', playerName: 'B'},
        {id: '3', playerName: 'C'}, {id: '4', playerName: 'D'}]
    }));
    expect(component.canStart).toBeTrue();
  });

  it('does not offer start to a non-host', () => {
    stateService.getPlayerId.and.returnValue('someone-else');
    component['onLobbyUpdate'](lobby({
      players: [{id: 'host-1', playerName: 'A'}, {id: '2', playerName: 'B'},
        {id: '3', playerName: 'C'}, {id: '4', playerName: 'D'}]
    }));
    expect(component.isHost).toBeFalse();
    expect(component.canStart).toBeFalse();
  });

  it('transitions to DONE when the lobby reports the game as started', () => {
    spyOn(component.initializationStateEmitter, 'emit');
    component['onLobbyUpdate'](lobby({started: true}));
    expect(component.initializationStateEmitter.emit).toHaveBeenCalledWith(InitializationState.DONE);
  });
});
