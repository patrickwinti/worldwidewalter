import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectPropositionComponent } from './select-proposition.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";

describe('SelectPropositionComponent', () => {
  let component: SelectPropositionComponent;
  let fixture: ComponentFixture<SelectPropositionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SelectPropositionComponent],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(SelectPropositionComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
