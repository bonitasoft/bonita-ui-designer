(function () {

  angular
    .module('bonitasoft.designer.home')
    .controller('ImportArtifactController', ImportArtifactController);

  function ImportArtifactController($modalInstance, alerts, type, title) {

    var vm = this;
    vm.url = 'import/' + type;
    vm.filename = '';
    vm.popupTitle = title;

    vm.onComplete = onComplete;
    vm.cancel = cancel;

    function isErrorResponse(response) {
      return response && response.type && response.message;
    }

    function onComplete(response) {
      //Even if a problem occurs in the backend a response is sent with a message
      //If the message has a type and a message this an error
      if (isErrorResponse(response)) {
        alerts.addError(response.message);
        $modalInstance.dismiss('cancel');
      } else {
        $modalInstance.close();
      }
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  }

})();
