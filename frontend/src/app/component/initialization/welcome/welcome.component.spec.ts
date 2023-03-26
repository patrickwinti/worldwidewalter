import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';

import { WelcomeComponent } from './welcome.component';
import { getGameServiceMock, getStateServiceMock } from "src/app/testing/mock-services"
import { of } from "rxjs";
import { GameService } from "../../../service/game.service";
import { GameDto } from "../../../dto/game-dto";
import { StateService } from "../../../service/state.service";

describe('TestComponent', () => {
  let component: WelcomeComponent;
  let fixture: ComponentFixture<WelcomeComponent>;
  let gameService = getGameServiceMock();
  let stateService = getStateServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WelcomeComponent],
      providers: [
        {provide: GameService, useValue: gameService},
        {provide: StateService, useValue: stateService}
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(WelcomeComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('createGame should call gameService and set gameId in stateService', fakeAsync(() => {
    // arrange
    gameService.requestNewGame.and.returnValue(of({
      id: 'gameId'
    } as GameDto));

    // act
    component.requestNewGame();
    tick();

    // assert
    expect(gameService.requestNewGame).toHaveBeenCalled();
    expect(stateService.setGameId).toHaveBeenCalledOnceWith('gameId');
  }))
});
