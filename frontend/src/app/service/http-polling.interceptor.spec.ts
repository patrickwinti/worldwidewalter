import { TestBed } from '@angular/core/testing';
import { HttpPollingInterceptor } from "./http-polling.interceptor";
import { getHttpClientMock } from "../testing/mock-services";
import { HttpClient } from "@angular/common/http";


describe('TestInterceptor', () => {
  let httpMock = getHttpClientMock();
  let interceptor: HttpPollingInterceptor;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        HttpPollingInterceptor,
        {provide: HttpClient, useValue: httpMock}
      ]
    }).compileComponents();

    interceptor = TestBed.inject(HttpPollingInterceptor);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });
});
