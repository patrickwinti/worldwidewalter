import {ChangeDetectionStrategy, Component, EventEmitter, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {firstValueFrom} from "rxjs";
import {Session} from "../../model/session";

@Component({
  selector: 'www-welcome',
  templateUrl: './welcome.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WelcomeComponent {
  @Output() newGameSession = new EventEmitter<Session>()

  constructor(private http: HttpClient) {
  }

  async createGame() {
    await firstValueFrom(this.http.get<Session>('http://localhost:8080/create-game')).then(
      (value) => this.newGameSession.emit(value),
      () => this.newGameSession.emit(undefined)
    );
  }
}
