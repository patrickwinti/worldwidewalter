import { TestBed } from '@angular/core/testing';
import { GameService } from './game.service';
import { of } from "rxjs";
import { getHttpClientMock } from "../testing/mock-services";
import { HttpClient } from "@angular/common/http";

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
});
