(function() {

  'use strict';

  angular
    .module('mock.modal', [])
    .service('$uibModalInstance', $uibModalInstanceMock);

  function $uibModalInstanceMock($q) {

    return {
      create: mock,
      fake: fake
    };

    /*
     * Create a mock object using spies
     * Useful when you want to test a modal instance controller
     */
    function mock() {
      return {
        close: jasmine.createSpy('modalInstance.close'),
        dismiss: jasmine.createSpy('modalInstance.dismiss'),
        result: {
          then: function(data) {
            return $q.when(data);
          }
        }
      };
    }

    /*
     * Create fake modal instance
     * Useful when you want to test a controller that open a modal a perform operation on modal closing
     */
    function fake() {
      var deferred = $q.defer();
      return {
        close: function(data) {
          return deferred.resolve(data);
        },
        dismiss: function(data) {
          return deferred.reject(data);
        },
        result: deferred.promise
      };
    }
  }

})();
