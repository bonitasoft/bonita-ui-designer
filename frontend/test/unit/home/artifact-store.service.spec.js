describe('HomeCtrl', function() {
  var $q, pageRepo, widgetRepo, artifactStore, $rootScope;
  var pages = [{ id: 'page1', name: 'Page 1' }];
  var widgets = [{ id: 'widget1', name: 'Widget 1', custom: true }];

  beforeEach(angular.mock.module('bonitasoft.designer.home'));
  beforeEach(inject(function($injector) {

    $q = $injector.get('$q');
    pageRepo = $injector.get('pageRepo');
    widgetRepo = $injector.get('widgetRepo');
    artifactStore = $injector.get('artifactStore');
    $rootScope = $injector.get('$rootScope');

    spyOn(pageRepo, 'all').and.returnValue($q.when(pages));
    spyOn(widgetRepo, 'customs').and.returnValue($q.when(widgets));
  }));

  it('should retrieve all artifacts', function(done) {
    var foobar = [{ id: 'artifact1', name: 'artifact1' }];

    artifactStore.registerRepository({
      all: () => $q.when(foobar)
    });

    artifactStore
      .load()
      .then((artifacts) => {
        expect(artifacts).toEqual(pages.concat(widgets).concat(foobar));
        done();
      });

    $rootScope.$apply();
  });
});
