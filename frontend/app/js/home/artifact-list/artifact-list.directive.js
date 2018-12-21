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
(() => {
  'use strict';

  class ArtifactListController {

    constructor($uibModal, $timeout, $localStorage, repositories, gettextCatalog, $state, artifactNamingValidatorService) {
      this.$uibModal = $uibModal;
      this.$timeout = $timeout;
      this.$localStorage = $localStorage;
      this.getRepository = (type) => repositories.get(type);
      this.gettextCatalog = gettextCatalog;
      this.$state = $state;
      this.artifactNamingValidatorService = artifactNamingValidatorService;
    }

    translateKeys(key) {
      return {
        'Delete': this.gettextCatalog.getString('Delete'),
        'Export': this.gettextCatalog.getString('Export'),
        'Rename': this.gettextCatalog.getString('Rename'),
        'Cancel': this.gettextCatalog.getString('Cancel'),
        'Last Update:': this.gettextCatalog.getString('Last Update:'),
        'In folder:': this.gettextCatalog.getString('In folder:'),
        'Validation error. Fix is recommended before export': this.gettextCatalog.getString('Validation error. Fix is recommended before export')
      }[key] || key;
    }

    deleteArtifact(artifact) {
      var template = !angular.isDefined(artifact.usedBy) ? 'js/home/artifact-list/confirm-deletion-popup.html' : 'js/home/artifact-list/alert-deletion-notpossible-popup.html';
      var modalInstance = this.$uibModal.open({
        templateUrl: template,
        windowClass: 'modal-centered',
        controller: 'DeletionPopUpController',
        resolve: {
          artifact: () => artifact,
          type: () => artifact.type
        }
      });

      modalInstance.result
        .then((id) => this.getRepository(artifact.type).delete(id))
        .then(this.refreshAll);
    }

    exportArtifact(artifact) {
      if (this.showExportPopup(artifact.type)) {
        var modalInstance = this.$uibModal.open({
          templateUrl: 'js/editor/header/export-popup.html',
          controller: 'ExportPopUpController',
          controllerAs: 'ctrl',
          resolve: {
            page: () => artifact
          }
        });

        modalInstance.result
          .then(() => {
            this.achieveExport(artifact);
          });
      } else {
        this.achieveExport(artifact);
      }
    }

    showExportPopup(artifactType) {
      let storage = this.$localStorage.bonitaUIDesigner;
      return artifactType === 'page' &&
        (!storage || !storage.doNotShowExportMessageAgain);
    }

    achieveExport(artifact) {
      this.downloadArtifact(this.getRepository(artifact.type).exportUrl(artifact));
    }

    /**
     * Toggles the name edition, to allow editing the name
     * or cancel the edition, and just display it
     *
     * @param {Object} artifact - the item to rename
     */
    toggleItemEdition(index) {
      this.editionIndex = this.editionIndex !== undefined ? undefined : index;
    }

    isInEditionMode(index) {
      return this.editionIndex === index;
    }

    /**
     * Hack to disable button if name input is in error
     * @param index
     * @returns {boolean}
     */
    isValid(index) {
      let el = document.getElementById('page-name-input-' + index);
      return el !== null && !el.getAttribute('uib-tooltip');
    }

    isArtifactNameAlreadyExist(name, artifact, artifacts) {
      var res = false;
      if (name !== artifact.name) {
        res = this.artifactNamingValidatorService.isArtifactNameAlreadyUseForType(name, artifact.type, artifacts);
      }
      return res;

    }

    /**
     * Renames an item with a new name, only if the name has changed.
     * If it doesn't, than no http call is made, we just toggle the edition
     *
     * @param {Object} artifact - the item to rename
     */
    renameItem(artifact) {
      if (artifact.newName !== artifact.name) {
        this.getRepository(artifact.type)
          .rename(artifact.id, artifact.newName)
          .then(response => {
            let location = response.headers('location');
            if (location) {
              let newId = location.substring(location.lastIndexOf('/') + 1);
              artifact.newId = newId;
            } else {
              artifact.newId = artifact.id;
            }
          })
          .catch(() => {
            artifact.newName = artifact.name;
            artifact.newId = artifact.id;
          })
          .finally(() => {
            artifact.name = artifact.newName;
            artifact.id = artifact.newId;
            artifact.editionUrl = this.$state.href(`designer.${ artifact.type }`, { id: artifact.id });
          });
      }

      /**
       * We need to defer the action of hidding the page because of the click event
       * When you click it will trigger:
       *   1. onBlur -> hide input
       *   2. click -> toggle input -> display input
       * So with a defferd action, the input is hidden on blur even if we click on da edit button
       */
      this.$timeout(() => this.editionIndex = undefined, 100);
    }
  }

  angular.module('bonitasoft.designer.home').directive('artifactList', () => ({
    controller: ArtifactListController,
    controllerAs: 'artifactList',
    scope: true,
    bindToController: {
      all: '=*artifacts',
      existingArtifacts: '=',
      refreshAll: '=',
      downloadArtifact: '='
    },
    templateUrl: 'js/home/artifact-list/artifact-list.html'
  }));
})();
