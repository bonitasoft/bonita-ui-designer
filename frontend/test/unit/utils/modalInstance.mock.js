angular
  .module('mock.modal',[])
  .service('$modalInstance', function() {
    this.create = function() {
      // Create a mock object using spies
      return {
        close: jasmine.createSpy('modalInstance.close'),
        dismiss: jasmine.createSpy('modalInstance.dismiss'),
        result: {
          then: jasmine.createSpy('modalInstance.result.then')
        }
      };
    };
  });
