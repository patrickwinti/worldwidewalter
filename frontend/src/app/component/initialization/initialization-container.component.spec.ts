import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InitializationContainerComponent } from './initialization-container.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('InitializationContainerComponent', () => {
  let component: InitializationContainerComponent;
  let fixture: ComponentFixture<InitializationContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InitializationContainerComponent ],
      schemas: [NO_ERRORS_SCHEMA],
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
