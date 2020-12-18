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
      all: [{ name: 'test', type: 'widget' }, { name: 'bonita', type: 'page' },  { name:'fragmentBontia', type: 'fragment' }]
    };

    element = $compile('<uid-create-artifact artifacts="artifacts.all"></uid-create-artifact>')($scope);
    $scope.$apply();
    controller = element.controller('uidCreateArtifact');
  }));

  it('should expose data for view', () => {
    expect(controller.type).toEqual(artifactFactories.getFactory('page'));
    expect(controller.types).toEqual(artifactFactories.getFactories());
  });

  it('should preselect type when activated artifact holds a valid type', () => {
    expect(controller.type).toBe(controller.types.page);
    controller.artifactActive = { id: 'widget' };
    $scope.$apply();
    expect(controller.type).toBe(controller.types.widget);
  });

  it('should check page name if it already exists with case-insensitive', () => {
    var type = artifactFactories.getFactory('page');
    expect(controller.isArtifactNameAlreadyExist('bonita', type)).toBeTruthy();
    expect(controller.isArtifactNameAlreadyExist('Bonita', type)).toBeTruthy();
    expect(controller.isArtifactNameAlreadyExist('test', type)).toBeFalsy();
  });


  it('should check widget name if it already exists', () => {
    var type = artifactFactories.getFactory('widget');
    expect(controller.isArtifactNameAlreadyExist('test', type)).toBeTruthy();

    type = artifactFactories.getFactory('page');
    expect(controller.isArtifactNameAlreadyExist('test', type)).toBeFalsy();
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

  it('should expose data for view', () => {
    expect(controller.type).toEqual(artifactFactories.getFactory('page'));
    expect(controller.types).toEqual(artifactFactories.getFactories());
  });

  it('should check page name if it already exists', () => {

    var type = artifactFactories.getFactory('page');
    expect(controller.isArtifactNameAlreadyExist('bonita', type)).toBeTruthy();
    expect(controller.isArtifactNameAlreadyExist('Bonita', type)).toBeTruthy();
    expect(controller.isArtifactNameAlreadyExist('test', type)).toBeFalsy();
  });

  it('should check widget name if it already exists', () => {

    var type = artifactFactories.getFactory('page');
    expect(controller.isArtifactNameAlreadyExist('bonita', type)).toBeTruthy();
    expect(controller.isArtifactNameAlreadyExist('test', type)).toBeFalsy();

    type = artifactFactories.getFactory('widget');
    expect(controller.isArtifactNameAlreadyExist('test', type)).toBeTruthy();
    expect(controller.isArtifactNameAlreadyExist('Test', type)).toBeTruthy();

    type = artifactFactories.getFactory('fragment');
    expect(controller.isArtifactNameAlreadyExist('test', type)).toBeFalsy();

    expect(controller.isArtifactNameAlreadyExist('fragmentBontia', type)).toBeTruthy();
  });

  it('should create a fragment and navigate to editor', () => {
    let generatedFragmentId = '12321',
      deferred = q.defer(),
      fragmentRepo = jasmine.createSpyObj('fragmentRepo', ['create']);

    fragmentRepo.create.and.returnValue(deferred.promise);

    spyOn(artifactFactories.getFactory('fragment'), 'create').and.callThrough();
    spyOn(state, 'go');
    spyOn(repositories, 'get').and.returnValue(fragmentRepo);

    deferred.resolve({ id: generatedFragmentId });

    controller.create(artifactFactories.getFactory('fragment'), 'test');
    $scope.$apply();

    expect(state.go).toHaveBeenCalledWith('designer.fragment', {
      id: generatedFragmentId
    });
  });
});
