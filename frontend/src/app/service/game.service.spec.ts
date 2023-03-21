import { fakeAsync, flush, TestBed, tick } from '@angular/core/testing';
import { GameService } from './game.service';
import { Observer, of } from "rxjs";
import { getHttpClientMock } from "../testing/mock-services";
import { HttpClient } from "@angular/common/http";
import { GameState } from "../model/game-state";
import { GameDto } from "../dto/game-dto";

describe('GameService', () => {
  let service: GameService;
  let httpMock = getHttpClientMock();

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: HttpClient, useValue: httpMock}
      ]
    }).compileComponents();

    service = TestBed.inject(GameService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call http.get on click createGame', () => {
    // arrange
    httpMock.post.and.returnValue(of(undefined));

    // act
    service.requestNewGame();

    // assert
    expect(httpMock.post).toHaveBeenCalledWith('http://localhost:8080/api/games', {})
  });

  describe('getGameAsSoonAsInGivenState', () => {
    it('should send http requests until condition is met', fakeAsync(() => {
      // arrange
      let gameReady = {
        state: GameState.READY
      } as GameDto;
      let gameWaiting = {
        state: GameState.WAITING_FOR_PLAYERS
      } as GameDto;

      httpMock.get.calls.reset();
      httpMock.get.and.returnValues(
        of(gameWaiting),
        of(gameWaiting),
        of(gameReady),
        of(gameReady)
      );

      // act
      service.getGameAsSoonAsInGivenState('1', GameState.READY, 5, 0)
        .subscribe({
          complete: () => {
            // assert
            expect(httpMock.get).toHaveBeenCalledTimes(5);
          }
        });

      tick(1000);
      flush();
    }));


    it('should send http requests until max retries', fakeAsync(() => {
      // arrange
      let gameWaiting1 = {
        id: '1',
        state: GameState.WAITING_FOR_PLAYERS
      } as GameDto;
      let gameWaiting2 = {
        id: '2',
        state: GameState.WAITING_FOR_PLAYERS
      } as GameDto;
      httpMock.get.and.returnValues(
        of(gameWaiting1),
        of(gameWaiting1),
        of(gameWaiting1),
        of(gameWaiting2),
        of(gameWaiting1)
      );

      // act
      let game$ = service.getGameAsSoonAsInGivenState('1', GameState.READY, 4, 1);

      game$.subscribe({
        next: (val) => {
          expect(val.id).toBe('2');
        },
        complete: () => {
          // assert
          expect(httpMock).toHaveBeenCalledTimes(4);
        }
      } as Observer<GameDto>);

      tick(1000);
      flush();
    }));
  });
});
