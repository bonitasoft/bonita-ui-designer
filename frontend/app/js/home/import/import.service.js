/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.home.import')
    .service('importArtifactService', importArtifactService);

  function importArtifactService(alerts, gettextCatalog, $q, importRepo, importErrorMessagesService) {

    var service = {
      forceImport,
      isErrorResponse,
      manageImportResponse,
      doesImportOverrideExistingContent
    };
    return service;

    function isErrorResponse(response) {
      return response && response.type && response.message;
    }

    function manageImportResponse(type, checkForOverrides, response) {
      var deferred = $q.defer();
      //Even if a problem occurs in the backend a response is sent with a message
      //If the message has a type and a message this is an error
      if (service.isErrorResponse(response)) {

        alerts.addError({
          title: gettextCatalog.getString('Import error'),
          contentUrl: 'js/home/import/import-error-message.html',
          context: importErrorMessagesService.getErrorContext(response, type)
        });
        deferred.reject();
      } else {
        var importReportContext = angular.extend(response, {
          type: response.element.type || 'widget'   // TODO remove this when widget has type
        });
        if (!checkForOverrides || service.doesImportOverrideExistingContent(response)) {
          //
          alerts.addSuccess({
            title: gettextCatalog.getString('Successful import'),
            contentUrl: 'js/home/import/import-success-message.html',
            context: importReportContext
          }, 15000);
          deferred.resolve();
        } else {
          deferred.resolve(importReportContext);
        }
      }
      return deferred.promise;
    }

    function doesImportOverrideExistingContent(report) {
      return !(report.overridden || (report.dependencies && report.dependencies.overridden));
    }

    function forceImport(report) {
      var type = report.element.type || 'widget';   // TODO inline this when widget has type
      return importRepo.forceImport(report.uuid)
        .then(service.manageImportResponse.bind(service, type, false));
    }
  }

})();
