describe('ImportArtifactSuccessMessageTemplate', function () {

  var element, scope;

  beforeEach(module('bonitasoft.designer.home', 'bonitasoft.designer.templates'));

  // remove double space, \t, \n etc ...
  function hardTrim(string) {
    return string.replace(/(\r\n|\n|\r|\t)/gm,'').replace(/ +(?= )/g,'').trim();
  }

  beforeEach(inject(function ($controller, $rootScope, gettextCatalog, $compile, $templateCache) {
    scope = $rootScope.$new();

    var template = $templateCache.get('js/home/import-artifact-success-message.html');
    element = $compile(template)(scope);
  }));

  it('should print to user that element has been added', function () {
    angular.extend(scope, {
      type: 'page',
      element: {
        name: 'pageName'
      },
      overridden: false
    });
    scope.$apply();

    expect(hardTrim(element.find('p').first().text())).toBe('page pageName successfully added');
  });

  it('should print to user that element has been overridden', function () {
    angular.extend(scope, {
      type: 'page',
      element: {
        name: 'pageName'
      },
      overridden: true
    });
    scope.$apply();

    expect(hardTrim(element.find('p').first().text())).toBe('page pageName successfully overridden');
  });

  it('should not display added dependencies section when no dependencies are added', function () {
    angular.extend(scope, {
      dependencies: {

      }
    });
    scope.$apply();

    expect(element.find('section.ImportReport-added').length).toBe(0);
  });

  it('should display added dependencies', function () {
    angular.extend(scope, {
      dependencies: {
        added: {
          page: [ { name: 'aPage' }, { name: 'anotherPage' }],
          widget: [ { name: 'aWidget' }]
        }
      }
    });
    scope.$apply();

    expect(element.find('section.ImportReport-added').length).toBe(1);
    expect(hardTrim(element.find('section.ImportReport-added ul li').first().text())).toBe('pages aPage, anotherPage');
    expect(hardTrim(element.find('section.ImportReport-added ul li').next().text())).toBe('widget aWidget');
  });

  it('should not display overridden dependencies section when no dependencies are overridden', function () {
    angular.extend(scope, {
      dependencies: {

      }
    });
    scope.$apply();

    expect(element.find('section.ImportReport-overridden').length).toBe(0);
  });

  it('should display overridden dependencies', function () {
    angular.extend(scope, {
      dependencies: {
        overridden: {
          page: [ { name: 'aPage' }, { name: 'anotherPage' }],
          widget: [ { name: 'aWidget' }]
        }
      }
    });
    scope.$apply();

    expect(element.find('section.ImportReport-overridden').length).toBe(1);
    expect(hardTrim(element.find('section.ImportReport-overridden ul li').first().text())).toBe('pages aPage, anotherPage');
    expect(hardTrim(element.find('section.ImportReport-overridden ul li').next().text())).toBe('widget aWidget');
  });
});
