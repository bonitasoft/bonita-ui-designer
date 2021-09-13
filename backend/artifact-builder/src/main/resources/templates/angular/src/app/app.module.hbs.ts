import { NgModule, CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { {{appTag}} } from './{{appTag}}.component';
import { PropertiesValues } from './directives/properties-values-directive';
import { uidModel } from './directives/uid-model-directive';

@NgModule({
  declarations: [
    {{appTag}},
    PropertiesValues,
    uidModel
  ],
  imports: [
    BrowserModule
  ],
  schemas: [
   CUSTOM_ELEMENTS_SCHEMA
  ],
  providers: [],
  bootstrap: [{{appTag}}]
})
export class {{appTag}}Module { }
