import { Component, OnInit } from '@angular/core';
import { AppState } from "./model/app-state";

@Component({
  selector: 'www-root',
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  title = 'www-ui';
  appState: AppState;
  AppState = AppState;

  ngOnInit(): void {
    this.appState = AppState.INITIALIZATION;
  }

  startGame() {
    this.appState = AppState.GAME;
  }
}
