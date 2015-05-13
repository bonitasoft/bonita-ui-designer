describe('emptyTypeahead', function() {

  var element, ngModel;

  beforeEach(module('pb.directives'));

  beforeEach(inject(function($compile, $rootScope) {
    var $scope = $rootScope.$new();
    element = $compile('<div empty-typeahead ng-model="model"></div>')($scope);
    ngModel = element.controller('ngModel')
  }));

  it('should call all model parser with an empty string when $viewValue is undefined', function () {
    var parser = jasmine.createSpy();
    ngModel.$parsers.push(parser);

    element.triggerHandler('focus');

    expect(parser).toHaveBeenCalledWith(' ');
  });

  it('should call model parser with the actual $viewValue when defined', function () {
    var parser = jasmine.createSpy();
    ngModel.$viewValue = 'foobar';
    ngModel.$parsers.push(parser);

    element.triggerHandler('focus');

    expect(parser).toHaveBeenCalledWith('foobar');
  });
});
