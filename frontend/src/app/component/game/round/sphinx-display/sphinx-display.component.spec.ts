import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SphinxDisplayComponent } from './sphinx-display.component';

describe('SphinxDisplayComponent', () => {
  let component: SphinxDisplayComponent;
  let fixture: ComponentFixture<SphinxDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SphinxDisplayComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SphinxDisplayComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
