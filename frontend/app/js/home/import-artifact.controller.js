(function() {
  'use strict';
  angular
    .module('bonitasoft.designer.home')
    .service('importArtifactService', importArtifactService)
    .controller('ImportArtifactController', ImportArtifactController)
    .controller('ImportArtifactReportController', ImportArtifactReportController);

  function importArtifactService(alerts, gettextCatalog, $q, $http, pageRepo, widgetRepo) {
    var forceImportRepoFns = {};
    registerForceImportFn('page', pageRepo.forceImport);
    registerForceImportFn('widget', widgetRepo.forceImport);

    var service = {
      forceImport,
      isErrorResponse,
      manageImportResponse,
      doesImportOverrideExistingContent,
      registerForceImportFn,
      forceImportRepoFns
    };
    return service;

    function registerForceImportFn(name, fn) {
      forceImportRepoFns[name] = fn;
    }
   
    function isErrorResponse(response) {
      return response && response.type && response.message;
    }

    function manageImportResponse (type, checkForOverrides, response) {
      var deferred = $q.defer();
      //Even if a problem occurs in the backend a response is sent with a message
      //If the message has a type and a message this is an error
      if (service.isErrorResponse(response)) {
        alerts.addError(response.message);
        deferred.reject();
      }else {
        var importReportContext = angular.extend(response, {
          type: type
        });
        if (!checkForOverrides || service.doesImportOverrideExistingContent(response)){
          //
          alerts.addSuccess({
            title: gettextCatalog.getString('Successful import'),
            contentUrl: 'js/home/import-artifact-success-message.html',
            context:importReportContext
          }, 15000);
          deferred.resolve();
        } else{
          deferred.resolve(importReportContext);
        }
      }
      return deferred.promise;
    }

    function doesImportOverrideExistingContent (report) {
      return !(report.overridden || (report.dependencies && report.dependencies.overridden));
    }

    function forceImport(report, type, success, error) {
      forceImportRepoFns[type](report.uuid).then(function(response) {
        return response.data;
      }).then(service.manageImportResponse.bind(service, type, false)).then(success, error);
    }
  }

  function ImportArtifactController($modalInstance, type, title, importArtifactService) {

    var vm = this;
    vm.type = type;
    vm.url = 'import/' + type;
    vm.filename = '';
    vm.popupTitle = title;

    vm.onComplete = onComplete;

    function onComplete(response) {

      importArtifactService.manageImportResponse(type, true, response).then($modalInstance.close, $modalInstance.dismiss);
    }
  }

  function ImportArtifactReportController($modalInstance, importReport, title, type, importArtifactService) {

    var vm = this;
    vm.popupTitle = title;
    vm.report = importReport;
    vm.joinOnNames = joinOnNames;

    vm.forceImport = forceImport;


    function joinOnNames(artifacts) {
      return artifacts.map(function(item) {
        return item.name;
      }).join(', ');
    }

    function forceImport() {
        importArtifactService.forceImport(importReport, type, $modalInstance.close, $modalInstance.dismiss);
    }
  }

})();
