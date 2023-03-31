import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectPropositionComponent } from './select-proposition.component';

describe('SelectPropositionComponent', () => {
  let component: SelectPropositionComponent;
  let fixture: ComponentFixture<SelectPropositionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SelectPropositionComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(SelectPropositionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
