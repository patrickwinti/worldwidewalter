import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WelcomeComponent } from './welcome.component';
import {HttpClient} from "@angular/common/http";
import {getHttpClientMock} from "src/app/testing/mock-services"
import {of} from "rxjs";

describe('TestComponent', () => {
  let component: WelcomeComponent;
  let fixture: ComponentFixture<WelcomeComponent>;
  let httpMock = getHttpClientMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WelcomeComponent ],
      providers: [
        {provide: HttpClient, useValue: httpMock}
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

  it('should call http.get on click createGame', () => {
    // arrange
    httpMock.get.and.returnValue(of(undefined));

    // act
    component.createGame();

    // assert
    expect(httpMock.get).toHaveBeenCalledWith('http://localhost:8080/create-game')
  })
});
