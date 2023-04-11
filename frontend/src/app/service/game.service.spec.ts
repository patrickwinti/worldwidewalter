import { TestBed } from '@angular/core/testing';
import { GameService } from './game.service';
import { of } from "rxjs";
import { getAppConfigServiceMock, getHttpClientMock } from "../testing/mock-services";
import { HttpClient } from "@angular/common/http";
import { AppConfigService } from "./app-config.service";

describe('GameService', () => {
  let service: GameService;
  let httpMock = getHttpClientMock();
  let appConfigService = getAppConfigServiceMock();
  let baseUrl = 'baseUrl';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: HttpClient, useValue: httpMock},
        {provide: AppConfigService, useValue: appConfigService}
      ]
    }).compileComponents();

    appConfigService.getBaseUrl.and.returnValue(baseUrl);
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
    expect(httpMock.post).toHaveBeenCalledWith(baseUrl + '/games', null)
  });
});
