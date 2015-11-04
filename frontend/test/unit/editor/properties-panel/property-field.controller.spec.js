describe('widget property field controller', function() {
  var $scope, $rootScope, createController, controller;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.properties-panel'));
  beforeEach(inject(function(_$rootScope_, $controller) {
    $rootScope = _$rootScope_;

    $scope = $rootScope.$new();
    $scope.propertyValue = {
      type: 'constant'
    };
    $scope.property = { label: 'aLabel', name: 'aName' };

    createController = function($scope) {
      return $controller('PropertyFieldDirectiveCtrl', {
        $scope: $scope
      });
    };
    controller = createController($scope);
    $scope.$apply();
  }));

  it('should return the list of dataNames', function() {
    $scope.pageData = {
      jeanne: {},
      robert: {}
    };
    var names = $scope.getDataNames();
    expect(Array.isArray(names)).toBe(true);

    expect(names.length).toBe(2);
    expect(names[0]).toBe('jeanne');
    expect(names[1]).toBe('robert');
  });

  it('should create a constant property value when there is none for an expression bond', function() {
    delete $scope.propertyValue;
    $scope.property.bond = 'expression';

    createController($scope);

    expect($scope.propertyValue.type).toBe('constant');
  });

  it('should toggle the expression editor by changing parameter value type', function() {
    $scope.propertyValue.type = 'constant';

    controller.toggleExpressionEditor();
    expect($scope.propertyValue.type).toBe('expression');

    controller.toggleExpressionEditor();
    expect($scope.propertyValue.type).toBe('constant');
  });

  it('should store property value whenever we switch editor to come back to it', function() {
    $scope.propertyValue.type = 'constant';
    $scope.property.defaultValue = 'foo';

    controller.toggleExpressionEditor();
    expect($scope.propertyValue.value).toBeUndefined();

    $scope.propertyValue.value = 'bar';

    controller.toggleExpressionEditor();
    expect($scope.propertyValue.value).toBe('foo');

    controller.toggleExpressionEditor();
    expect($scope.propertyValue.value).toBe('bar');
  });

  it('should return true if the propertyValue is an expression', function() {
    $scope.propertyValue.type = 'expression';
    expect(controller.isExpression()).toBe(true);
  });

  it('should trigger a call to the right field template', function() {
    expect(controller.getFieldTemplate({
      type: 'integer'
    })).toBe('js/editor/properties-panel/field/integer.html');
  });

  it('should trigger a call to choice-grouped field template when type is choice and choice values are objects', function() {
    expect(controller.getFieldTemplate({
      type: 'choice',
      choiceValues: [{ group: 'aGroup', value: 'aValue' }]
    })).toBe('js/editor/properties-panel/field/choice-grouped.html');
  });

  it('should trigger a call to text field template if type is not supported', function() {
    expect(controller.getFieldTemplate({
      type: 'whatever'
    })).toBe('js/editor/properties-panel/field/text.html');
  });

  it('should trigger a call to the right bond template', function() {
    expect(controller.getBondTemplate({
      bond: 'interpolation'
    })).toBe('js/editor/properties-panel/bond/interpolation.html');
  });

  it('should return false if the propertyValue is an expression', function() {
    $scope.propertyValue.type = 'constant';
    expect(controller.isExpression()).toBe(false);
  });

  describe('check if we have a condition to display', function() {

    beforeEach(function() {
      $scope.property = {};
    });

    it('should return true if there is no condition', function() {
      expect($scope.isDisplayed()).toBe(true);
    });

    it('should true if the condition is valid', function() {
      $scope.property.showFor = 'properties.displayLabel.value === true';
      $scope.properties = {
        displayLabel: {
          value: true
        }
      };
      expect($scope.isDisplayed()).toBe(true);
    });

    it('should false if the condition is not valid', function() {
      $scope.property.showFor = 'properties.displayLabel.value === true';
      $scope.properties = {
        displayLabel: {
          value: false
        }
      };
      expect($scope.isDisplayed()).toBe(false);
    });
  });

});
