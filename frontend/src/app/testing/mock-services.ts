import {HttpClient} from "@angular/common/http";

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
