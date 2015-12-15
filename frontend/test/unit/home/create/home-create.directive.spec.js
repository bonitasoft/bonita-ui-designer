describe('home toolbar', () => {

  var element, $scope, controller;

  beforeEach(angular.mock.module('bonitasoft.designer.home', 'mock.modal'));

  beforeEach(inject(function($compile, $rootScope) {
    $scope = $rootScope.$new();
    $scope.refreshAll = jasmine.createSpy('refreshAll');
    $scope.filters = {};
    element = $compile('<uid-create-artifact></uid-create-artifact>')($scope);
    $scope.$apply();
    controller = element.controller('uidCreateArtifact');
  }));

  describe('directive', () => {
    it('should create an artifact', function() {
      spyOn(controller, 'createElement');

      element.find('.HomeCreate').click();

      expect(controller.createElement).toHaveBeenCalled();
    });
  });


  describe('controller', () => {

    var $modalInstance, $modal;

    beforeEach(inject(function(_$modalInstance_, _$modal_) {
      $modalInstance = _$modalInstance_;
      $modal= _$modal_;
    }));

    it('should open modal to create an artifact', () => {
      spyOn($modal, 'open').and.returnValue($modalInstance.create());

      controller.createElement();

      var [[args]] = $modal.open.calls.allArgs();
      expect(args.templateUrl).toEqual('js/home/create/create-popup.html');
      expect(args.controller).toEqual('CreatePopupController');
    });
  });
});
