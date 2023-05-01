import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CurrentGameInfoComponent } from './current-game-info.component';

describe('CurrentGameInfoComponent', () => {
  let component: CurrentGameInfoComponent;
  let fixture: ComponentFixture<CurrentGameInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CurrentGameInfoComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CurrentGameInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
