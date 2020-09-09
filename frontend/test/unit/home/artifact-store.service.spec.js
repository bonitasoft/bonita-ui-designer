describe('ArtifactStore', function() {
  var $q, pageRepo, widgetRepo, fragmentRepo, artifactStore, $rootScope;
  var pages = [{ id: 'page1', name: 'Page 1' }];
  var widgets = [{ id: 'widget1', name: 'Widget 1', custom: true }];
  var fragments = [{ id: 'fragment1', name: 'Fragment 1' }];

  beforeEach(angular.mock.module('bonitasoft.designer.home', 'bonitasoft.designer.editor.whiteboard'));
  beforeEach(inject(function($injector) {

    $q = $injector.get('$q');
    pageRepo = $injector.get('pageRepo');
    widgetRepo = $injector.get('widgetRepo');
    fragmentRepo = $injector.get('fragmentRepo');
    artifactStore = $injector.get('artifactStore');
    $rootScope = $injector.get('$rootScope');

    spyOn(pageRepo, 'all').and.returnValue($q.when(pages));
    spyOn(widgetRepo, 'customs').and.returnValue($q.when(widgets));
    spyOn(fragmentRepo, 'all').and.returnValue($q.when(fragments));
  }));

  it('should retrieve all artifacts', function(done) {
    var foobar = [{ id: 'artifact1', name: 'artifact1' }];

    artifactStore.registerRepository({
      all: () => $q.when(foobar)
    });

    artifactStore
      .load()
      .then((artifacts) => {
        expect(artifacts).toEqual(pages.concat(widgets).concat(fragments).concat(foobar));
        done();
      });

    $rootScope.$apply();
  });
});
