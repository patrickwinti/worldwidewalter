import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowRankingComponent } from './show-ranking.component';

describe('ShowRankingComponent', () => {
  let component: ShowRankingComponent;
  let fixture: ComponentFixture<ShowRankingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ShowRankingComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ShowRankingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
