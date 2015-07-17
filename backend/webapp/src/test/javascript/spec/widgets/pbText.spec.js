describe('pbParagraph', function () {

  var $compile, scope;

  beforeEach(module('bonitasoft.ui.widgets', 'ngSanitize'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = {};
  }));

  it('should contains specified html', function () {
    scope.properties.text = "<em>hello</em>";

    var element = $compile('<pb-text></pb-text>')(scope);
    scope.$apply();

    expect(element.text().trim()).toBe("hello");
  });

  it('should allow text alignment', function () {
    scope.properties.alignment = "right";
    var element = $compile('<pb-text></pb-text>')(scope);
    scope.$apply();

    expect(element.find('p').hasClass('text-right')).toBeTruthy();
  });
});
