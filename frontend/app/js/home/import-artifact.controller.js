(function () {

  angular
    .module('bonitasoft.designer.home')
    .controller('ImportArtifactController', ImportArtifactController);

  function ImportArtifactController($modalInstance, alerts, type, title) {

    var vm = this;
    vm.url = 'import/' + type;
    vm.filename = '';
    vm.popupTitle = title;

    vm.onSuccess = onSuccess;
    vm.onError = onError;
    vm.cancel = cancel;

    function isErrorResponse(response) {
      return response && response.type && response.message;
    }

    function onSuccess(response) {
      //Even if a problem occurs in the backend a response is sent with a message
      //If the message has a type and a message this an error
      if (isErrorResponse(response)) {
        alerts.addError(response.message);
      }
      $modalInstance.close();
    }

    function onError(error) {
      alerts.addError(error.message);
      $modalInstance.dismiss('cancel');
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  }

})();
