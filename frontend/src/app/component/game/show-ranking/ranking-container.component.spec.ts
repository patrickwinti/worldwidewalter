import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RankingContainerComponent } from './ranking-container.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { getGameServiceMock } from "../../../testing/mock-services";
import { GameService } from "../../../service/game.service";

describe('ShowRankingComponent', () => {
  let component: RankingContainerComponent;
  let fixture: ComponentFixture<RankingContainerComponent>;
  let gameService = getGameServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RankingContainerComponent],
      providers: [{provide: GameService, useValue: gameService}],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(RankingContainerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
