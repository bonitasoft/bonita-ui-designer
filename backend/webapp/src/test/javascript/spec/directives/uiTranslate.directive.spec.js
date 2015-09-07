describe('uiTranslate directive', function () {

  beforeEach(module('bonitasoft.ui.directives'));

  var $scope, $compile;

  beforeEach(inject(function ($rootScope, _$compile_, gettextCatalog) {
    $compile = _$compile_;
    $scope = $rootScope.$new();

    $scope.boats = 3;

    gettextCatalog.setCurrentLanguage('nl');
    gettextCatalog.setStrings("nl", {
      "Hello": "Hallo",
      "One boat": ["Een boot", "{{$count}} boats"]
    });
  }));

  it('should translate element contents', function () {
    var element = $compile('<p ui-translate="">Hello</p>')($scope);
    $scope.$apply();

    expect(element.text()).toBe('Hallo');
  });

  it('should support plural internationalization', function () {
    var element = $compile('<p ui-translate="" translate-n="boats" translate-plural="{{$count}} boats">One boat</p>')($scope);
    $scope.$apply();

    expect(element.text()).toBe('3 boats');
  });
});
