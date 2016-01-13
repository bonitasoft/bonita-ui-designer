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

  class ImportReportPopupController {

    constructor($uibModalInstance, importReport, importArtifactService) {
      this.$uibModalInstance = $uibModalInstance;
      this.importArtifactService = importArtifactService;
      this.report = importReport;
    }

    joinOnNames(artifacts) {
      return artifacts.map((item) => item.name).join(', ');
    }

    forceImport() {
      this.importArtifactService.forceImport(this.report)
        .then(this.$uibModalInstance.close, this.$uibModalInstance.dismiss);
    }

    hasDependencies() {
      return this.report.dependencies && this.report.dependencies.added || this.report.dependencies.overridden;
    }

    get type() {
      return this.report.element.type || 'widget';   // to be removed when widget has type
    }

    get name() {
      return this.report.element.name;
    }
  }

  angular
    .module('bonitasoft.designer.home.import')
    .controller('ImportReportPopupController', ImportReportPopupController);

})();
