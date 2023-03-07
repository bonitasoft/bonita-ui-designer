/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

  class WebResourcesCtrl {
    constructor($scope, $rootScope, $uibModal, gettextCatalog, bonitaResources, artifact,  artifactRepo) {
      this.$scope = $scope;
      this.$uibModal = $uibModal;
      this.gettextCatalog = gettextCatalog;
      this.artifact = artifact;
      this.displayWebResources = [];
      this.httpVerbs = bonitaResources.httpVerbs;
      this.artifactRepo = artifactRepo;
      this.sort('value');
      this.loadAutoWebResources();
      $rootScope.$on('artifactUpdate', () => this.loadAutoWebResources());
    }

    loadAutoWebResources(){
      this.artifactRepo.loadAutoWebResources(this.artifact)
        .then((data) => {
          this.autoWebResources = data;
          this.refreshTable()
        });
    }

    refreshTable(){
      if(!this.artifact.webResources){
        this.artifact.webResources = [];
      }
      this.artifact.webResources.map(r => {
        if(this.autoWebResources.some(autoR => this.isSameWebResource(r,autoR))){
          return Object.defineProperty(r, 'warning', { enumerable: false, value: true});
        }
      });
      this.displayWebResources = this.artifact.webResources.concat(this.autoWebResources);
    }

    openWebResourcePopup(webResource) {
      const modalInstance = this.$uibModal.open({
        templateUrl: 'js/editor/bottom-panel/web-resources-panel/web-resource-popup.html',
        controller: 'WebResourcePopupCtrl',
        controllerAs: 'ctrl',
        size: 'md',
        resolve: {
          webResources: () => Object.assign([], this.displayWebResources),
          data: () => webResource,
          isSameWebResource: () => this.isSameWebResource
        }
      });

      modalInstance.result.then(this.addOrUpdateWebResource.bind(this));
    };

    isSameWebResource(a,b){
      return a.verb.toLowerCase() === b.verb.toLowerCase() && a.value.toLowerCase() === b.value.toLowerCase();
    }



    addOrUpdateWebResource(webResource){
      if (webResource.hasOwnProperty('id')) {
        const indexToUpdate = this.artifact.webResources.findIndex(resource => resource.id === webResource.id);
        this.artifact.webResources[indexToUpdate].verb = webResource.verb;
        this.artifact.webResources[indexToUpdate].value = webResource.value;
      } else {
        this.artifact.webResources.push(webResource);
      }
      this.refreshTable();
    }

    sort(sortCriteria) {
      this.$scope.isReversedSorting = this.$scope.sortCriteria === sortCriteria ? !this.$scope.isReversedSorting : false;
      this.$scope.sortCriteria = sortCriteria;
    };

    getResources(serchTerm) {
      function toMatchSearchTerm(webResource) {
        function contains(value, search) {
          return (value || '').toLowerCase().indexOf((search || '').toLowerCase()) !== -1;
        }

        return contains(webResource.verb, serchTerm) ||
          contains(webResource.value, serchTerm) ||
          contains(webResource.scopes ? webResource.scopes.join(',') : '', serchTerm);
      }
      return Object.keys(this.displayWebResources)
        .map((resource) => {
          let webResource = this.displayWebResources[resource];
          return Object.defineProperty(webResource, 'id', { configurable: true ,enumerable: false, value: resource})
        })
        .filter((webResource) => this.verbFilter(webResource))
        .filter(toMatchSearchTerm);
    };

    verbFilter(webResource) {
      const element = this.httpVerbs.find(verb => verb.type.toLowerCase() === webResource.verb.toLowerCase());
      return element && element.filter;
    }

    openDeletePopup(webResource) {
      const modalInstance = this.$uibModal.open({
        templateUrl: 'js/confirm-delete/confirm-delete-popup.html',
        controller: 'ConfirmDeletePopupController',
        controllerAs: 'ctrl',
        size: 'md',
        resolve: {
          artifact: () => `${webResource.verb.toUpperCase()}|${webResource.value}`,
          type: () => this.gettextCatalog.getString('Bonita resource')
        }
      });
      modalInstance.result.then(this.deleteResource.bind(this, webResource));
    };

    deleteResource(webResource){
      if(webResource.hasOwnProperty('id')){
        this.artifact.webResources = this.artifact.webResources.filter((obj) => obj.id !== webResource.id)
        this.refreshTable();
      }
    }

    openHelp() {
      this.$uibModal.open({
        templateUrl: 'js/editor/bottom-panel/web-resources-panel/help-popup.html',
        size: 'lg'
      });
    }

    isUpdatable(resource){
      return !this.isAutomaticDetection(resource);
    }
    isAutomaticDetection(resource){
      return resource.hasOwnProperty('automatic') && resource.automatic;
    }

    isOnWarning(resource){
      return resource.hasOwnProperty('warning') && resource.warning;
    }
  }

  class WebResourcePopupCtrl {
    constructor($scope, $uibModalInstance, data, webResources, bonitaResources,isSameWebResource) {
      this.$scope = $scope;
      this.httpVerbs = bonitaResources.httpVerbs;
      this.$uibModalInstance = $uibModalInstance;
      this.webResources = webResources || [];
      this.isSameWebResource = isSameWebResource;
      this.resources = bonitaResources.resources;
      this.resourceIdToEdit = data ? data.id : undefined;

      if(data) {
        this.newResource = Object.assign({}, data);
        this.newResource.id = data.id;
      }else{
        this.newResource = {verb: 'get', value: ''}
      }
    }

    getVerbLabel(type) {
      return this.httpVerbs
        .filter(verb => verb.type === type)
        .reduce((acc, item) => item.label, undefined);
    };

    isWebResourceUnique(item) {
      if(!item.value) {
        return true;
      }
      const indexToUpdate = this.webResources.find(resource => {
        return resource.hasOwnProperty('id') && resource.id === this.newResource.id;
      });
      let alreadyExist = this.webResources
        .filter(data => indexToUpdate === undefined || !this.isSameWebResource(indexToUpdate,data)) // except himself
        .some(webResource => this.isSameWebResource(item,webResource));
      return !item || !alreadyExist;
    };



    cancel() {
      this.$uibModalInstance.dismiss('cancel');
    }

    canBeSaved() {
      return this.$scope.declareNewResource.$valid;
    };

    save(dataToSave) {
      this.$uibModalInstance.close(dataToSave);
    };
  }

  angular
    .module('bonitasoft.designer.webResources')
    .controller('WebResourcesCtrl', WebResourcesCtrl)
    .controller('WebResourcePopupCtrl', WebResourcePopupCtrl);
})
();
