describe('CustomWidgetEditorCtrl', function() {

  var $scope, alerts, $q, widgetRepo, $modal, modalScope, modalInstance;
  var awesomeWidget = {
    template: '<div>hello</div>',
    properties: [],
    name: 'Awesome Widget',
    id: 'awesomeCustomWidget'
  };

  beforeEach(module('pb.custom-widget', 'mock.modal'));

  beforeEach(inject(function($rootScope, $controller, $timeout, _$q_, _widgetRepo_, _alerts_, _$modal_, $modalInstance) {
    $scope = $rootScope.$new();

    $q = _$q_;
    widgetRepo = _widgetRepo_;
    alerts = _alerts_;
    $modal = _$modal_;
    modalInstance = $modalInstance.create();

    $controller('CustomWidgetEditorCtrl', {
      $scope: $scope,
      $modal:  _$modal_,
      widget: awesomeWidget,
      $timeout: $timeout
    });

  }));

  it('should update a property', function() {
    // given a param in the widget
    var property = {
      label: 'Value',
      name: 'value',
      type: 'text',
      defaultValue: 'This is the initial value'
    };

    var expectedProperties = [property];
    spyOn(widgetRepo, 'updateProperty').and.returnValue($q.when({data: expectedProperties}));

    $scope.updateParam('aParam', property);
    $scope.$apply();

    // then we should have updated the param in the repo
    var updateParamArgs = widgetRepo.updateProperty.calls.mostRecent().args;
    expect(updateParamArgs[0]).toBe(awesomeWidget.id);
    expect(updateParamArgs[1]).toBe('aParam');
    expect(updateParamArgs[2]).toBe(property);

    expect($scope.widget.properties).toEqual(expectedProperties);
  });

  it('should add a property', function() {
    // given a new param
    var property = {
      label: 'Value',
      name: 'value',
      type: 'text',
      defaultValue: 'This is the initial value'
    };
    var expectedProperties = [property];
    spyOn(widgetRepo, 'addProperty').and.returnValue($q.when({data: expectedProperties}));

    $scope.addParam( property);
    $scope.$apply();

    // then we should have updated the param in the repo
    var updateParamArgs = widgetRepo.addProperty.calls.mostRecent().args;
    expect(updateParamArgs[0]).toBe(awesomeWidget.id);
    expect(updateParamArgs[1]).toBe(property);

    expect($scope.widget.properties).toEqual(expectedProperties);
  });

  it('should delete a property', function() {
    var properties = [{
      label: 'Value',
      name: 'value',
      type: 'text',
      defaultValue: 'This is the initial value'
    }];
    spyOn(widgetRepo, 'deleteProperty').and.returnValue($q.when({data: properties}));;

    // when the param is deleted
    $scope.deleteParam({ name: 'toBeDeleted'});

    expect(widgetRepo.deleteProperty).toHaveBeenCalledWith($scope.widget.id, 'toBeDeleted');
  });

  it('should save a widget with its id', function() {
    // given a widget with a name
    spyOn(widgetRepo, 'save').and.returnValue($q.when());
    spyOn(alerts, 'addSuccess');

    // when the widget is saved
    $scope.save();
    $scope.$apply();

    // then we should have called the repo
    var savedWidget = widgetRepo.save.calls.mostRecent().args[0];
    expect(savedWidget.id).toBe( awesomeWidget.id );
    expect(savedWidget.template).toBe(awesomeWidget.template);
    expect(alerts.addSuccess).toHaveBeenCalledWith('Custom widget [ ' + awesomeWidget.name + ' ] successfully saved', 2000);
  });

  it('should open a dialog to create a new property', function() {
    spyOn($modal, 'open').and.returnValue(modalInstance);
    $scope.createOrUpdate();
    expect($modal.open).toHaveBeenCalled();
  });

});
