import { HttpClient } from "@angular/common/http";
import { GameService } from "../service/game.service";

export function getHttpClientMock(): jasmine.SpyObj<HttpClient> {
  return jasmine.createSpyObj(
    'HttpClient',
    [
      'get',
      'post',
      'put'
    ]
  )
}

export function getGameServiceMock(): jasmine.SpyObj<GameService> {
  return jasmine.createSpyObj(
    'GameService',
    [
      'requestNewGame',
      'getGame',
      'getGameAsSoonAsInGivenState'
    ]
  )
}
