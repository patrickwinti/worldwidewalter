import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectionDisclosureComponent } from './selection-disclosure.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";

describe('SelectionDisclosureComponent', () => {
  let component: SelectionDisclosureComponent;
  let fixture: ComponentFixture<SelectionDisclosureComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectionDisclosureComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SelectionDisclosureComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
