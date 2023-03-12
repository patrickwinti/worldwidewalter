import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {WelcomeComponent} from "./components/welcome/welcome.component";
import {HttpClientModule} from "@angular/common/http";
import {InitializationContainerComponent} from './components/initialization/initialization-container.component';

@NgModule({
  declarations: [
    AppComponent,
    WelcomeComponent,
    InitializationContainerComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
