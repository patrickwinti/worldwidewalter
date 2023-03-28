import { ComponentFixture, TestBed } from '@angular/core/testing';
import { getGameServiceMock } from "../../../testing/mock-services";
import { GameService } from "../../../service/game.service";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { WaitingForPlayersComponent } from "./waiting-for-players.component";

describe('WaitingPageComponent', () => {
  let component: WaitingForPlayersComponent;
  let fixture: ComponentFixture<WaitingForPlayersComponent>;

  beforeEach(async () => {
    const gameService = getGameServiceMock();

    await TestBed.configureTestingModule({
      declarations: [WaitingForPlayersComponent],
      providers: [
        {provide: GameService, useValue: gameService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(WaitingForPlayersComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
