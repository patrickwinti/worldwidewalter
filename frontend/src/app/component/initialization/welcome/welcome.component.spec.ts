import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';

import { WelcomeComponent } from './welcome.component';
import { getGameServiceMock } from "src/app/testing/mock-services"
import { of } from "rxjs";
import { GameService } from "../../../service/game.service";
import { GameDto } from "../../../dto/game-dto";

describe('TestComponent', () => {
  let component: WelcomeComponent;
  let fixture: ComponentFixture<WelcomeComponent>;
  let gameService = getGameServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WelcomeComponent],
      providers: [
        {provide: GameService, useValue: gameService}
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

  it('createGame should call gameService', () => {
    // arrange
    gameService.requestNewGame.and.returnValue(of({} as GameDto));

    // act
    component.requestNewGame();

    // assert
    expect(gameService.requestNewGame).toHaveBeenCalled();
  })

  it('createGame should emit gameDto on successful http call', fakeAsync(() => {
    // arrange
    gameService.requestNewGame.and.returnValue(of({
      id: 'gameId'
    } as GameDto));
    spyOn(component.newGameEmitter, 'emit');

    // act
    component.requestNewGame();
    tick();

    // assert
    expect(gameService.requestNewGame).toHaveBeenCalled();
    expect(component.newGameEmitter.emit).toHaveBeenCalledOnceWith({
      id: 'gameId'
    } as GameDto);
  }))
});
