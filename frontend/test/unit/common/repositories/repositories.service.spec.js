describe('repositories service', () => {

  var repositories, pageRepo, widgetRepo, fragmentRepo;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories', 'bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_repositories_, _pageRepo_, _widgetRepo_, _fragmentRepo_) {
    repositories = _repositories_;
    pageRepo = _pageRepo_;
    widgetRepo = _widgetRepo_;
    fragmentRepo = _fragmentRepo_;
  }));

  it('should get page repository', function() {
    expect(repositories.get('page')).toBe(pageRepo);
  });

  it('should get widget repository', function() {
    expect(repositories.get('widget')).toBe(widgetRepo);
  });

  it('should get fragment repository', function() {
    expect(repositories.get('fragment')).toBe(fragmentRepo);
  });
});
