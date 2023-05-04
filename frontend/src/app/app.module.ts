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
import { ResultContainerComponent } from "./component/game/show-ranking/result-container.component";
import { RoundComponent } from './component/game/round/round.component';
import { OverlaySpinnerComponent } from './component/overlay-spinner/overlay-spinner.component';
import { SphinxDisplayComponent } from './component/game/round/sphinx-display/sphinx-display.component';
import { CountDownComponent } from './component/count-down/count-down.component';
import { RankingDisplayComponent } from './component/game/show-ranking/ranking-display/ranking-display.component';
import { EnterPropositionComponent } from "./component/game/round/enter-proposition/enter-proposition.component";
import { SelectPropositionComponent } from "./component/game/round/select-proposition/select-proposition.component";
import {
  SelectionDisclosureComponent
} from './component/game/show-ranking/selection-disclosure/selection-disclosure.component';
import { ErrorComponent } from './component/error/error.component';
import { InfoComponent } from "./component/info/info.component";
import { NgOptimizedImage } from "@angular/common";
import { CurrentGameInfoComponent } from './component/game/current-game-info/current-game-info.component';
import { PromptDisplayComponent } from './component/game/prompt-display/prompt-display.component';

@NgModule({
  declarations: [
    AppComponent,
    WelcomeComponent,
    InitializationContainerComponent,
    JoinComponent,
    GameContainerComponent,
    EnterPropositionComponent,
    SelectPropositionComponent,
    ResultContainerComponent,
    RoundComponent,
    OverlaySpinnerComponent,
    CountDownComponent,
    RankingDisplayComponent,
    SphinxDisplayComponent,
    SelectionDisclosureComponent,
    ErrorComponent,
    InfoComponent,
    CurrentGameInfoComponent,
    PromptDisplayComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    NgOptimizedImage
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
