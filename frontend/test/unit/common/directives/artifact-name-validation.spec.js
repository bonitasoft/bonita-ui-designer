describe('artifact name vaildation', function() {
  var $compile, element, $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.common.directives'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $scope = _$rootScope_.$new();
    $scope.artifact = {};

    element = $compile('<form name="form"><input ng-model="artifact.name" artifact-name-validation /></form>')($scope);
    $scope.$apply();
  }));

  it('should display nothing when name is valid', function() {
    element.find('input').val('validName').trigger('input');
    $scope.$apply();

    expect(element.hasClass('ng-valid')).toBeTruthy();
    expect(element.find('div[uib-tooltip-popup] .tooltip-inner').length).toBe(0);
  });

  it('should display an error message in tooltip when name contain space', function() {

    element.find('input').val('Wrong name').trigger('input');
    $scope.$apply();

    expect(element.hasClass('ng-invalid')).toBeTruthy();
    expect(element.hasClass('ng-invalid-pattern')).toBeTruthy();
    expect(element.find('div[uib-tooltip-popup] .tooltip-inner').text()).toBe('Name must contains only alphanumeric characters with no space');
  });

  it('should display an error message in tooltip when name contain special characters', function() {

    element.find('input').val('Wrong#name').trigger('input');
    $scope.$apply();

    expect(element.hasClass('ng-invalid')).toBeTruthy();
    expect(element.hasClass('ng-invalid-pattern')).toBeTruthy();
    expect(element.find('div[uib-tooltip-popup] .tooltip-inner').text()).toBe('Name must contains only alphanumeric characters with no space');
  });

  it('should display an error message in tooltip when name has more than 240 characters', function() {
    const longName = '1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890' +
        '1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890' +
        '1234567890123456789012345678901234567890TooLong';

    element.find('input').val(longName).trigger('input');
    $scope.$apply();

    expect(element.hasClass('ng-invalid')).toBeTruthy();
    expect(element.hasClass('ng-invalid-maxlength')).toBeTruthy();
    expect(element.find('div[uib-tooltip-popup] .tooltip-inner').text()).toBe('Name must be less than 240 characters long');
  });

  it('should display tooltip on top left by default', function() {
    element.find('input').val('Wrong#name').trigger('input');
    $scope.$apply();

    expect(element.find('div[uib-tooltip-popup]').attr('placement')).toBe('top-left');
  });

  it('should display tootlip on provided placement', function() {
    element = $compile('<form name="form"><input ng-model="artifact.name" artifact-name-validation="bottom" /></form>')($scope);
    $scope.$apply();

    element.find('input').val('Wrong#name').trigger('input');
    $scope.$apply();

    expect(element.find('div[uib-tooltip-popup]').attr('placement')).toBe('bottom');
  });

  it('should register extra validation messages', function() {
    let template =
      `<form name="form">
        <input ng-model="artifact.name" ng-minlength="20" required artifact-name-validation
             artifact-name-validation-messages="{ minlength: '{{ type }} should have at least 20 characters', required: 'this field is required' }" />
      </form>`;
    element = $compile(template)($scope);
    $scope.$apply();

    element.find('input').val('shortname').trigger('input');
    $scope.type = 'Page';
    $scope.$apply();
    expect(element.find('div[uib-tooltip-popup] .tooltip-inner').text()).toBe('Page should have at least 20 characters');

    element.find('input').val('').trigger('input');
    $scope.$apply();
    expect(element.find('div[uib-tooltip-popup] .tooltip-inner').text()).toBe('this field is required');
  });
});
