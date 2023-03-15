import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'www-join',
  templateUrl: './join.component.html'
})
export class JoinComponent implements OnInit {
  @Input() presetGameId: string;
  gameIdIsReadOnly: boolean;
  gameId: string;
  userName: string;

  joinGame() {
    console.log('joining game: ' + this.gameId + 'with username: ' + this.userName);
  }

  ngOnInit(): void {
    if (this.presetGameId !== undefined) {
      this.gameId = this.presetGameId;
      this.gameIdIsReadOnly = true;
    } else {
      this.gameId = '';
      this.gameIdIsReadOnly = false;
    }
  }
}
