import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RankingContainerComponent } from './ranking-container.component';
import { ResultDto } from "../../../dto/results-dto";

describe('ShowRankingComponent', () => {
  let component: RankingContainerComponent;
  let fixture: ComponentFixture<RankingContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RankingContainerComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(RankingContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
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
