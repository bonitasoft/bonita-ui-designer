describe('HomeCtrl', function() {
  var $scope, $q, $modal, pageRepo, widgetRepo, pages, widgets, controller;

  beforeEach(angular.mock.module('bonitasoft.designer.home'));
  beforeEach(inject(function($controller, $rootScope, $injector) {
    $scope = $rootScope.$new();
    $q = $injector.get('$q');
    $modal = $injector.get('$modal');
    pageRepo = $injector.get('pageRepo');
    widgetRepo = $injector.get('widgetRepo');

    pages = [
      {
        id: 'page1',
        name: 'Page 1'
      }
    ];
    widgets = [
      {
        id: 'widget1',
        name: 'Widget 1',
        custom: true
      }
    ];

    spyOn(pageRepo, 'all').and.returnValue($q.when(pages));
    spyOn(widgetRepo, 'customs').and.returnValue($q.when(widgets));

    controller = $controller('HomeCtrl', {
      $scope: $scope,
      pageRepo: pageRepo,
      widgetRepo: widgetRepo
    });
    $scope.$digest();
  }));

  it('should refresh everything', function() {

    $scope.refreshAll();
    $scope.$apply();

    expect(pageRepo.all).toHaveBeenCalled();
    expect(widgetRepo.customs).toHaveBeenCalled();
    expect($scope.artifacts).toEqual(pages.concat(widgets));
  });
});
