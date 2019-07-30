(function () {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .value('widgets', [
      {
        designerVersion: '1.5-SNAPSHOT',
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
        designerVersion: '1.5-SNAPSHOT',
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
      },
      {
        id: 'pbDatePicker',
        name: 'Date Picker',
        lastUpdate: 1554292993458,
        template: '<div ng-class=\"{\n    \"form-horizontal\": properties.labelPosition === \"left\" && !properties.labelHidden,\n    \"row\": properties.labelPosition === \"top\" && !properties.labelHidden || properties.labelHidden\n    }\">\n    <div class=\"form-group\">\n        <label\n            ng-if=\"!properties.labelHidden\"\n            ng-class=\"{ \"control-label--required\": properties.required }\"\n            class=\"control-label col-xs-{{ !properties.labelHidden && properties.labelPosition === \"left\" ? properties.labelWidth : 12 }}\">\n            {{ properties.label | uiTranslate }}\n        </label>\n\n        <div\n            class=\"col-xs-{{ 12 - (!properties.labelHidden && properties.labelPosition === \"left\" ? properties.labelWidth : 0) }}\">\n            <p class=\"input-group\">\n                <input class=\"form-control\"\n                       name=\"{{ctrl.name}}\"\n                       type=\"text\"\n                       placeholder=\"{{properties.placeholder | uiTranslate}}\"\n                       ng-model=\"properties.value\"\n                       ng-readonly=\"properties.readOnly\"\n                       ng-required=\"properties.required\"\n                       bs-datepicker\n                       data-container=\"body\"\n                       data-autoclose=\"1\"\n                       data-timezone=\"UTC\"\n                       date-format=\"{{properties.dateFormat | uiTranslate}}\"\n                       data-trigger=\"focus\"\n                       data-start-week=\"{{ctrl.firstDayOfWeek}}\">\n\n                <span class=\"input-group-btn\">\n                    <button ng-if=\"properties.showToday\"\n                            type=\"button\"\n                            class=\"btn btn-default today\n                                {{$form[ctrl.name].$dirty && ($form[ctrl.name].$error.date || $form[ctrl.name].$error.parse ||\n                                (properties.required && $form[ctrl.name].$error.required)) ? \"btn-invalid\":\"\"}}\"\n                            ng-click=\"ctrl.setDateToToday()\"\n                            ng-disabled=\"properties.readOnly\" ui-translate>\n                        {{properties.todayLabel || \"Today\" | uiTranslate}}\n                    </button>\n                    <button type=\"button\"\n                            class=\"btn btn-default calendar\n                               {{$form[ctrl.name].$dirty && ($form[ctrl.name].$error.date || $form[ctrl.name].$error.parse ||\n                               (properties.required && $form[ctrl.name].$error.required)) ? \"btn-invalid\":\"\"}}\"\n                            ng-click=\"ctrl.openDatePicker()\"\n                            ng-disabled=\"properties.readOnly\">\n                        <i class=\"glyphicon glyphicon-calendar\"></i>\n                    </button>\n                </span>\n            </p>\n            <div ng-messages=\"$form[ctrl.name].$dirty && $form[ctrl.name].$error \"\n                 ng-messages-include=\"forms-generic-errors.html\" role=\"alert\">\n                <div ng-message=\"parse\" ng-if=\"!environment || !environment.editor\" class=\"text-danger\">\n                    {{ \"This is not a valid date\" | uiTranslate }}\n                </div>\n            </div>\n        </div>\n    </div>\n</div>\n\"',
        icon: '<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0.000000 0.061756 50.000000 20.000000\"><path fill=\"none\" d=\"M0 .062h50v20H0z\"/><path d=\"M0 .062v20h50v-20zm49 19H1v-18h48z\" fill=\"#fff\"/><g fill=\"#fff\"><path d=\"M20.152 1.864c-.246.143-.308.28-.328.69l-.016.354-.672.02c-.67.018-.67.018-.87.126-.35.193-.538.482-.604.94-.023.164-.032 2.29-.026 6.804l.01 6.564.075.202c.152.395.522.688.964.762.13.02 2.545.03 6.446.024l6.24-.01.19-.08c.23-.102.477-.308.608-.51.21-.322.2-.08.197-5.346 0-2.63-.014-5.638-.026-6.686l-.02-1.902-.095-.206c-.122-.262-.285-.437-.53-.566-.188-.098-.207-.098-.866-.12l-.672-.016-.016-.37c-.017-.335-.024-.38-.11-.496-.283-.402-.91-.346-1.13.098-.027.063-.057.237-.067.436l-.02.332h-7.663l-.017-.367c-.01-.2-.035-.397-.056-.435-.164-.31-.616-.43-.927-.24zm10.955 10.313v4.975l-6.12.01c-4.47.004-6.132-.002-6.15-.03-.022-.046-.032-9.79-.008-9.888.015-.056.33-.06 6.146-.052l6.13.01z\"/><path d=\"M21.842 7.557l-.317.014.01.74.01.74.716.012c.48.003.732-.004.754-.028.024-.025.036-.293.036-.754v-.713h-.252c-.138 0-.337-.007-.442-.01-.105-.007-.338-.007-.514 0zM24.06 7.557l-.203.014-.017.182c-.016.137-.01 1.083.01 1.292 0 .014.337.02.75.018l.743-.01.01-.744.01-.74h-.354c-.198 0-.447-.008-.55-.015-.106-.007-.286-.003-.394.004zM26.247 7.56l-.104.01.01.74.01.74.742.012.744.007.01-.35c.008-.19 0-.53-.01-.758l-.018-.408-.638-.004c-.35 0-.688.004-.747.01zM28.48 7.585c-.01.02-.01.36-.01.75l.01.716.51.012c.277.007.615 0 .746-.01l.235-.025v-1.46l-.136-.003c-.672-.02-1.346-.01-1.356.02zM19.226 10.22c-.01.21-.01.56 0 .77l.016.383.74-.007.74-.01V9.853l-.74-.01-.74-.007zM21.538 9.882c-.01.025-.01.367-.01.758l.01.715.74.01.74.008.023-.314c.015-.176.015-.52 0-.77l-.022-.453h-.734c-.563 0-.736.01-.75.045zM23.876 10.22c-.01.21-.01.56 0 .77l.017.383.74-.007.74-.01V9.853l-.74-.01-.74-.007zM26.19 9.882c-.01.025-.01.367-.01.758l.01.715.74.01.74.008.022-.314c.014-.176.014-.52 0-.77l-.023-.453h-.735c-.563 0-.737.01-.75.045zM28.46 10.22c-.008.21-.008.56 0 .77l.018.383.74-.007.74-.01V9.853l-.74-.01-.74-.007zM19.226 12.34v.726c0 .286.01.545.01.566 0 .035.163.045.713.045.39 0 .73-.014.752-.028.03-.025.04-.2.033-.762l-.01-.73-.743-.01-.747-.007zM21.548 12.165c-.014.01-.024.35-.024.748 0 .726 0 .73.076.747.04.01.38.014.753.01l.684-.01.01-.454c.01-.252.01-.59-.01-.76l-.02-.305h-.726c-.4 0-.736.012-.746.025zM23.876 12.34v.726c0 .286.01.545.01.566 0 .035.164.045.714.045.39 0 .73-.014.753-.028.03-.025.04-.2.033-.762l-.01-.73-.743-.01-.747-.007zM26.198 12.165c-.013.01-.023.35-.023.748 0 .726 0 .73.075.747.04.01.38.014.754.01l.684-.01.01-.454c.01-.252.01-.59-.01-.76l-.02-.305h-.726c-.4 0-.737.012-.747.025zM28.46 12.34v.726c0 .286.01.545.01.566 0 .035.165.045.715.045.39 0 .73-.014.753-.028.03-.025.04-.2.033-.762l-.008-.73-.744-.01-.747-.007zM19.23 14.522c0 .045-.01.384-.01.754v.67h1.522l-.01-.743-.01-.74-.743-.01-.747-.008zM21.525 15.196v.75H23.048l.01-.656c0-.36 0-.695-.01-.75l-.02-.095h-1.5zM23.88 14.522c0 .045-.01.384-.01.754v.67H25.393l-.01-.743-.01-.74-.743-.01-.747-.008zM26.175 15.196v.75H27.698l.01-.656c0-.36 0-.695-.01-.75l-.02-.095h-1.5zM28.465 14.522c0 .045-.01.384-.01.754v.67H29.978l-.01-.743-.01-.74-.743-.01-.747-.008z\"/></g></svg>',
        controller: 'function PbDatePickerCtrl($scope, $log, widgetNameFactory, $element, $locale, $bsDatepicker) {\n\n  \"rause strict\";\n\n  this.name = widgetNameFactory.getName(\"pbDatepicker\");\n  this.firstDayOfWeek = ($locale && $locale.DATETIME_FORMATS && $locale.DATETIME_FORMATS.FIRSTDAYOFWEEK) || 0;\n\n  $bsDatepicker.defaults.keyboard = false;\n\n  this.setDateToToday = function() {\n    var today = new Date();\n    if(today.getDay() !== today.getUTCDay()) {\n      //we need to add this offset for the displayed date to be correct\n      if(today.getTimezoneOffset() > 0) {\n        today.setTime(today.getTime() - 1440 * 60 * 1000);\n      } else if(today.getTimezoneOffset() < 0) {\n        today.setTime(today.getTime() + 1440 * 60 * 1000);\n      }\n    }\n    today.setUTCHours(0);\n    today.setUTCMinutes(0);\n    today.setUTCSeconds(0);\n    today.setUTCMilliseconds(0);\n    $scope.properties.value = today;\n  };\n\n  this.openDatePicker = function () {\n    $element.find(\"input\")[0].focus();\n  };\n\n\n  if (!$scope.properties.isBound(\"value\")) {\n    $log.error(\"the pbDatepicker property named \"value\" need to be bound to a variable\");\n  }\n\n\n}\n',
        description: 'Calendar for selecting a date (only)',
        custom: false,
        order: 10,
        properties: [{
          label: 'Read-only',


          name: 'readOnly',
          type: 'boolean',
          defaultValue: false,
          bond: 'expression'
        }, {
          label: 'Required',
          name: 'required',
          help: 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
          type: 'boolean',
          defaultValue: false,
          bond: 'expression'
        }, {
          label: 'Label hidden',
          name: 'labelHidden',
          type: 'boolean',
          defaultValue: false,
          bond: 'constant'
        }, {
          label: 'Label',
          name: 'label',
          showFor: 'properties.labelHidden.value === false',
          type: 'text',
          defaultValue: 'Date',
          bond: 'interpolation'
        }, {
          label: 'Label position',
          name: 'labelPosition',
          showFor: 'properties.labelHidden.value === false',
          type: 'choice',
          defaultValue: 'top',
          choiceValues: ['left', 'top'],
          bond: 'constant'
        }, {
          label: 'Label width',
          name: 'labelWidth',
          showFor: 'properties.labelHidden.value === false',
          type: 'integer',
          defaultValue: 4,
          bond: 'constant',
          constraints: {
            min: '1',
            max: '12'
          }
        }, {
          label: 'Value',
          name: 'value',
          caption: 'Input: <a href=\"https://en.wikipedia.org/wiki/ISO_8601\" target=\"_blank\">ISO 8601</a> formatted String (yyyy-MM-dd), Date object or Long number <br>Output: Date object (see widget help)',
          type: 'text',
          bond: 'variable'
        }, {
          label: 'Technical date format',
          name: 'dateFormat',
          caption: 'Use the characters: M (month), d (day), y (year)',
          type: 'text',
          defaultValue: 'MM/dd/yyyy',
          bond: 'expression'
        }, {
          label: 'Date Placeholder',
          name: 'placeholder',
          caption: 'Includes the localized version of the Technical date format property, to guide the user',
          help: 'Short hint that describes the expected input format for the date',
          type: 'text',
          defaultValue: 'Enter a date (mm/dd/yyyy)',
          bond: 'interpolation'
        }, {
          label: 'Show Today button',
          name: 'showToday',
          help: 'Display or hide the shortcut button to current day',
          type: 'boolean',
          defaultValue: true,
          bond: 'constant'
        }, {
          label: 'Today button label',
          name: 'todayLabel',
          help: 'Can be translated in subscription editions using the asset localization.json',
          showFor: 'properties.showToday.value === true',
          type: 'text',
          defaultValue: 'Today',
          bond: 'interpolation'
        }],
        assets: [{
          name: 'angular-strap-2.3.9-patched-dateTimePicker-setValidity.compat.min.js',
          type: 'js',
          order: 1,
          external: false
        }, {
          name: 'angular-strap-2.3.9-patched-dateTimePicker-setValidity.tpl.min.js',
          type: 'js',
          order: 2,
          external: false
        }],
        requiredModules: ['mgcrea.ngStrap.datepicker'],
        type: 'widget',
        hasHelp: true
      },
      {
        'designerVersion': '1.5-SNAPSHOT',
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
        'designerVersion': '1.10-SNAPSHOT',
        'id': 'pbTabsContainer',
        'name': 'Tabs container',
        'type': 'container',
        'order': -1,
        'description': 'Multiple groups of widgets, each group in a tab',
        'template': '',
        'custom': false,
        'icon': '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#fff" d="M50,11.4V8.6h-1v2.9H50z M50,16.2v-2.9h-1v2.9H50z M49,20h1v-1.9h-1v1V20z M44.1,20h2.9v-1h-2.9V20zM39.2,20h2.9v-1h-2.9V20z M34.3,20h2.9v-1h-2.9V20z M29.4,20h2.9v-1h-2.9V20z M24.5,20h2.9v-1h-2.9V20z M19.6,20h2.9v-1h-2.9V20zM14.7,20h2.9v-1h-2.9V20z M9.8,20h2.9v-1H9.8V20z M4.9,20h2.9v-1H4.9V20z M0,18.1V20h2.9v-1H1v-1H0z M0,13.3v2.9h1v-2.9H0zM0,8.6v2.9h1V8.6H0z M0,1v5.7h1V1H0z M15.7,0H1v1h14.7V0z M16.7,1h-1v4.8h2v-1h-1V1z M22.5,4.8h-2.9v1h2.9V4.8z M27.5,4.8h-2.9v1h2.9V4.8z M32.4,4.8h-2.9v1h2.9V4.8z M37.3,4.8h-2.9v1h2.9V4.8z M42.2,4.8h-2.9v1h2.9V4.8z M47.1,4.8h-2.9v1h2.9V4.8z M50,4.8h-1v1v1h1V4.8z"/><path fill="#CBD5E1" d="M34.3,1h-1v3.8h1V1z M18.6,1h14.7V0H18.6V1z M18.6,4.8V1h-1v3.8H18.6z"/></svg>',
        'properties': [
          {
            'label': 'Vertical Display',
            'name': 'vertical',
            'type': 'boolean',
            'defaultValue': false,
            'bond': 'constant'
          },
          {
            'label': 'Type',
            'name': 'type',
            'type': 'choice',
            'help': 'Tab display',
            'defaultValue': 'tabs',
            'choiceValues': ['tabs', 'pills'],
            'bond': 'constant'
          }
        ]
      },
      {
        'designerVersion': '1.10-SNAPSHOT',
        'id': 'pbTabContainer',
        'name': 'Tab container',
        'type': 'container',
        'order': -2,
        'description': 'Group of widgets, inside a Tabs container',
        'template': '',
        'requiredModules': ['ui.bootstrap.tabs', 'ui.bootstrap.tpls'],
        'icon': '<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 50 20\"><path fill=\"#fff\" d=\"M50,11.4V8.6h-1v2.9H50z M50,16.2v-2.9h-1v2.9H50z M49,20h1v-1.9h-1v1V20z M44.1,20h2.9v-1h-2.9V20zM39.2,20h2.9v-1h-2.9V20z M34.3,20h2.9v-1h-2.9V20z M29.4,20h2.9v-1h-2.9V20z M24.5,20h2.9v-1h-2.9V20z M19.6,20h2.9v-1h-2.9V20zM14.7,20h2.9v-1h-2.9V20z M9.8,20h2.9v-1H9.8V20z M4.9,20h2.9v-1H4.9V20z M0,18.1V20h2.9v-1H1v-1H0z M0,13.3v2.9h1v-2.9H0zM0,8.6v2.9h1V8.6H0z M0,1v5.7h1V1H0z M15.7,0H1v1h14.7V0z M16.7,1h-1v4.8h2v-1h-1V1z M22.5,4.8h-2.9v1h2.9V4.8z M27.5,4.8h-2.9v1h2.9V4.8z M32.4,4.8h-2.9v1h2.9V4.8z M37.3,4.8h-2.9v1h2.9V4.8z M42.2,4.8h-2.9v1h2.9V4.8z M47.1,4.8h-2.9v1h2.9V4.8z M50,4.8h-1v1v1h1V4.8z\"/><path fill=\"#CBD5E1\" d=\"M34.3,1h-1v3.8h1V1z M18.6,1h14.7V0H18.6V1z M18.6,4.8V1h-1v3.8H18.6z\"/></svg>',
        'properties': [
          {
            'label': 'Disabled',
            'name': 'disabled',
            'type': 'boolean',
            'defaultValue': false
          },
          {
            'label': 'Title',
            'name': 'title',
            'type': 'text',
            'defaultValue': 'Tab x',
            'bond': 'interpolation'
          }
        ]
      },
      {
        'custom':
          false,
        'id':
          'pbFormContainer',
        'type':
          'container',
        'template':
          '',
        'icon':
          '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#fff" d="M5 19h3v1H5v-1zm5-18h3V0h-3v1zM5 1h3V0H5v1zm5 19h3v-1h-3v1zm5 0h3v-1h-3v1zm5 0h3v-1h-3v1zM15 1h3V0h-3v1zM1 9H0v2h1V9zM0 2h1V1h2V0H0v2zm23-2h-3v1h3V0zM1 4H0v3h1V4zm0 14H0v2h3v-1H1v-1zm0-5H0v3h1v-3zM27 0h-2v1h2V0zm22 19v1h1v-1.9h-1v.9zm-24 1h2v-1h-2v1zM44 1h3V0h-3v1zm5 15h1v-3h-1v3zm0-16v2h1V0h-1zm0 7h1V4h-1v3zm0 4h1V9h-1v2zm-5 9h3v-1h-3v1zM39 1h3V0h-3v1zM29 1h3V0h-3v1zm0 19h3v-1h-3v1zm5 0h3v-1h-3v1zm0-19h3V0h-3v1zm5 19h3v-1h-3v1zm-9-6H18v4h12v-4z"/><path fill="#CBD5E1" d="M45 2v4H18V2h27zM18 12h27V8H18v4zm4 5h4v-2h-4v2zM7 5h9V3H7v2zm-2 6h7V9H5v2zm8 0h3V9h-3v2z"/></svg>',
        'name':
          'Form container',
        'order':
          0,
        'description':
          'Container used for a form. Eases validation',
        'properties':
          []
      }
    ])
  ;


})();
