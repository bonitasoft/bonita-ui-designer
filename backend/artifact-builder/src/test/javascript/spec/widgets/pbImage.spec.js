describe('pbImage', function () {
  var $compile, scope,
    defaultImage = "data:image/svg+xml,%3Csvg%20xmlns%3D'http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg'%20viewBox%3D'0%200%2050%2020'%3E%3Cpath%20fill%3D'%23ccc'%20d%3D'M10%201v18h30V1H10zm29%2017H11V2h28v16zM20%209.1l3%203%205-7L36%2016H14l6-6.9zM18%206c0%201.1-.9%202-2%202s-2-.9-2-2%20.9-2%202-2%202%20.9%202%202z'%2F%3E%3C%2Fsvg%3E";

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
    expect(element.find('img').attr('class')).toContain("img-responsive");
    expect(element.find('img').attr('alt')).toBeFalsy();
  });

  it('should contains src and alt html attributes', function () {
    scope.properties.srcType = "Asset";
    scope.properties.assetName = "bonita.jpg";
    scope.properties.alt = "Bonitasoft Logo";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('ng-src')).toBe("assets/img/bonita.jpg");
    expect(element.find('img').attr('class')).toContain("img-responsive");
    expect(element.find('img').attr('alt')).toBe('Bonitasoft Logo');
  });

  it('should contains the default image if srcType is URL but url is empty and not asset', function () {
    scope.properties.url = "";
    scope.properties.assetName = "bonita.jpg";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('src')).toEqual(defaultImage);
  });

  it('should contains the default image if srcType is Asset but assetName is empty and not url', function () {
    scope.properties.srcType = "Asset";
    scope.properties.url = "http://www.bonitasoft.com";
    scope.properties.assetName = "";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('src')).toEqual(defaultImage);
  });

  it('should contains the default image if srcType is URL but url and asset are empty', function () {
    scope.properties.url = "";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('src')).toEqual(defaultImage);
  });

  it('should contains a escaped src ', function () {
    scope.properties.url = "\"><script>javascript:alert('test');</script>";

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('src')).toBe("\"><script>javascript:alert('test');</script>");
  });

  it('should contains path to image preview in ng-src when environment set a editor pageId', function () {
    var pageId = 'c0eae20f-14dd-4312-a678-2f1fab0a3898';
    scope.properties.srcType = "Asset";
    scope.properties.assetName = "bonita.jpg";
    scope.environment = {
      editor: {
        pageId: pageId
      }
    };

    var element = $compile('<pb-image></pb-image>')(scope);
    scope.$apply();
    expect(element.find('img').attr('ng-src')).toEqual('preview/page/' + pageId + '/assets/img/bonita.jpg');
    expect(element.find('img').attr('class')).toContain("img-responsive");
    expect(element.find('img').attr('alt')).toBeFalsy();
  });
});
