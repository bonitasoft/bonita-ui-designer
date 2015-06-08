describe('widget property field controller', function () {
  var $scope, rootScope, ctrl, $rootScope, createController;

  beforeEach(module('pb.directives'));
  beforeEach(inject(function (_$rootScope_, $controller) {
    $rootScope = _$rootScope_;

    $scope = $rootScope.$new();
    $scope.propertyValue = undefined;
    $scope.property = {label: 'aLabel', name: 'aName'};

    createController = function ($scope) {
      $controller('PropertyFieldDirectiveCtrl', {
        $scope: $scope
      });
    };
    createController($scope);
    $scope.$apply();
  }));

  it('should init property value if not already done', function () {
    expect($scope.propertyValue).toBeDefined();
  });

  it('should not be linked by default', function () {
    expect($scope.shouldBeLinked()).toBe(false);
  });

  it('should be linked when property value type is data', function () {
    $scope.propertyValue.type = 'data';

    expect($scope.shouldBeLinked()).toBe(true);
  });

  it('should link field to a data', function () {

    $scope.link();

    expect($scope.shouldBeLinked()).toBe(true);
    expect($scope.propertyValue.type).toBe('data');
  });

  it('should unlink field from data', function () {

    $scope.unlink();

    expect($scope.shouldBeLinked()).toBe(false);
    expect($scope.propertyValue.type).toBe('constant');
  });

  it('should set old linked value to property value when unlicking field', function () {
    $scope.propertyValue = {value: 'aValue', type: 'constant'};

    $scope.link();
    $scope.unlink();

    expect($scope.propertyValue.value).toBe('aValue');
  });

  it('should set old unlinked value to property value when licking field', function () {
    $scope.propertyValue = {value: 'aValue', type: 'data'};

    $scope.unlink();
    $scope.link();

    expect($scope.propertyValue.value).toBe('aValue');
  });

  it('should not allow unbinding a bidirectional property', function () {
    $scope.propertyValue = {value: 'aValue', type: 'data'};
    $scope.property = {bidirectional: true};

    $scope.unlink();

    expect($scope.propertyValue.type).toBe('data');
  });

  it('should force binding of bidirectional property', function () {
    var scope = $rootScope.$new();
    scope.property = {bidirectional: true};

    createController(scope);

    expect(scope.propertyValue.type).toBe('data');
    expect(scope.linked).toBe(true);
  });

  it('should return the list of dataNames', function () {
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

  describe('check if we have a condition to display', function () {

    beforeEach(function () {
      $scope.property = {};
    });

    it('should return true if there is no condition', function () {
      expect($scope.displayCondition()).toBe(true);
    });

    it('should true if the condition is valid', function () {
      $scope.property.showFor = 'properties.displayLabel.value === true';
      $scope.properties = {
        displayLabel: {
          value: true
        }
      };
      expect($scope.displayCondition()).toBe(true);
    });

    it('should false if the condition is not valid', function () {
      $scope.property.showFor = 'properties.displayLabel.value === true';
      $scope.properties = {
        displayLabel: {
          value: false
        }
      };
      expect($scope.displayCondition()).toBe(false);
    });
  });

});
