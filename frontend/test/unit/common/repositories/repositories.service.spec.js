describe('repositories service', () => {

  var repositories, pageRepo, widgetRepo;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));

  beforeEach(inject(function(_repositories_, _pageRepo_, _widgetRepo_) {
    repositories = _repositories_;
    pageRepo = _pageRepo_;
    widgetRepo = _widgetRepo_;
  }));

  it('should get page repository', function() {
    expect(repositories.get('page')).toBe(pageRepo);
  });

  it('should get widget repository', function() {
    expect(repositories.get('widget')).toBe(widgetRepo);
  });
});
