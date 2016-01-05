describe('home create button', () => {

  var element, $scope, controller, q, artifactFactories, state, repositories;

  beforeEach(angular.mock.module('bonitasoft.designer.home', 'mock.modal'));

  beforeEach(inject(function($compile, $rootScope, $q, _artifactFactories_, _$state_, _repositories_) {
    q = $q;
    state = _$state_;
    artifactFactories = _artifactFactories_;
    repositories = _repositories_;

    $scope = $rootScope.$new();
    $scope.refreshAll = jasmine.createSpy('refreshAll');
    $scope.filters = {};
    $scope.artifacts = {
      all: [{ name: 'test', type: 'widget' }, { name: 'bontia', type: 'page' }]
    };

    element = $compile('<uid-create-artifact artifacts="artifacts.all"></uid-create-artifact>')($scope);
    $scope.$apply();
    controller = element.controller('uidCreateArtifact');
  }));

  it('should expose data for view', () => {
    expect(controller.type).toEqual(artifactFactories.getFactory('page'));
    expect(controller.types).toEqual(artifactFactories.getFactories());
  });

  it('should check widget name if it already exists', () => {
    var type = artifactFactories.getFactory('page');
    expect(controller.isNameUniqueIfRelevantForType('bonita', type)).toBeFalsy();
    expect(controller.isNameUniqueIfRelevantForType('test', type)).toBeFalsy();

    type = artifactFactories.getFactory('widget');
    expect(controller.isNameUniqueIfRelevantForType('test', type)).toBeTruthy();

    type = artifactFactories.getFactory('page');
    expect(controller.isNameUniqueIfRelevantForType('test', type)).toBeFalsy();
  });

  it('should create a widget and navigate to editor', () => {
    let generatedWidgetId = '12321',
      deferred = q.defer(),
      widgetRepo = jasmine.createSpyObj('widgetRepo', ['create']);

    widgetRepo.create.and.returnValue(deferred.promise);

    spyOn(artifactFactories.getFactory('widget'), 'create').and.callThrough();
    spyOn(state, 'go');
    spyOn(repositories, 'get').and.returnValue(widgetRepo);

    deferred.resolve({ id: generatedWidgetId });

    controller.create(artifactFactories.getFactory('widget'), 'test');
    $scope.$apply();

    expect(state.go).toHaveBeenCalledWith('designer.widget', {
      id: generatedWidgetId
    });
  });

  it('should create a page and navigate to editor', () => {
    let generatedPageId = '12321',
      deferred = q.defer(),
      pageRepo = jasmine.createSpyObj('pageRepo', ['create']);

    pageRepo.create.and.returnValue(deferred.promise);

    spyOn(artifactFactories.getFactory('page'), 'create').and.callThrough();
    spyOn(state, 'go');
    spyOn(repositories, 'get').and.returnValue(pageRepo);

    deferred.resolve({ id: generatedPageId });

    controller.create(artifactFactories.getFactory('page'), 'test');
    $scope.$apply();

    expect(state.go).toHaveBeenCalledWith('designer.page', {
      id: generatedPageId
    });
  });
});
