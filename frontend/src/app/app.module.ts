import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { WelcomeComponent } from "./component/initialization/welcome/welcome.component";
import { HTTP_INTERCEPTORS, HttpClientModule } from "@angular/common/http";
import { InitializationContainerComponent } from './component/initialization/initialization-container.component';
import { JoinComponent } from './component/initialization/join/join.component';
import { FormsModule } from "@angular/forms";
import { GameContainerComponent } from './component/game/game-container.component';
import { HttpPollingInterceptor } from "./service/http-polling.interceptor";
import { EnterPropositionComponent } from './component/game/enter-proposition/enter-proposition.component';
import { SelectPropositionComponent } from "./component/game/select-proposition/select-proposition.component";
import { RankingContainerComponent } from "./component/game/show-ranking/ranking-container.component";
import { RoundComponent } from './component/game/round/round.component';
import { OverlaySpinnerComponent } from './component/overlay-spinner/overlay-spinner.component';
import { SphinxDisplayComponent } from './component/game/round/sphinx-display/sphinx-display.component';
import { CountDownComponent } from './component/count-down/count-down.component';
import { RankingDisplayComponent } from './component/game/show-ranking/ranking-display/ranking-display.component';

@NgModule({
  declarations: [
    AppComponent,
    WelcomeComponent,
    InitializationContainerComponent,
    JoinComponent,
    GameContainerComponent,
    EnterPropositionComponent,
    SelectPropositionComponent,
    RankingContainerComponent,
    RoundComponent,
    OverlaySpinnerComponent,
    SelectPropositionComponent,
    CountDownComponent,
    RankingDisplayComponent,
    SphinxDisplayComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpPollingInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
