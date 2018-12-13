describe('home create button', () => {

  var element, $scope, controller, q, artifactFactories, state, repositories, artifactNamingValidatorService;

  beforeEach(angular.mock.module('bonitasoft.designer.home', 'mock.modal'));

  beforeEach(inject(function($compile, $rootScope, $q, _artifactFactories_, _$state_, _repositories_,_artifactNamingValidatorService_) {
    q = $q;
    state = _$state_;
    artifactFactories = _artifactFactories_;
    repositories = _repositories_;
    artifactNamingValidatorService = _artifactNamingValidatorService_;

    $scope = $rootScope.$new();
    $scope.refreshAll = jasmine.createSpy('refreshAll');
    $scope.filters = {};
    $scope.artifacts = {
      all: [{ name: 'test', type: 'widget' }, { name: 'bonita', type: 'page' }]
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
});
