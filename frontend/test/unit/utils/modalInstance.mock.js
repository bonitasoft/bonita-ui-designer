angular
  .module('mock.modal',[])
  .service('$modalInstance', function($q) {
    this.create = function() {
      // Create a mock object using spies
      return {
        close: jasmine.createSpy('modalInstance.close'),
        dismiss: jasmine.createSpy('modalInstance.dismiss'),
        result: {
          then: function(data) {
            return $q.when(data);
          }
        }
      };
    };
  });
