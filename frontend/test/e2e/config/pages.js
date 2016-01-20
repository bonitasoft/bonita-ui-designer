(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .value('pages', [
      {
        id: 'person-page',
        name: 'Person',
        type: 'page',
        lastUpdate: 1447944407862,
        favorite: true,
        data: {
          alreadyExistsData: {type: 'constant', value: 'aValue'},
          jsonExample: {type: 'json', value: {}},
          urlExample: {type: 'url', value: 'https://api.github.com/users/jnizet'}
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
            'active': true,
            'external': true
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
      },
      {
        id: 'empty',
        name: 'empty',
        type: 'page',
        lastUpdate: 1447891242960,
        rows: [[]],
        data: {}
      },
      {
        id: 'emptyForm',
        name: 'emptyForm',
        type: 'form',
        lastUpdate: 1447891242000,
        rows: [[]],
        data: {}
      },
      {
        id: 'emptyLayout',
        name: 'emptyLayout',
        type: 'layout',
        lastUpdate: 1447891242444,
        rows: [[]],
        data: {}
      }
    ]
  );
})();
