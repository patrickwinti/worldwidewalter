import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnterPropositionComponent } from './enter-proposition.component';
import { RoundDto } from "../../../../dto/round-dto";
import { NO_ERRORS_SCHEMA } from "@angular/core";

describe('EnterPropositionComponent', () => {
  let component: EnterPropositionComponent;
  let fixture: ComponentFixture<EnterPropositionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnterPropositionComponent],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(EnterPropositionComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit should initialize Array of Propositions', () => {
    // arrange
    component.round = {
      numberOfPlaceholders: 4
    } as RoundDto;

    // act
    component.ngOnInit();

    // assert
    expect(component.proposition.length).toBe(4);
    expect(component.proposition[0].text).toEqual('');
  });

  it('sendProposition should call gameService', () => {
    // arrange
    component.proposition = [{text: '1'}, {text: '2'}];
    component.round = {id: 'round0'} as RoundDto;
    let spy = spyOn(component.propositionEmitter,'emit');

    // act
    component.emitProposition();

    // assert
    expect(spy).toHaveBeenCalledOnceWith(
      ['1', '2']
    )
  })
});
