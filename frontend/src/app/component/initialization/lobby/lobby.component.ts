import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { GameService } from "../../../service/game.service";
import { StateService } from "../../../service/state.service";
import { WsService } from "../../../service/ws.service";
import { InitializationState } from "../../../model/initialization-state";
import { LobbyDto } from "../../../dto/lobby-dto";
import { PlayerDto } from "../../../dto/player-dto";
import { firstValueFrom } from "rxjs";
import { avatarHue, initials } from "../../../shared/avatar";

@Component({
  selector: 'www-lobby',
  templateUrl: './lobby.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class LobbyComponent implements OnInit, OnDestroy {
  @Output() initializationStateEmitter = new EventEmitter<InitializationState>();
  lobby: LobbyDto | null = null;
  private topic = '';

  constructor(private gameService: GameService,
              private stateService: StateService,
              private wsService: WsService,
              private cd: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    const gameId = this.stateService.getGameId();
    this.topic = '/topic/games/' + gameId + '/lobby';
    // Push updates as players join/leave and when the host starts.
    this.wsService.subscribe<LobbyDto>(this.topic, lobby => this.onLobbyUpdate(lobby));
    // Initial snapshot: a fresh WebSocket subscription only receives future changes.
    firstValueFrom(this.gameService.getLobby(gameId)).then(
      lobby => this.onLobbyUpdate(lobby),
      () => { /* ignore; a WebSocket update will follow */ }
    );
  }

  ngOnDestroy(): void {
    if (this.topic) {
      this.wsService.unsubscribe(this.topic);
    }
  }

  get gameId(): string {
    return this.stateService.getGameId();
  }

  get players(): PlayerDto[] {
    return this.lobby?.players ?? [];
  }

  get isHost(): boolean {
    return !!this.lobby && this.lobby.hostId === this.stateService.getPlayerId();
  }

  get canStart(): boolean {
    return this.isHost && this.players.length >= (this.lobby?.minimumPlayers ?? Number.MAX_SAFE_INTEGER);
  }

  startGame(): void {
    firstValueFrom(this.gameService.startGame(this.gameId)).then(
      () => { /* transition happens for everyone via the "started" broadcast */ },
      () => { /* ignore; the button simply stays available */ }
    );
  }

  get minimumPlayers(): number {
    return this.lobby?.minimumPlayers ?? 0;
  }

  get missingPlayers(): number {
    return Math.max(0, this.minimumPlayers - this.players.length);
  }

  get emptySlots(): number[] {
    return Array.from({ length: this.missingPlayers }, (_, i) => i);
  }

  readonly initials = initials;
  readonly avatarHue = avatarHue;

  copied = false;

  copyLink(): void {
    navigator.clipboard?.writeText(window.location.origin + '?gameId=' + this.gameId).then(
      () => {
        this.copied = true;
        this.cd.markForCheck();
        setTimeout(() => { this.copied = false; this.cd.markForCheck(); }, 2000);
      },
      () => { /* clipboard unavailable; the code is shown regardless */ }
    );
  }

  private onLobbyUpdate(lobby: LobbyDto): void {
    this.lobby = lobby;
    if (lobby.started) {
      this.initializationStateEmitter.emit(InitializationState.DONE);
    }
    this.cd.markForCheck();
  }
}
