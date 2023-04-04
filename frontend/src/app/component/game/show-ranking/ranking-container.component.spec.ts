import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RankingContainerComponent } from './ranking-container.component';

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
});
