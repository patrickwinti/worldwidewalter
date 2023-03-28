import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnterPropositionComponent } from './enter-proposition.component';

describe('EnterPropositionComponent', () => {
  let component: EnterPropositionComponent;
  let fixture: ComponentFixture<EnterPropositionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EnterPropositionComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnterPropositionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
