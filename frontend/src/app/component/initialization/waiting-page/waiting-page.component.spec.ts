import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WaitingPageComponent } from './waiting-page.component';
import { getGameServiceMock } from "../../../testing/mock-services";
import { GameService } from "../../../service/game.service";
import { NO_ERRORS_SCHEMA } from "@angular/core";

describe('WaitingPageComponent', () => {
  let component: WaitingPageComponent;
  let fixture: ComponentFixture<WaitingPageComponent>;

  beforeEach(async () => {
    const gameService = getGameServiceMock();

    await TestBed.configureTestingModule({
      declarations: [ WaitingPageComponent ],
      providers: [
        {provide: GameService, useValue: gameService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WaitingPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
