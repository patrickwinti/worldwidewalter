import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnterPropositionComponent } from './enter-proposition.component';
import { StateService } from "../../../../service/state.service";
import { getGameServiceMock, getStateServiceMock } from "../../../../testing/mock-services";
import { RoundDto } from "../../../../dto/round-dto";
import { GameService } from "../../../../service/game.service";
import { PropositionSubmissionDto } from "../../../../dto/proposition-submission-dto";

describe('EnterPropositionComponent', () => {
  let component: EnterPropositionComponent;
  let fixture: ComponentFixture<EnterPropositionComponent>;
  let stateService = getStateServiceMock();
  let gameService = getGameServiceMock();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnterPropositionComponent],
      providers: [
        {provide: StateService, useValue: stateService},
        {provide: GameService, useValue: gameService},
      ]
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
    stateService.getRound.and.returnValue({
      numberOfGaps: 4
    } as RoundDto)

    // act
    component.ngOnInit();

    // assert
    expect(component.propositionsForGaps.length).toBe(4);
    expect(component.propositionsForGaps[0].text).toEqual('');
  });

  it('sendProposition should call gameService', () => {
    // arrange
    gameService.submitProposition.calls.reset();
    component.propositionsForGaps = [{text: '1'}, {text: '2'}];
    spyOnProperty(component, 'round', 'get').and.returnValue({id: 'round0'} as RoundDto);

    // act
    component.sendProposition()

    // assert
    expect(gameService.submitProposition).toHaveBeenCalledOnceWith(
      'round0', {gaps: ['1', '2']} as PropositionSubmissionDto
    )
  })

  // struggling to get test running. Will check with colleagues at work
  // it('on successful submission, go to next state', fakeAsync(()  => {
  //   component.propositionsForGaps = [{text: '1'}, {text: '2'}];
  //   spyOnProperty(component, 'round', 'get').and.returnValue({id: 'round0'} as RoundDto);
  //   gameService.submitProposition.and.returnValue(of());
  //
  //   component.sendProposition();
  //   tick(100);
  //   tick();
  //
  //   expect(stateService.goToNextState).toHaveBeenCalled();
  // }));
});
