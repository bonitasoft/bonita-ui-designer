describe('pbImage', function () {

  var $compile, scope;

  beforeEach(module('bonitasoft.ui.widgets', 'ngSanitize'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = {};
  }));


  it('should contains a src but not other field should be empty', function () {
    scope.properties.src = "assets/img/bonita.jpg";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('ng-src')).toBe("assets/img/bonita.jpg");
    expect(element.find('img').attr('class')).toBe("img-responsive");
    expect(element.find('img').attr('alt')).toBeFalsy();
  });

  it('should contains src, width, height and alt html attributes', function() {
    scope.properties.src = "assets/img/bonita.jpg";
    scope.properties.description = "Bonitasoft Logo";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('ng-src')).toBe("assets/img/bonita.jpg");
    expect(element.find('img').attr('class')).toBe("img-responsive");
    expect(element.find('img').attr('alt')).toBe('Bonitasoft Logo');
  });

  it('should contains a escaped src ', function () {
    scope.properties.src = "\"><script>javascript:alert('test');</script>";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('src')).toBe("\"><script>javascript:alert('test');</script>");
  });
});
