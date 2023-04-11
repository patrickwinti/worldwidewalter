import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CountDownComponent } from './count-down.component';

describe('CountDownComponent', () => {
  let component: CountDownComponent;
  let fixture: ComponentFixture<CountDownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CountDownComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CountDownComponent);
    component = fixture.componentInstance;
    component.timeoutString = '2023-03-29 22:13:08'
  });

  it('should create', () => {
    component.ngOnInit();
    expect(component).toBeTruthy();
  });
});
