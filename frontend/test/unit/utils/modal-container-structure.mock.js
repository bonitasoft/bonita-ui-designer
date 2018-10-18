angular.module('modalContainerStructureMock', [])
  .value('modalContainerStructureMockJSON', {
    'data': {
      'id': 'person',
      'name': 'person',
      'lastUpdate': 1536756736934,
      'rows': [[{
        'type': 'modalContainer',
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
          'modalId': {
            'type': 'constant',
            'value': 'myModal'
          }
        },
        'reference': '3fcebbe4-d3fe-4f7d-9da6-d903a572024c',
        'id': 'pbModalContainer',
        'container': {
          'type': 'container',
          'dimension': {
            'xs': 12
          },
          'propertyValues': {},
          'reference': 'b200465b-954b-4a48-a189-b2747c577dcb',
          'id': 'pbContainer',
          'rows': [[{
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
              'required': {
                'type': 'constant',
                'value': false
              },
              'minLength': {
                'type': 'constant',
                'value': ''
              },
              'maxLength': {
                'type': 'constant',
                'value': ''
              },
              'readOnly': {
                'type': 'constant',
                'value': false
              },
              'labelHidden': {
                'type': 'constant',
                'value': false
              },
              'label': {
                'type': 'interpolation',
                'value': 'Default label'
              },
              'labelPosition': {
                'type': 'constant',
                'value': 'top'
              },
              'labelWidth': {
                'type': 'constant',
                'value': 4
              },
              'placeholder': {
                'type': 'interpolation'
              },
              'value': {
                'type': 'variable',
                'value': ''
              },
              'type': {
                'type': 'constant',
                'value': 'text'
              },
              'min': {
                'type': 'constant'
              },
              'max': {
                'type': 'constant'
              },
              'step': {
                'type': 'constant',
                'value': 1
              }
            },
            'reference': '8e4afd36-8e39-4ccd-8c84-0b82e9eea2c0',
            'id': 'pbInput'
          }]]
        }
      }]],
      'assets': [{
        'id': '04a17797-d79b-466c-a141-d4f69aacfc76',
        'name': 'style.css',
        'type': 'css',
        'order': 0,
        'external': false
      }, {
        'id': '2ea87377-19c8-428f-b466-3d0adf935fd7',
        'name': 'localization.json',
        'type': 'json',
        'order': 0,
        'external': false
      }],
      'inactiveAssets': [],
      'data': {},
      'uuid': '66e9bcc4-0b94-4f2e-9a50-a1003d4b0009',
      'type': 'page'
    }
  });
