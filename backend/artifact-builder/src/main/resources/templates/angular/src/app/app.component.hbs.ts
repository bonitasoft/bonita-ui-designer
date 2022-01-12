import { Component } from '@angular/core';

{{#each jsAsset}}
import '{{ this }}';
{{/each}}

{{#each widgets}}
import '{{ this }}';
{{/each}}

{{#each cssAsset}}
import '{{ this }}';
{{/each}}

@Component({
    selector: '{{appTag}}',
    templateUrl: './{{appTag}}.component.html',
    styleUrls: ['style.css']
})

export class {{appTag}} {
    constructor() {
      document.addEventListener('uid-action', (event) => {
        console.log(event);
      });
    }
}
