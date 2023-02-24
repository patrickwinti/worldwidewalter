import { Component } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {firstValueFrom} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'www-ui';
  displayText: string = '';

  constructor(private http: HttpClient) {
  }

  async onClick() {
    console.log('clicked');
    this.displayText = await firstValueFrom(this.http.get<string>('http://localhost:8080'));
  }
}
