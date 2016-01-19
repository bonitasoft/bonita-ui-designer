(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .value('widgets', [
      {
        designerVersion: '1.3.0-SNAPSHOT',
        id: 'pbParagraph',
        name: 'Paragraph',
        lastUpdate: 1430212276146,
        template: '<p class="text-{{ properties.alignment }}" ng-bind-html="properties.text"></p>\r\n',
        icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><g><path fill="#424251" d="M8.2,20h15v-1h-15V20z M8.2,17h31v-1h-31V17z M8.2,4h32V3h-32V4z M8.2,14h34v-1h-34V14z M8.2,7h30V6h-30V7z M8.2,1h35V0h-35V1z"/></g></svg>',
        custom: false,
        order: 12,
        properties: [{
          label: 'Text',
          name: 'text',
          caption: 'You can use basics html tags',
          type: 'html',
          bond: 'expression',
          defaultValue: 'Neque porro quisquam est qui dolorem ipsum quia dolor sit amet,<br/>consectetur,<br/>adipisci velit...'
        }, {
          label: 'Alignment',
          name: 'alignment',
          type: 'choice',
          defaultValue: 'left',
          choiceValues: ['left', 'center', 'right'],
          bond: 'constant'
        }]
      },
      {
        designerVersion: '1.3.0-SNAPSHOT',
        id: 'pbInput',
        name: 'Input',
        lastUpdate: 1430212276119,
        template: '<div class="row">\r\n    <label\r\n        ng-if="!properties.labelHidden"\r\n        ng-class="{ \"widget-label-horizontal\": !properties.labelHidden && properties.labelPosition === \"left\"}"\r\n        class="col-xs-{{ !properties.labelHidden && properties.labelPosition === \"left\" ? properties.labelWidth : 12 }}">\r\n        {{ properties.label }}\r\n    </label>\r\n\r\n    <div class="col-xs-{{ 12 - (!properties.labelHidden && properties.labelPosition === \"left\" ? properties.labelWidth : 0) }}">\r\n        <input\r\n            type="{{properties.type}}"\r\n            class="form-control"\r\n            placeholder="{{ properties.placeholder }}"\r\n            ng-model="properties.value"\r\n            ng-readonly="properties.readOnly">\r\n    </div>\r\n\r\n</div>\r\n',
        icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><g><path fill="#424251" d="M1,19V1h48v18H1z M0,0v20h50V0H0z M6,18v-1H5v1H6z M8,17H7v1h1V17z M7,2v1h1V2H7z M5,3h1V2H5V3z M6,3v14h1 V3H6z "/></g></svg>',
        custom: false,
        order: 2,
        'properties': [
          {
            'label': 'Required',
            'name': 'required',
            'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
            'type': 'boolean',
            'defaultValue': false,
            bond: 'expression'
          },
          {
            'label': 'Value min length',
            'name': 'minLength',
            'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
            'type': 'integer',
            'defaultValue': '',
            'constraints': {
              'min': '0'
            },
            bond: 'expression'
          },
          {
            'label': 'Value max length',
            'name': 'maxLength',
            'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
            'type': 'integer',
            'defaultValue': '',
            'constraints': {
              'min': '1'
            },
            bond: 'expression'
          },
          {
            'label': 'Read-only',
            'name': 'readOnly',
            'type': 'boolean',
            'defaultValue': false,
            bond: 'expression'
          },
          {
            'label': 'Label hidden',
            'name': 'labelHidden',
            'type': 'boolean',
            'bond': 'constant',
            'defaultValue': false
          },
          {
            'label': 'Label',
            'name': 'label',
            'type': 'text',
            'defaultValue': 'Default label',
            'showFor': 'properties.labelHidden.value === false',
            'bond': 'interpolation'
          },
          {
            'label': 'Label position',
            'name': 'labelPosition',
            'type': 'choice',
            'choiceValues': [
              'left',
              'top'
            ],
            'defaultValue': 'top',
            'bond': 'constant',
            'showFor': 'properties.labelHidden.value === false'
          },
          {
            'label': 'Label width',
            'name': 'labelWidth',
            'type': 'integer',
            'defaultValue': 4,
            'showFor': 'properties.labelHidden.value === false',
            'bond': 'constant',
            'constraints': {
              'min': '1',
              'max': '12'
            }
          },
          {
            'label': 'Placeholder',
            'name': 'placeholder',
            'help': 'Short hint that describes the expected value',
            'type': 'text',
            'bond': 'interpolation'
          },
          {
            'label': 'Value',
            'name': 'value',
            'type': 'text',
            'bond': 'variable'
          },
          {
            'label': 'Type',
            'name': 'type',
            'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
            'type': 'choice',
            'choiceValues': [
              'text',
              'number',
              'email',
              'password'
            ],
            'bond': 'constant',
            'defaultValue': 'text'
          },
          {
            'label': 'Min value',
            'name': 'min',
            'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
            'type': 'integer',
            'showFor': 'properties.type.value === "number"',
            bond: 'expression'
          },
          {
            'label': 'Max value',
            'name': 'max',
            'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
            'type': 'integer',
            'showFor': 'properties.type.value === "number"',
            bond: 'expression'
          }
        ]
      },
      {
        id: 'customAwesomeWidget',
        name: 'awesomeWidget',
        custom: true,
        lastUpdate: 1447944407862,
        template: '<div>My {{ properties.qualifier }} widget just {{ properties.verb }}!</div>',
        properties: [
          {
            label: 'Qualifier',
            name: 'qualifier',
            type: 'text',
            defaultValue: 'awesome',
            bond: 'expression'
          },
          {
            label: 'Verb',
            name: 'verb',
            type: 'text',
            defaultValue: 'rocks',
            bond: 'expression'
          }
        ],
        assets: [
          {
            'name': 'awesome-gif.gif',
            'type': 'img',
            'order': 3,
            'active': true
          },
          {
            'name': 'https://awesome.cdn.com/cool.js',
            'type': 'js',
            'order': 2,
            'active': true,
            'external': true
          }
        ]
      },
      {
        id: 'customFavoriteWidget',
        name: 'favoriteWidget',
        custom: true,
        lastUpdate: 1447891242960,
        favorite: true,
        template: '<div>My {{ properties.qualifier }} widget just {{ properties.verb }}!</div>',
        properties: [
          {
            label: 'Qualifier',
            name: 'qualifier',
            type: 'text',
            defaultValue: 'awesome',
            bond: 'expression'
          },
          {
            label: 'Verb',
            name: 'verb',
            type: 'text',
            defaultValue: 'rocks',
            bond: 'expression'
          }
        ],
        assets: []
      }
    ]);


})();
