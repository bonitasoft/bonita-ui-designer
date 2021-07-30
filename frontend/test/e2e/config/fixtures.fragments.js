(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .value('fragments', [{
      designerVersion: '1.5-SNAPSHOT',
      modelVersion: '2.2',
      id: 'personFragment',
      name: 'personFragment',
      type: 'fragment',
      favorite: true,
      lastUpdate: 1447943123163,
      rows: [
        [{
          type: 'component',
          dimension: {
            lg: 12,
            md: 12,
            sm: 12,
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
              value: 'Person'
            },
            alignment: {
              type: 'constant',
              value: 'center'
            }
          },
          reference: '30d7b19d-00c1-4a26-a32d-2ab34a1e50ef',
          id: 'pbParagraph'
        },
        {
          type: 'component',
          dimension: {
            lg: 12,
            md: 12,
            sm: 12,
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
              value: 'Person'
            },
            alignment: {
              type: 'constant',
              value: 'center'
            }
          },
          reference: '5779fb62-a995-45b8-871a-53e52af98c6b',
          id: 'customAwesomeWidget'
        }

        ]
      ],
      variables: {
        user: {
          type: 'constant',
          displayValue: null,
          exposed: true
        },
        admin: {
          type: 'constant',
          displayValue: null,
          exposed: false
        }
      },
      assets: [
        {
          'id': '5fb991a8-6e91-4ef2-ab48-baba98f305eb',
          'name': 'awesome-gif.gif',
          'type': 'img',
          'componentId': 'customAwesomeWidget',
          'scope': 'widget',
          'order': 1,
          'active': true,
          'external': false
        },
        {
          'id': '5fb991a8-6e91-4ef2-ab48-baba98f305eb',
          'name': 'https://awesome.cdn.com/cool.js',
          'type': 'js',
          'componentId': 'customAwesomeWidget',
          'scope': 'widget',
          'order': 1,
          'active': true,
          'external': false,
        }]
    }, {
      designerVersion: '1.5-SNAPSHOT',
      modelVersion: '2.2',
      id: 'empty',
      name: 'empty',
      type: 'fragment',
      lastUpdate: 1430239099006,
      rows: [[]],
      variables: {},
      assets: []
    },
    {
      designerVersion: '1.2.9-SNAPSHOT',
      modelVersion: '2.2',
      favorite: false,
      id: 'fragWithTitleFrag',
      name: 'fragWithTitleFrag',
      type: 'fragment',
      lastUpdate: 1461167681398,
      rows: [
        [{
          'type': 'component',
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
            },
            'text': {
              'type': 'interpolation',
              'value': 'Bonita'
            },
            'level': {
              'type': 'constant',
              'value': 'Level 2'
            },
            'alignment': {
              'type': 'constant',
              'value': 'left'
            }
          },
          'reference': '06d43b38-6afa-4814-9127-3c4547d40fd4',
          'id': 'pbTitle'
        }],
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
          'reference': '31d7fa2e-d606-4807-a653-2e4e56dec1ad',
          'id': 'simpleFragment',
          'binding': {
            'title': ''
          }
        }]
      ],
      assets: [],
      inactiveAssets: [],
      variables: {}
    }, {
      designerVersion: '1.2.9-SNAPSHOT',
      modelVersion: '2.2',
      favorite: false,
      id: 'simpleFragment',
      name: 'simpleFragment',
      type: 'fragment',
      lastUpdate: 1461166991515,
      rows: [
        [{
          'type': 'component',
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
            },
            'text': {
              'type': 'interpolation',
              'value': '{{title}}'
            },
            'level': {
              'type': 'constant',
              'value': 'Level 2'
            },
            'alignment': {
              'type': 'constant',
              'value': 'left'
            }
          },
          'reference': 'aadf2568-712a-41c1-9b14-102922dca1ce',
          'id': 'pbTitle'
        }]
      ],
      assets: [],
      inactiveAssets: [],
      variables: {
        'title': {
          'type': 'constant',
          'exposed': true
        }
      }
    }]
    );
})();
