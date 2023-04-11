import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RankingDisplayComponent } from './ranking-display.component';

describe('RankingDisplayComponent', () => {
  let component: RankingDisplayComponent;
  let fixture: ComponentFixture<RankingDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RankingDisplayComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RankingDisplayComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
