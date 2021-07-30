(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .value('pages', [
      {
        id: 'personPage',
        name: 'Person',
        type: 'page',
        modelVersion: '2.2',
        lastUpdate: 1447944407862,
        favorite: true,
        variables: {
          jsonExample: {type: 'json', displayValue: '{}'},
          urlExample: {type: 'url', displayValue: 'https://api.github.com/users/jnizet'},
          alreadyExistsData: {type: 'constant', displayValue: 'aValue'}
        },
        assets: [
          {
            'id': '7caa5385-e622-4202-a2e2-f07088f81854',
            'name': 'localization.json',
            'type': 'json',
            'order': 0,
            'active': true,
            'scope': 'page'
          },
          {
            'id': '9b34734c-5cc0-441e-a2ba-a52e1b7eb1e3',
            'name': 'myWidgetStyle.css',
            'type': 'css',
            'order': 1,
            'active': true,
            'external': false,
            'componentId': 'customWidget',
            'scope': 'widget'
          },
          {
            'id': '5aa4fe10-bd31-44e6-a5e6-39fc8a961691',
            'name': 'https://github.myfile.js',
            'type': 'js',
            'order': 2,
            'active': true,
            'external': true,
            'scope': 'page'
          },
          {
            'id': '0401a807-db07-4204-af8b-340078e6ee46',
            'name': 'protractor.png',
            'type': 'img',
            'order': 3,
            'active': true,
            'external': false,
            'scope': 'page'
          },
          {
            'id': '79555334-8f48-4f43-9291-0c82b6c94b1b',
            'name': 'myStyle.css',
            'type': 'css',
            'order': 4,
            'active': true,
            'external': false,
            'scope': 'page'
          }
        ],
        rows: [
          [{
            id: 'pbContainer',
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
            id: 'pbContainer',
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
      },
      {
        id: 'empty',
        name: 'empty',
        type: 'page',
        modelVersion: '2.2',
        lastUpdate: 1447891242960,
        rows: [[]],
        variables: {}
      },
      {
        id: 'emptyForm',
        name: 'emptyForm',
        type: 'form',
        lastUpdate: 1447891242000,
        rows: [[]],
        variables: {}
      },
      {
        id: 'emptyLayout',
        name: 'emptyLayout',
        type: 'layout',
        lastUpdate: 1447891242444,
        rows: [[]],
        variables: {}
      },
      {
        id: 'aPageToMigrate',
        name: 'aPageToMigrate',
        type: 'page',
        status: { migration: true, compatible:true },
        lastUpdate: 1447791242963,
        rows: [[]],
        variables: {}
      },
      {
        id: 'aPageIncompatible',
        name: 'aPageIncompatible',
        type: 'page',
        modelVersion: '4.0',
        status: { migration: false, compatible:false },
        lastUpdate: 1447791242960,
        rows: [[]],
        variables: {}
      },
      {
        designerVersion: '1.2.9-SNAPSHOT',
        favorite: false,
        id: 'fragInFragInFragFragment',
        name: 'fragInFragInFrag',
        type: 'page',
        modelVersion: '2.2',
        lastUpdate: 1447944407850,
        rows: [
          [{
            'type': 'fragment',
            'dimension': {
              'xs': 12,
              'sm': 12,
              'md': 12,
              'lg': 12
            },
            'propertyValues': {
              'cssClasses': {
                'type': 'constant',
                'value': ''
              },
              'hidden': {
                'type': 'constant',
                'value': false
              }
            },
            'reference': '9907799f-2574-4652-885a-04d9abd1a801',
            'id': '002bdab8-d53f-4035-ad64-1e0635587cf0',
            'binding': {}
          }]
        ],
        assets: [{
          'id': '85c96008-3807-4e31-9a5d-13bf1ed0b2d8',
          'name': 'localization.json',
          'type': 'json',
          'scope': 'page',
          'order': 0,
          'active': true,
          'external': false
        }],
        inactiveAssets: [],
        variables: { }
      }
    ]
    );
})();
