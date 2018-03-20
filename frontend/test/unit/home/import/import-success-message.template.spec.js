describe('ImportArtifactSuccessMessageTemplate', function() {

  var element, scope;

  beforeEach(angular.mock.module('bonitasoft.designer.home.import'));

  // remove double space, \t, \n etc ...
  function hardTrim(string) {
    return string.replace(/(\r\n|\n|\r|\t)/gm,'').replace(/ +(?= )/g,'').trim();
  }

  beforeEach(inject(function($controller, $rootScope, gettextCatalog, $compile, $templateCache) {
    scope = $rootScope.$new();

    var template = $templateCache.get('js/home/import/import-success-message.html');
    element = $compile(template)(scope);
  }));

  it('should print to user that element has been added', function() {
    angular.extend(scope, {
      type: 'page',
      element: {
        name: 'pageName'
      },
      overwritten: false
    });
    scope.$apply();

    expect(hardTrim(element.find('p').first().text())).toBe('page pageName successfully imported.');
  });

  it('should print to user that element has been overwritten', function() {
    angular.extend(scope, {
      type: 'page',
      element: {
        name: 'pageName'
      },
      overwritten: true
    });
    scope.$apply();

    expect(hardTrim(element.find('p').first().text())).toBe('page pageName successfully imported.');
  });

  it('should not display added dependencies section when no dependencies are added', function() {
    angular.extend(scope, {
      dependencies: {

      }
    });
    scope.$apply();

    expect(element.find('section.ImportReport-added').length).toBe(0);
  });

  it('should display added dependencies', function() {
    angular.extend(scope, {
      element: { name: 'aPage' },
      overwritten: false,
      dependencies: {
        added: {
          page: [{ name: 'anotherPage' }],
          widget: [{ name: 'aWidget' }]
        }
      }
    });
    scope.$apply();

    expect(element.find('section.ImportReport-added').length).toBe(1);
    expect(hardTrim(element.find('section.ImportReport-added ul li').first().text())).toBe('page anotherPage');
    expect(hardTrim(element.find('section.ImportReport-added ul li').next().text())).toBe('widget aWidget');
  });

  it('should not display overwritten dependencies section when no dependencies are overwritten', function() {
    angular.extend(scope, {
      dependencies: {

      }
    });
    scope.$apply();

    expect(element.find('section.ImportReport-overwritten').length).toBe(0);
  });

  it('should display overwritten dependencies', function() {
    angular.extend(scope, {
      dependencies: {
        overwritten: {
          page: [{ name: 'aPage' }, { name: 'anotherPage' }],
          widget: [{ name: 'aWidget' }]
        }
      }
    });
    scope.$apply();

    expect(element.find('section.ImportReport-overwritten').length).toBe(1);
    expect(hardTrim(element.find('section.ImportReport-overwritten ul li').first().text())).toBe('pages aPage, anotherPage');
    expect(hardTrim(element.find('section.ImportReport-overwritten ul li').next().text())).toBe('widget aWidget');
  });
});
