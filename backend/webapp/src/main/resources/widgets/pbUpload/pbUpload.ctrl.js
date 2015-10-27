function PbUploadCtrl($scope, $sce, $element, widgetNameFactory, $timeout, $log, gettextCatalog) {
  var ctrl = this;
  this.name = widgetNameFactory.getName('pbInput');
  this.filename = '';
  this.filemodel = '';

  this.clear = clear;
  this.startUploading = startUploading;
  this.uploadError = uploadError;
  this.uploadComplete = uploadComplete;

  this.name = widgetNameFactory.getName('pbUpload');

  var input = $element.find('input');
  var form = $element.find('form');

  input.on('change', forceSubmit);
  $scope.$on('$destroy', function() {
    input.off('change', forceSubmit);
  });

  $scope.$watch('properties.url', function(newUrl, oldUrl){
    ctrl.url = $sce.trustAsResourceUrl(newUrl);
    if (newUrl === undefined) {
      $log.warn('you need to define a url for pbUpload');
    }
  });

  if (!$scope.properties.isBound('value')) {
    $log.error('the pbUpload property named "value" need to be bound to a variable');
  }

  function clear() {
    ctrl.filename = '';
    ctrl.filemodel = '';
    $scope.properties.value = {};
  }

  function uploadError(error) {
    $log.warn('upload fails too', error);
    ctrl.filemodel = '';
    ctrl.filename = gettextCatalog.getString('Upload failed');
  }

  function startUploading() {
    ctrl.filemodel = '';
    ctrl.filename  = gettextCatalog.getString('Uploading...');
  }

  function uploadComplete(response) {
    //when the upload widget return a String, it means an error has occurred (with a html document as a response)
    //if it's not a string, we test if it contains some error message
    if(angular.isString(response) || (response && response.type && response.message)){
      $log.warn('upload fails');
      ctrl.filemodel = '';
      ctrl.filename = gettextCatalog.getString('Upload failed');
      $scope.properties.errorContent = angular.isString(response) ? response : response.message;
      return;
    }

    if (response.filename) {
      ctrl.filemodel = true;
      ctrl.filename = response.filename;
    }

    $scope.properties.value = response;
  }

  function forceSubmit(event) {
    if( !event.target.value) {
      return;
    }

    form.triggerHandler('submit');
    form[0].submit();
  }
}
