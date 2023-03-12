import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InitializationContainerComponent } from './initialization-container.component';

describe('InitializationComponent', () => {
  let component: InitializationContainerComponent;
  let fixture: ComponentFixture<InitializationContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InitializationContainerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InitializationContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
