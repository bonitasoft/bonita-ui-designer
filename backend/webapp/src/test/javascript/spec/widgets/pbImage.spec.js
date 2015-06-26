describe('pbImage', function () {

  var $compile, scope;

  beforeEach(module('pb.widgets', 'ngSanitize'));

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
    expect(element.find('img').attr('width')).toBeFalsy();
    expect(element.find('img').attr('height')).toBeFalsy();
    expect(element.find('img').attr('alt')).toBeFalsy();
  });

  it('should contains src, width, height and alt html attributes', function() {
    scope.properties.src = "assets/img/bonita.jpg";
    scope.properties.description = "Bonitasoft Logo";
    scope.properties.imageWidth = 123;
    scope.properties.imageHeight = 456;

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('ng-src')).toBe("assets/img/bonita.jpg");
    expect(element.find('img').attr('width')).toBe('123');
    expect(element.find('img').attr('height')).toBe('456');
    expect(element.find('img').attr('alt')).toBe('Bonitasoft Logo');
  });

  it('should contains a escaped src ', function () {
    scope.properties.src = "\"><script>javascript:alert('test');</script>";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('src')).toBe("\"><script>javascript:alert('test');</script>");
  });
});
