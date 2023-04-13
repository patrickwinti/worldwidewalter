import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RankingContainerComponent } from './ranking-container.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { getGameServiceMock } from "../../../testing/mock-services";
import { GameService } from "../../../service/game.service";
import { ResultDto } from "../../../dto/results-dto";

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


  it('should sort ranking', () => {
    // arrange
    var unsorted = [
      {playerName: 'Hanna', points: 12} as ResultDto,
      {playerName: 'Mudi', points: 3} as ResultDto,
      {playerName: 'Herzog', points: 9} as ResultDto,
      {playerName: 'Herzog', points: 1} as ResultDto
    ];

    // act
    var sorted = component.sortRanking(unsorted);

    // assert
    expect(sorted).toEqual([
      {playerName: 'Hanna', points: 12} as ResultDto,
      {playerName: 'Herzog', points: 9} as ResultDto,
      {playerName: 'Mudi', points: 3} as ResultDto,
      {playerName: 'Herzog', points: 1} as ResultDto
    ])
  })
});
