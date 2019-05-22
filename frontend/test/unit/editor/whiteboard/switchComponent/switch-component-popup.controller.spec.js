import widgets from './widgets-mock';
import widget from './widgetsFrom-mock';

describe('switchComponentPopupController', function () {
  let $scope;
  let widgetRepo;
  let modalInstance;
  let controller;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard', 'bonitasoft.designer.common.repositories', 'mock.modal'));

  beforeEach(inject(function ($rootScope, $controller, _widgetRepo_, _$uibModalInstance_, $q, _properties_) {
    $scope = $rootScope.$new();

    widgetRepo = _widgetRepo_;
    spyOn(widgetRepo, 'all').and.returnValue($q.when(widgets));

    modalInstance = _$uibModalInstance_.create();
    controller = $controller('SwitchComponentPopupController', {
      $uibModalInstance: modalInstance,
      widgetFrom: widget,
      widgets: widgets,
      properties: _properties_,
      dictionary: {}
    });
  }));

  it('should contains widgets available without widgetFrom name when user open popup', function () {
    expect(controller.widgetsToDisplay).toEqual([{
      id: 'pbAutocomplete',
      name: 'Autocomplete',
      custom: false
    }]);
  });

  it('should format widgetFrom properties when user open popup', function () {
    expect(controller.propertiesFrom).toEqual({
      name: 'Input',
      options: Object({
        cssClasses: Object({type: 'constant', value: '', label: 'CSS classes'}),
        hidden: Object({type: 'constant', value: false, label: 'Hidden'}),
        required: Object({type: 'constant', value: false, label: 'Required'}),
        minLength: Object({type: 'constant', value: '', label: 'Value min length'}),
        maxLength: Object({type: 'constant', value: '', label: 'Value max length'}),
        readOnly: Object({type: 'constant', value: false, label: 'Read-only'}),
        labelHidden: Object({type: 'constant', value: false, label: 'Label hidden'}),
        label: Object({type: 'interpolation', value: 'Default label', label: 'Label'}),
        labelPosition: Object({type: 'constant', value: 'top', label: 'Label position'}),
        labelWidth: Object({type: 'constant', value: 4, label: 'Label width'}),
        placeholder: Object({type: 'interpolation', label: 'Placeholder'}),
        value: Object({type: 'variable', value: '', label: 'Value'}),
        type: Object({type: 'constant', value: 'text', label: 'Type'}),
        min: Object({type: 'constant', label: 'Min value'}),
        max: Object({type: 'constant', label: 'Max value'}),
        step: Object({type: 'constant', value: 1, label: 'Step'})
      })
    });

    it('should display by alphabetical sort widgets when user open popup', function () {
      expect(controller.widgetsToDisplay).toEqual([{
        id: 'pbAutocomplete',
        name: 'Autocomplete',
        custom: false
      }, {id: 'pbInput', name: 'Input', custom: true}]);
    });

  });

  it('should get widgetTo properties when user select a widget', function () {
    let expected = {
      id: 'pbInput',
      name: 'Input',
      options: {
        cssClasses: {
          label: 'CSS classes',
          caption: 'Space-separated list',
          name: 'cssClasses',
          type: 'string',
          defaultValue: '',
          bond: 'expression',
          help: 'Any accessible CSS classes. By default UI Designer comes with Bootstrap http://getbootstrap.com/css/#helper-classes'
        },
        hidden: {
          label: 'Hidden',
          name: 'hidden',
          type: 'boolean',
          defaultValue: false,
          bond: 'expression'
        },
        required: {
          label: 'Required',
          name: 'required',
          help: 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
          type: 'boolean',
          defaultValue: false,
          bond: 'expression'
        },
        minLength: {
          label: 'Value min length',
          name: 'minLength',
          help: 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
          type: 'integer',
          defaultValue: '',
          bond: 'expression',
          constraints: {min: '0'}
        },
        maxLength: {
          label: 'Value max length',
          name: 'maxLength',
          help: 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
          type: 'integer',
          defaultValue: '',
          bond: 'expression',
          constraints: {min: '1'}
        },
        readOnly: {
          label: 'Read-only',
          name: 'readOnly',
          type: 'boolean',
          defaultValue: false,
          bond: 'expression'
        },
        labelHidden: {
          label: 'Label hidden',
          name: 'labelHidden',
          type: 'boolean',
          defaultValue: false,
          bond: 'constant'
        },
        label: {
          label: 'Label',
          name: 'label',
          showFor: 'properties.labelHidden.value === false',
          type: 'text',
          defaultValue: 'Default label',
          bond: 'interpolation'
        },
        labelPosition: {
          label: 'Label position',
          name: 'labelPosition',
          showFor: 'properties.labelHidden.value === false',
          type: 'choice',
          defaultValue: 'top',
          choiceValues: ['left', 'top'],
          bond: 'constant'
        },
        labelWidth: {
          label: 'Label width',
          name: 'labelWidth',
          showFor: 'properties.labelHidden.value === false',
          type: 'integer',
          defaultValue: 4,
          bond: 'constant',
          constraints: {min: '1', max: '12'}
        },
        placeholder: {
          label: 'Placeholder',
          name: 'placeholder',
          help: 'Short hint that describes the expected value',
          type: 'text',
          bond: 'interpolation'
        },
        value: {label: 'Value', name: 'value', type: 'text', bond: 'variable'},
        type: {
          label: 'Type',
          name: 'type',
          help: 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
          type: 'choice',
          defaultValue: 'text',
          choiceValues: ['text', 'number', 'email', 'password'],
          bond: 'constant'
        },
        min: {
          label: 'Min value',
          name: 'min',
          help: 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
          type: 'integer',
          bond: 'expression'
        },
        max: {
          label: 'Max value',
          name: 'max',
          help: 'In the context of a form container, use $form.$invalid as a Boolean to check form validity in a widget property',
          type: 'integer',
          bond: 'expression'
        },
        step: {
          label: 'Step',
          name: 'step',
          help: 'Specifies the legal number intervals between values',
          type: 'integer',
          defaultValue: 1,
          bond: 'expression'
        }
      }
    };
    expect(controller.getWidget('pbInput')).toEqual(expected);
  });

  it('should display clear button when a widget is selected', function () {
    controller.selectedWidget = 'Input';
    expect(controller.isClearButtonVisible()).toBeTruthy();

    controller.clearValue();
    expect(controller.isClearButtonVisible()).toBeFalsy();
  });

  it('should disabled switch button in popup when user never click on show properties button', function () {
    controller.selectedWidget = 'autocomplete';
    expect(controller.propertiesIsDisplay).toBeFalsy();

    controller.showProperties();
    expect(controller.propertiesIsDisplay).toBeTruthy();
  });

});
