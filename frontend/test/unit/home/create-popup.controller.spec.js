describe('CreatePopupController', () => {
  var artifactFactories, createPopupCtrl, $scope, q, $modalInstance, repositories, state, artifacts;

  beforeEach(angular.mock.module('bonitasoft.designer.home', 'mock.modal'));

  beforeEach(inject(function(_$modalInstance_, $controller, _$q_, _artifactFactories_, $rootScope, _repositories_, _$state_) {
    $scope = $rootScope.$new();
    artifactFactories = _artifactFactories_;
    $modalInstance = _$modalInstance_.create();
    repositories = _repositories_;
    state = _$state_;
    q = _$q_;
    artifacts = [ { name:'test', type: 'widget' }, { name:'bontia', type: 'page' } ];

    createPopupCtrl = $controller('CreatePopupController', {
      $modalInstance,
      artifacts,
      $scope
    });
  }));

  it('should expose data for view', () => {
    expect(createPopupCtrl.type).toEqual(artifactFactories.getFactory('page'));
    expect(createPopupCtrl.types).toEqual(artifactFactories.getFactories());
  });

  it('should check widget name if it already exists', () => {

    var type = artifactFactories.getFactory('page');
    expect(createPopupCtrl.isNameUniqueIfRelevantForType('bonita', type)).toBeFalsy();
    expect(createPopupCtrl.isNameUniqueIfRelevantForType('test', type)).toBeFalsy();

    type = artifactFactories.getFactory('widget');
    expect(createPopupCtrl.isNameUniqueIfRelevantForType('test', type)).toBeTruthy();

    type = artifactFactories.getFactory('page');
    expect(createPopupCtrl.isNameUniqueIfRelevantForType('test', type)).toBeFalsy();
  });

  it('should create a widget and navigate to editor', () => {
    let generatedWidgetId = '12321',
      deferred = q.defer(),
      widgetRepo = jasmine.createSpyObj('widgetRepo', ['create']);

    widgetRepo.create.and.returnValue(deferred.promise);

    spyOn(artifactFactories.getFactory('widget'), 'create').and.callThrough();
    spyOn(state, 'go');
    spyOn(repositories, 'get').and.returnValue(widgetRepo);

    deferred.resolve({id: generatedWidgetId});

    createPopupCtrl.create(artifactFactories.getFactory('widget'), 'test');
    $scope.$apply();

    expect($modalInstance.close).toHaveBeenCalled();
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

    deferred.resolve({id: generatedPageId});

    createPopupCtrl.create(artifactFactories.getFactory('page'), 'test');
    $scope.$apply();

    expect($modalInstance.close).toHaveBeenCalled();
    expect(state.go).toHaveBeenCalledWith('designer.page', {
      id: generatedPageId
    });
  });



});
