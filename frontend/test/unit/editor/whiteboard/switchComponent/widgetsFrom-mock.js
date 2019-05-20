let widget = {
  'dimension': {'xs': 12, 'sm': 12, 'md': 12, 'lg': 12},
  'propertyValues': {
    'cssClasses': {'type': 'constant', 'value': '', 'label': 'CSS classes'},
    'hidden': {'type': 'constant', 'value': false, 'label': 'Hidden'},
    'required': {'type': 'constant', 'value': false, 'label': 'Required'},
    'minLength': {'type': 'constant', 'value': '', 'label': 'Value min length'},
    'maxLength': {'type': 'constant', 'value': '', 'label': 'Value max length'},
    'readOnly': {'type': 'constant', 'value': false, 'label': 'Read-only'},
    'labelHidden': {'type': 'constant', 'value': false, 'label': 'Label hidden'},
    'label': {'type': 'interpolation', 'value': 'Default label', 'label': 'Label'},
    'labelPosition': {'type': 'constant', 'value': 'top', 'label': 'Label position'},
    'labelWidth': {'type': 'constant', 'value': 4, 'label': 'Label width'},
    'placeholder': {'type': 'interpolation', 'label': 'Placeholder'},
    'value': {'type': 'variable', 'value': '', 'label': 'Value'},
    'type': {'type': 'constant', 'value': 'text', 'label': 'Type'},
    'min': {'type': 'constant', 'label': 'Min value'},
    'max': {'type': 'constant', 'label': 'Max value'},
    'step': {'type': 'constant', 'value': 1, 'label': 'Step'}
  },
  $$widget: {
    'designerVersion': '1.9.0-SNAPSHOT',
    'favorite': false,
    'id': 'pbInput',
    'name': 'Input',
    'lastUpdate': 1556868536130,
    'description': 'Field where the user can enter information',
    'custom': false,
    'order': 1,
    'properties': [{
      'label': 'CSS classes',
      'caption': 'Space-separated list',
      'name': 'cssClasses',
      'type': 'string',
      'defaultValue': '',
      'bond': 'expression',
      'help': 'Any accessible CSS classes. By default UI Designer comes with Bootstrap http://getbootstrap.com/css/#helper-classes'
    }, {
      'label': 'Hidden',
      'name': 'hidden',
      'type': 'boolean',
      'defaultValue': false,
      'bond': 'expression'
    }, {
      'label': 'Required',
      'name': 'required',
      'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
      'type': 'boolean',
      'defaultValue': false,
      'bond': 'expression'
    }, {
      'label': 'Value min length',
      'name': 'minLength',
      'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
      'type': 'integer',
      'defaultValue': '',
      'bond': 'expression',
      'constraints': {'min': '0'}
    }, {
      'label': 'Value max length',
      'name': 'maxLength',
      'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
      'type': 'integer',
      'defaultValue': '',
      'bond': 'expression',
      'constraints': {'min': '1'}
    }, {
      'label': 'Read-only',
      'name': 'readOnly',
      'type': 'boolean',
      'defaultValue': false,
      'bond': 'expression'
    }, {
      'label': 'Label hidden',
      'name': 'labelHidden',
      'type': 'boolean',
      'defaultValue': false,
      'bond': 'constant'
    }, {
      'label': 'Label',
      'name': 'label',
      'showFor': 'properties.labelHidden.value === false',
      'type': 'text',
      'defaultValue': 'Default label',
      'bond': 'interpolation'
    }, {
      'label': 'Label position',
      'name': 'labelPosition',
      'showFor': 'properties.labelHidden.value === false',
      'type': 'choice',
      'defaultValue': 'top',
      'choiceValues': ['left', 'top'],
      'bond': 'constant'
    }, {
      'label': 'Label width',
      'name': 'labelWidth',
      'showFor': 'properties.labelHidden.value === false',
      'type': 'integer',
      'defaultValue': 4,
      'bond': 'constant',
      'constraints': {'min': '1', 'max': '12'}
    }, {
      'label': 'Placeholder',
      'name': 'placeholder',
      'help': 'Short hint that describes the expected value',
      'type': 'text',
      'bond': 'interpolation'
    }, {
      'label': 'Value',
      'name': 'value',
      'caption': 'Any variable: <i>myData</i> or <i>myData.attribute</i>',
      'help': 'Read-write binding, initialized or updated by users\' input (bi-directional bond)',
      'type': 'text',
      'bond': 'variable'
    }, {
      'label': 'Type',
      'name': 'type',
      'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
      'type': 'choice',
      'defaultValue': 'text',
      'choiceValues': ['text', 'number', 'email', 'password'],
      'bond': 'constant'
    }, {
      'label': 'Min value',
      'name': 'min',
      'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
      'type': 'integer',
      'bond': 'expression'
    }, {
      'label': 'Max value',
      'name': 'max',
      'help': 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
      'type': 'integer',
      'bond': 'expression'
    }, {
      'label': 'Step',
      'name': 'step',
      'help': 'Specifies the legal number intervals between values',
      'type': 'integer',
      'defaultValue': 1,
      'bond': 'expression'
    }],
    'type': 'widget'
  }
};
export default widget;
