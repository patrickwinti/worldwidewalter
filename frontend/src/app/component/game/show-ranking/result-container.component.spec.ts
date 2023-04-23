import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultContainerComponent } from './result-container.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { getGameServiceMock } from "../../../testing/mock-services";
import { GameService } from "../../../service/game.service";
import { RankingDto } from "../../../dto/result-dto";

describe('ShowRankingComponent', () => {
  let component: ResultContainerComponent;
  let fixture: ComponentFixture<ResultContainerComponent>;
  let gameService = getGameServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ResultContainerComponent],
      providers: [{provide: GameService, useValue: gameService}],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ResultContainerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should sort ranking', () => {
    // arrange
    var unsorted = [
      {playerName: 'Hanna', points: 12} as RankingDto,
      {playerName: 'Mudi', points: 3} as RankingDto,
      {playerName: 'Herzog', points: 9} as RankingDto,
      {playerName: 'Herzog', points: 1} as RankingDto
    ];

    // act
    var sorted = component.sortRanking(unsorted);

    // assert
    expect(sorted).toEqual([
      {playerName: 'Hanna', points: 12} as RankingDto,
      {playerName: 'Herzog', points: 9} as RankingDto,
      {playerName: 'Mudi', points: 3} as RankingDto,
      {playerName: 'Herzog', points: 1} as RankingDto
    ])
  })
});
