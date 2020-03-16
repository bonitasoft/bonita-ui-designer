export default [{
  'id': 'pbInput',
  'name': 'Input',
  'custom': true,
  'properties': [{
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
},
  {
  'id': 'pbAutocomplete',
  'name': 'Autocomplete',
  'custom': false,
  'properties': [{
    'label': 'Read-only',
    'name': 'readOnly',
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
    'label': 'label Hidden',
    'name': 'labelHidden',
    'type': 'boolean',
    'defaultValue': false,
    'bond': 'constant'
  }, {
    'label': 'Label',
    'name': 'label',
    'showFor': 'properties.labelHidden.value === false',
    'type': 'text',
    'defaultValue': 'Default name',
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
    'label': 'Available values',
    'name': 'availableValues',
    'type': 'collection',
    'defaultValue': ['London', 'Paris', 'San Francisco'],
    'bond': 'expression'
  }, {
    'label': 'Displayed key',
    'name': 'displayedKey',
    'help': 'Object key to display',
    'type': 'text',
    'bond': 'constant'
  }, {
    'label': 'Value',
    'name': 'value',
    'type': 'text',
    'bond': 'variable'
  }],
  'type': 'widget'
},
  {
    'id': 'pbTabContainer',
    'name': 'TabContainer',
    'custom': false,
    'properties': [],
    'type': 'widget'
  }
  ];
