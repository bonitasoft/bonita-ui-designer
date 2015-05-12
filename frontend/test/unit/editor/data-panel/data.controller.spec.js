describe('DataCtrl', function() {

  var $scope, repository, $q, $location, $modal;

  beforeEach(module('pb.controllers', 'pb.common.repositories', 'ui.bootstrap'));
  beforeEach(module('pb.common.services', 'gettext'));

  beforeEach(inject(function($rootScope, $controller, _$location_, _pageRepo_, _$q_, _$modal_) {
    $scope = $rootScope.$new();

    $scope.addData = {
      $setPristine: angular.noop
    };

    $location = _$location_;
    repository = _pageRepo_;
    $q = _$q_;
    $modal = _$modal_;

    $controller('DataCtrl', {
      $scope: $scope,
      artifactRepo: repository,
      mode: 'page',
      artifact: {}
    });

  }));

  it('should save a new data', function() {
    var data = {name: "colin", value: 4};
    spyOn(repository, 'saveData').and.returnValue($q.when({data: [data]}));

    $scope.save(data);
    $scope.$apply();

    expect(repository.saveData).toHaveBeenCalledWith($scope.page, data);
    expect($scope.page.data).toContain(data);
  });

  it('should delete a data', function() {
    var data = {name: "colin", value: 4};
    spyOn(repository, 'deleteData').and.returnValue($q.when({data: []}));

    $scope.delete(data);
    $scope.$apply();

    expect(repository.deleteData).toHaveBeenCalledWith($scope.page, data);
    expect($scope.page.data).not.toContain(data);
  });

  it('should filter a data that match a key value', function() {
    var data = {
      "colin": { value: 4 },
      "api": { value: "user" }
    };

    $scope.page.data = data;
    $scope.searchedData = "colin";
    $scope.filterPageData();
    $scope.$apply();

    expect($scope.pageData).toEqual({"colin": { value: 4 }});
  });

  it('should filter a data that match a data value', function() {
    var data = {
      "colin": { value: 4 },
      "api": { value: {"user": "toto"} }
    };

    $scope.page.data = data;
    $scope.searchedData = "use";
    $scope.filterPageData();
    $scope.$apply();

    expect($scope.pageData).toEqual({ "api": { value: {"user": "toto"} } });
  });

  it('should reset filter for data', function() {
    var data = {
      "colin": { value: 4 },
      "api": { value: "user" }
    };

    $scope.page.data = data;
    $scope.searchedData = "colin";
    $scope.filterPageData();
    $scope.$apply();

    expect($scope.pageData).toEqual({"colin": { value: 4 }});
    $scope.clearFilter();
    expect($scope.pageData).toEqual( data );
  });

  it('should open a data popup', function(){
    spyOn($modal, 'open').and.returnValue( {
      result: $q.when({})
    });
    $scope.openDataPopup();
    expect($modal.open).toHaveBeenCalled()
  })
});
