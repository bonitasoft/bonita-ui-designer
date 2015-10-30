describe('input with label', function() {
  var $compile, $rootScope, element, directiveScope;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    var template = '<div class="row" ng-switch="propertyValues.labelPosition">' +
      '<label ng-switch-when="left" class="widget-label-horizontal col-xs-{{ propertyValues.labelWidth }}">{{ propertyValues.label }}</label>' +
      '<label ng-switch-when="top" class="col-xs-12">{{ propertyValues.label }}</label>' +
      '<div ng-switch-when="left" class="col-xs-{{ 12 - propertyValues.labelWidth }}"><input type="text" class="form-control" placeholder="{{ propertyValues.placeholder }} ({{ propertyValues.required ? \'required\' : \'not required\' }})"></div>' +
      '<div ng-switch-when="top" class="col-xs-12"><input type="text" class="form-control" placeholder="{{ propertyValues.placeholder }} ({{ propertyValues.required ? \'required\' : \'not required\' }})"></div>' +
      '</div>';

    // when compiling with an input
    element = $compile(template)($rootScope);
    directiveScope = element.isolateScope();
    $rootScope.$digest();
  }));

  it('should display the input with label on the left', function() {
    $rootScope.propertyValues = { labelPosition: 'left', labelWidth: 4 };
    $rootScope.$apply();

    expect(element.find('label').hasClass('widget-label-horizontal col-xs-4')).toBeTruthy();
    expect(element.find('div').hasClass('col-xs-8')).toBeTruthy();
  });

  it('should display the input with label on the left with a different width', function() {
    $rootScope.propertyValues = { labelPosition: 'left', labelWidth: 6 };
    $rootScope.$apply();

    // then we should have
    expect(element.find('label').hasClass('col-xs-6')).toBeTruthy();
    expect(element.find('div').hasClass('col-xs-6')).toBeTruthy();
  });

  it('should display the input with label on the left with a different label', function() {
    $rootScope.propertyValues = { label: 'Last name', labelPosition: 'left', labelWidth: 6 };
    $rootScope.$apply();

    // then we should have
    expect(element.find('label').html()).toContain('Last name');
  });

  it('should display the input with label on the top', function() {
    $rootScope.propertyValues = { labelPosition: 'top', labelWidth: 6 };
    $rootScope.$apply();

    // then we should have
    expect(element.find('label').hasClass('widget-label-horizontal col-xs-4')).toBeFalsy();
    expect(element.find('label').hasClass('col-xs-12')).toBeTruthy();
    expect(element.find('div').html()).toContain('<input type="text" class="form-control" placeholder=" (not required)">');
  });
});
