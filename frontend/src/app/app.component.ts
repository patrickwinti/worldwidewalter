import { Component } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {firstValueFrom} from "rxjs";
import {testDTO} from "./model/testDTO";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'www-ui';
  testDTO = {
    value: '',
    id: 0
  } as testDTO;

  constructor(private http: HttpClient) {
  }

  async onClickWorld() {
    console.log('clicked');
    this.testDTO = await firstValueFrom(this.http.get<testDTO>('http://localhost:8080/world'));
  }
  async onClickMoon() {
    console.log('clicked');
    this.testDTO = await firstValueFrom(this.http.get<testDTO>('http://localhost:8080/moon'));
    console.log('received: ', this.testDTO.value);
  }
}
