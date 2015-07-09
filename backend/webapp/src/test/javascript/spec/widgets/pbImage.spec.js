xdescribe('pbImage', function () {

  var $compile, scope;

  beforeEach(module('bonitasoft.ui.widgets', 'ngSanitize'));

  beforeEach(inject(function (_$compile_, $rootScope) {
    $compile = _$compile_;
    scope = $rootScope.$new();
    scope.properties = {};
  }));


  it('should contains a src but not other field should be empty', function () {
    scope.properties.url = "http://www.bonitasoft.com/bonita.jpg";
    scope.properties.srcType = "URL";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('ng-src')).toBe("http://www.bonitasoft.com/bonita.jpg");
    expect(element.find('img').attr('class')).toBe("img-responsive");
    expect(element.find('img').attr('alt')).toBeFalsy();
  });

  it('should contains src and alt html attributes', function() {
    scope.properties.srcType = "Asset";
    scope.properties.assetName = "bonita.jpg";
    scope.properties.alt = "Bonitasoft Logo";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('ng-src')).toBe("assets/img/bonita.jpg");
    expect(element.find('img').attr('class')).toBe("img-responsive");
    expect(element.find('img').attr('alt')).toBe('Bonitasoft Logo');
  });

  it('should contains a escaped src ', function () {
    scope.properties.url = "\"><script>javascript:alert('test');</script>";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('src')).toBe("\"><script>javascript:alert('test');</script>");
  });
});
