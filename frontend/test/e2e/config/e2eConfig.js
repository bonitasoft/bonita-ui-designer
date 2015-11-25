//  Add e2e module to main pb module
angular.module('uidesigner').requires.push('bonitasoft.designer.e2e');

angular.module('bonitasoft.designer.e2e', ['ngMockE2E'])

  .config(function($compileProvider) {
    // the 1.3 optimization needs to be disable for the e2e tests
    $compileProvider.debugInfoEnabled(true);
  })

  .service('e2ehelper', function() {

    return {
      getById: function(array, elementId) {
        return array.filter(function(elem) {
          return elem.id === elementId;
        })[0];
      },
      lastChunk: function(url) {
        return url.match(/([^\/]*)\/*$/)[1];
      }
    };
  })

  .run(function($httpBackend, e2ehelper) {

    var widgets = [
      {
        designerVersion: '1.2.4-SNAPSHOT',
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
        designerVersion: '1.2.4-SNAPSHOT',
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
            'active': true
          }
        ]
      },
      {
        id: 'customFavoriteWidget',
        name: 'favoriteWidget',
        custom: true,
        lastUpdate: 1447891242960,
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
    ];

    var personPage = {
      id: 'person',
      name: 'Person',
      type: 'page',
      lastUpdate: 1447944407862,
      data: {
        alreadyExistsData: { type: 'constant', value: 'aValue' },
        jsonExample: { type: 'json', value: {} },
        urlExample: { type: 'url', value: 'https://api.github.com/users/jnizet' }
      },
      assets: [
        {
          'id': '9b34734c-5cc0-441e-a2ba-a52e1b7eb1e3',
          'name': 'myStyle.css',
          'type': 'css',
          'order': 1,
          'active': true,
          'componentId': 'customWidget'
        },
        {
          'id': '5aa4fe10-bd31-44e6-a5e6-39fc8a961691',
          'name': 'https://github.myfile.js',
          'type': 'js',
          'order': 2,
          'active': true
        },
        {
          'id': '0401a807-db07-4204-af8b-340078e6ee46',
          'name': 'protractor.png',
          'type': 'img',
          'order': 3,
          'active': true
        },
        {
          'id': '79555334-8f48-4f43-9291-0c82b6c94b1b',
          'name': 'myStyle.css',
          'type': 'css',
          'order': 4,
          'active': true
        }
      ],
      rows: [
        [{
          type: 'container',
          dimension: {
            xs: 4
          },
          propertyValues: {
            cssClasses: {
              type: 'constant',
              value: ''
            },
            hidden: {
              type: 'constant',
              value: false
            },
            repeatedCollection: {
              type: 'constant',
              value: ''
            }
          },
          rows: [
            [{
              type: 'component',
              id: 'pbParagraph',
              dimension: {
                xs: 4
              },
              propertyValues: {
                cssClasses: {
                  type: 'constant',
                  value: ''
                },
                hidden: {
                  type: 'constant',
                  value: false
                },
                text: {
                  type: 'constant',
                  value: 'First name'
                },
                alignment: {
                  type: 'constant',
                  value: 'left'
                }
              }
            }],
            [{
              type: 'component',
              id: 'pbInput',
              dimension: {
                xs: 4
              },
              propertyValues: {
                cssClasses: {
                  type: 'constant',
                  value: ''
                },
                hidden: {
                  type: 'constant',
                  value: false
                },
                readOnly: {
                  type: 'constant',
                  value: false
                },
                labelHidden: {
                  type: 'constant',
                  value: false
                },
                label: {
                  type: 'constant',
                  value: 'Default label'
                },
                labelPosition: {
                  type: 'constant',
                  value: 'left'
                },
                labelWidth: {
                  type: 'constant',
                  value: 4
                },
                placeholder: {
                  type: 'constant'
                },
                value: {
                  type: 'constant'
                },
                type: {
                  type: 'constant',
                  value: 'text'
                }
              }
            }]
          ]
        }, {
          type: 'container',
          dimension: {
            xs: 4
          },
          propertyValues: {
            cssClasses: {
              type: 'constant',
              value: ''
            },
            hidden: {
              type: 'constant',
              value: false
            },
            repeatedCollection: {
              type: 'constant',
              value: ''
            }
          },
          rows: [
            [{
              type: 'component',
              id: 'pbParagraph',
              dimension: {
                xs: 12
              },
              propertyValues: {
                cssClasses: {
                  type: 'constant',
                  value: ''
                },
                hidden: {
                  type: 'constant',
                  value: false
                },
                text: {
                  type: 'constant',
                  value: 'Last name'
                },
                alignment: {
                  type: 'constant',
                  value: 'left'
                }
              }
            }],
            [{
              type: 'component',
              id: 'pbInput',
              dimension: {
                xs: 12
              },
              propertyValues: {
                cssClasses: {
                  type: 'constant',
                  value: ''
                },
                hidden: {
                  type: 'constant',
                  value: false
                },
                readOnly: {
                  type: 'constant',
                  value: false
                },
                labelHidden: {
                  type: 'constant',
                  value: false
                },
                label: {
                  type: 'constant',
                  value: 'Default label'
                },
                labelPosition: {
                  type: 'constant',
                  value: 'left'
                },
                labelWidth: {
                  type: 'constant',
                  value: 4
                },
                placeholder: {
                  type: 'constant'
                },
                value: {
                  type: 'constant'
                },
                type: {
                  type: 'constant',
                  value: 'text'
                }
              }
            }]
          ]
        }, {
          type: 'component',
          id: 'pbParagraph',
          dimension: {
            xs: 4
          },
          propertyValues: {
            cssClasses: {
              type: 'constant',
              value: ''
            },
            hidden: {
              type: 'constant',
              value: false
            },
            text: {
              type: 'constant',
              value: 'First name'
            },
            alignment: {
              type: 'constant',
              value: 'left'
            }
          }
        }]
      ]
    };

    var pages = [
      personPage,
      {
        id: 'empty',
        name: 'empty',
        type: 'page',
        lastUpdate: 1447891242960,
        rows: [[]],
        data: {}
      }
    ];

    /********************************************************************************************************
     *                                          MISCELLANEOUS
     * ******************************************************************************************************/
    $httpBackend.whenGET(/^partials\//).passThrough();

    // I18n
    $httpBackend.whenGET(/i18n\/.*-fr-FR.json/).respond({ 'fr': { 'New page': 'Nouvelle page' } });

    /********************************************************************************************************
     *                                            WIDGETS
     * ******************************************************************************************************/
    // create new widget property
    $httpBackend.whenPOST(/rest\/widgets\/.*\/properties/).respond(function(method, url, data) {
      var property = angular.fromJson(data);
      var widgetId = url.match(/rest\/widgets\/(.*)\/properties/)[1];
      var widget = e2ehelper.getById(widgets, widgetId);
      widget.properties.push(property);
      return [200, widget.properties, {}];
    });

    // update widget property
    $httpBackend.whenPUT(/rest\/widgets\/.*\/properties\/.*/).respond(function(method, url, data) {
      var updatedProperty = angular.fromJson(data);
      var urlMatches = url.match(/rest\/widgets\/(.*)\/properties\/(.*)/);
      var widgetId = urlMatches[1];
      var propertyName = urlMatches[2];
      var widget = e2ehelper.getById(widgets, widgetId);
      widget.properties = widget.properties.map(function(property) {
        if (property.name === propertyName) {
          return updatedProperty;
        }
        return property;
      });
      return [200, widget.properties, {}];
    });

    // delete widget property
    $httpBackend.whenDELETE(/rest\/widgets\/.*\/properties\/.*/).respond(function(method, url) {
      var urlMatches = url.match(/rest\/widgets\/(.*)\/properties\/(.*)/);
      var widgetId = urlMatches[1];
      var propertyName = urlMatches[2];
      var widget = e2ehelper.getById(widgets, widgetId);
      widget.properties = widget.properties.filter(function(property) {
        return property.name !== propertyName;
      });
      return [200, widget.properties, {}];
    });

    // get all
    $httpBackend.whenGET('rest/widgets').respond(widgets);

    // get all light representation
    $httpBackend.whenGET('rest/widgets?view=light').respond(function() {
      var response = widgets.map(({id, name, custom, lastUpdate}) => ({id, name, custom, lastUpdate}));
      return [200, response, {}];
    });

    // get by id
    $httpBackend.whenGET(/rest\/widgets\/.*/).respond(function(method, url) {
      var widgetId = e2ehelper.lastChunk(url);
      var widget = e2ehelper.getById(widgets, widgetId);
      return [200, widget, {}];
    });

    // create new widget
    $httpBackend.whenPOST('rest/widgets').respond(function(method, url, data) {
      var widget = angular.fromJson(data);
      widget.id = 'custom' + widget.name;
      widget.assets = [];
      widgets.push(widget);
      return [200, widget, {}];
    });

    // duplicate widget
    $httpBackend.whenPOST('rest/widgets?duplicata=customAwesomeWidget').respond(function(method, url, data) {
      var widget = angular.fromJson(data);
      widget.id = 'custom' + widget.name;
      widgets.push(widget);
      return [200, widget, {}];
    });

    // update widget
    $httpBackend.whenPUT(/rest\/widgets\/.*/).respond(200);

    /********************************************************************************************************
     *                                            PAGES
     * ******************************************************************************************************/
    // get all (light representation)
    $httpBackend.whenGET('rest/pages').respond(function() {
      var response = pages.map(({id, name, type, lastUpdate}) => ({id, name, type, lastUpdate}));
      return [200, response, {}];
    });

    // get by id
    $httpBackend.whenGET(/rest\/pages\/.*/).respond(function(method, url) {
      var pageId = e2ehelper.lastChunk(url);
      var page = e2ehelper.getById(pages, pageId);
      return [200, page, {}];
    });

    // create new page
    $httpBackend.whenPOST('rest/pages?duplicata=person').respond(function(method, url, data) {
      var page = angular.fromJson(data);
      page.id = page.name;
      pages.push(page);
      return [201, page, {}];
    });

    // duplicate page
    $httpBackend.whenPOST('rest/pages').respond(function(method, url, data) {
      var page = angular.fromJson(data);
      page.id = page.name;
      pages.push(page);
      return [201, page, {}];
    });

    // update page
    $httpBackend.whenPUT(/rest\/pages\/.*/).respond(200);

    $httpBackend.whenDELETE('rest/pages/person').respond(200);

    /********************************************************************************************************
     *                                            EXPORT
     * ******************************************************************************************************/
    $httpBackend.whenGET('export/page/person').respond(200);

  });
