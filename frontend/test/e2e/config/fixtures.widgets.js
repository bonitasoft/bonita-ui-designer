(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .value('widgets', [
      {
        designerVersion: '1.5.25',
        id: 'pbParagraph',
        name: 'Paragraph',
        type: 'widget',
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
        designerVersion: '1.5.25',
        id: 'pbInput',
        name: 'Input',
        type: 'widget',
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
        type: 'widget',
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
          },
          {
            'id': '9b34734c-5cc0-441e-a2ba-a52e1b7eb1e3',
            'name': 'myStyle.css',
            'type': 'css',
            'order': 3,
            'active': true
          }
        ]
      },
      {
        id: 'customFavoriteWidget',
        name: 'favoriteWidget',
        type: 'widget',
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
      }, {
        'designerVersion': '1.5.25',
        'favorite': false,
        'id': 'pbTitle',
        'name': 'Title',
        'lastUpdate': 1462147621767,
        'template': '<h1 ng-if="\'Level 1\' === properties.level" class="text-{{ properties.alignment }}">{{properties.text | uiTranslate}}</h1>\n<h2 ng-if="\'Level 2\' === properties.level" class="text-{{ properties.alignment }}">{{properties.text | uiTranslate}}</h2>\n<h3 ng-if="\'Level 3\' === properties.level" class="text-{{ properties.alignment }}">{{properties.text | uiTranslate}}</h3>\n<h4 ng-if="\'Level 4\' === properties.level" class="text-{{ properties.alignment }}">{{properties.text | uiTranslate}}</h4>\n<h5 ng-if="\'Level 5\' === properties.level" class="text-{{ properties.alignment }}">{{properties.text | uiTranslate}}</h5>\n<h6 ng-if="\'Level 6\' === properties.level" class="text-{{ properties.alignment }}">{{properties.text | uiTranslate}}</h6>\n',
        'icon': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><g><path fill="#fff" d="M29.6,8H33v10h3V4h-2.3c-0.1,0.9-1.4,2-4.1,2l-0.4,0.1L29.6,8z M24,11v7h3V3h-3v6h-6V3h-3v15h3v-7H24z"/></g></svg>',
        'description': 'Text used to structure the page or form content',
        'custom': false,
        'order': 13,
        'properties': [{
          'label': 'Text',
          'name': 'text',
          'type': 'text',
          'defaultValue': 'Title',
          'bond': 'interpolation'
        }, {
          'label': 'Title level',
          'name': 'level',
          'type': 'choice',
          'defaultValue': 'Level 2',
          'choiceValues': ['Level 1', 'Level 2', 'Level 3', 'Level 4', 'Level 5', 'Level 6'],
          'bond': 'constant'
        }, {
          'label': 'Alignment',
          'name': 'alignment',
          'type': 'choice',
          'defaultValue': 'left',
          'choiceValues': ['left', 'center', 'right'],
          'bond': 'constant'
        }],
        'assets': [],
        'type': 'widget'
      }, {
        'id': 'pbContainer',
        'name': 'Container',
        'type': 'container',
        'template': '',
        'icon': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#fff" d="M50,6.7V3.8h-1v2.9H50z M50,11.4V8.6h-1v2.9H50z M50,16.2v-2.9h-1v2.9H50z M49,20h1v-1.9h-1v1V20zM44.1,20h2.9v-1h-2.9V20z M39.2,20h2.9v-1h-2.9V20z M34.3,20h2.9v-1h-2.9V20z M29.4,20h2.9v-1h-2.9V20z M24.5,20h2.9v-1h-2.9V20zM19.6,20h2.9v-1h-2.9V20z M14.7,20h2.9v-1h-2.9V20z M9.8,20h2.9v-1H9.8V20z M4.9,20h2.9v-1H4.9V20z M0,18.1V20h2.9v-1H1v-1H0zM0,13.3v2.9h1v-2.9H0z M0,8.6v2.9h1V8.6H0z M0,3.8v2.9h1V3.8H0z M2.9,0H0v1.9h1V1h2V0z M7.8,0H4.9v1h2.9V0z M12.7,0H9.8v1h2.9V0zM17.6,0h-2.9v1h2.9V0z M22.5,0h-2.9v1h2.9V0z M27.5,0h-2.9v1h2.9V0z M32.4,0h-2.9v1h2.9V0z M37.3,0h-2.9v1h2.9V0z M42.2,0h-2.9v1h2.9V0z M47.1,0h-2.9v1h2.9V0z M50,0h-1v1v1h1V0z"/></svg>',
        'description': 'Group of widgets used to define the arrangement of the page elements. Its content can be repeated over an array',
        'order': -2,
        'properties': [
          {
            'name': 'repeatedCollection',
            'label': 'Collection',
            'help': 'Number of array elements defines the number of times the container structure is repeated. Array data is available to widgets in the container',
            'caption': 'Repeat container content. Variable of type array',
            'type': 'text',
            'bond': 'variable'
          }
        ]
      }, {
        'id': 'pbTabsContainer',
        'name': 'Tabs container',
        'type': 'container',
        'order': -1,
        'description': 'Multiple groups of widgets, each group in a tab',
        'template': '',
        'custom': false,
        'icon': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#fff" d="M50,11.4V8.6h-1v2.9H50z M50,16.2v-2.9h-1v2.9H50z M49,20h1v-1.9h-1v1V20z M44.1,20h2.9v-1h-2.9V20zM39.2,20h2.9v-1h-2.9V20z M34.3,20h2.9v-1h-2.9V20z M29.4,20h2.9v-1h-2.9V20z M24.5,20h2.9v-1h-2.9V20z M19.6,20h2.9v-1h-2.9V20zM14.7,20h2.9v-1h-2.9V20z M9.8,20h2.9v-1H9.8V20z M4.9,20h2.9v-1H4.9V20z M0,18.1V20h2.9v-1H1v-1H0z M0,13.3v2.9h1v-2.9H0zM0,8.6v2.9h1V8.6H0z M0,1v5.7h1V1H0z M15.7,0H1v1h14.7V0z M16.7,1h-1v4.8h2v-1h-1V1z M22.5,4.8h-2.9v1h2.9V4.8z M27.5,4.8h-2.9v1h2.9V4.8z M32.4,4.8h-2.9v1h2.9V4.8z M37.3,4.8h-2.9v1h2.9V4.8z M42.2,4.8h-2.9v1h2.9V4.8z M47.1,4.8h-2.9v1h2.9V4.8z M50,4.8h-1v1v1h1V4.8z"/><path fill="#CBD5E1" d="M34.3,1h-1v3.8h1V1z M18.6,1h14.7V0H18.6V1z M18.6,4.8V1h-1v3.8H18.6z"/></svg>',
        'properties': []
      }, {
        'custom': false,
        'id': 'pbFormContainer',
        'type': 'container',
        'template': '',
        'icon': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#fff" d="M5 19h3v1H5v-1zm5-18h3V0h-3v1zM5 1h3V0H5v1zm5 19h3v-1h-3v1zm5 0h3v-1h-3v1zm5 0h3v-1h-3v1zM15 1h3V0h-3v1zM1 9H0v2h1V9zM0 2h1V1h2V0H0v2zm23-2h-3v1h3V0zM1 4H0v3h1V4zm0 14H0v2h3v-1H1v-1zm0-5H0v3h1v-3zM27 0h-2v1h2V0zm22 19v1h1v-1.9h-1v.9zm-24 1h2v-1h-2v1zM44 1h3V0h-3v1zm5 15h1v-3h-1v3zm0-16v2h1V0h-1zm0 7h1V4h-1v3zm0 4h1V9h-1v2zm-5 9h3v-1h-3v1zM39 1h3V0h-3v1zM29 1h3V0h-3v1zm0 19h3v-1h-3v1zm5 0h3v-1h-3v1zm0-19h3V0h-3v1zm5 19h3v-1h-3v1zm-9-6H18v4h12v-4z"/><path fill="#CBD5E1" d="M45 2v4H18V2h27zM18 12h27V8H18v4zm4 5h4v-2h-4v2zM7 5h9V3H7v2zm-2 6h7V9H5v2zm8 0h3V9h-3v2z"/></svg>',
        'name': 'Form container',
        'order': 0,
        'description': 'Container used for a form. Eases validation',
        'properties': []
      }
    ]);


})();
