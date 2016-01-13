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
    .module('bonitasoft.designer.assets')
    .controller('AssetPopupCtrl', AssetPopupCtrl);

  function AssetPopupCtrl($scope, $uibModalInstance, alerts, assetsService, artifactRepo, asset, assets, mode, artifact, gettextCatalog) {

    var urlPrefixForLocalAsset = 'rest/' + mode + 's/' + artifact.id + '/assets/';

    var vm = this;
    vm.asset = asset;
    vm.isNewAsset = asset === undefined;

    //All datas (type, sources) are defined in the assets service.
    vm.assetTypes = assetsService.getAssetTypesByMode(mode);
    vm.assetSources = assetsService.getSources();
    vm.templates = assetsService.getFormTemplates();

    //Asset is converted in another object for the html form
    vm.newAsset = assetsService.assetToForm(asset);

    vm.cancel = $uibModalInstance.dismiss;
    vm.isExternalAsset = assetsService.isExternal;
    vm.onComplete = onComplete;
    vm.saveExternalAsset = saveExternalAsset;
    vm.updateSavingAction = updateSavingAction;
    vm.assetSavingAction = urlPrefixForLocalAsset + 'js';
    vm.isExisting = isExisting;
    vm.getWarningMessage = getWarningMessage;

    //pattern support relative URL
    vm.urlPattern = /^[\w#!:.?+=&%@\-\/]+$/;

    // When source change, we reset name to avoid collision,
    // expecially with `assetsService.isExternalAsset` which is not accurate until asset have type returned by backend
    $scope.$watch(() => vm.newAsset.external, (old, newValue) => old !== newValue && delete vm.newAsset.name);

    /**
     * An external asset is saved by a $http call
     */
    function saveExternalAsset(formAsset, $event) {
      if (assetsService.isExternal(formAsset)) {
        artifactRepo.createAsset(artifact.id, assetsService.formToAsset(formAsset)).then($uibModalInstance.close);
        $event.preventDefault(); //preventing native form action execution
      }
      // else nothing to do, form will be submitted as standard multipart/form-data form
    }

    function hasError(response) {
      return response && response.type && response.message;
    }

    /**
     * A local asset (file) is saved by the submit of the html form
     */
    function onComplete(response) {
      //Even if a problem occurs in the backend a response is sent with a message
      //If the message has a type and a message this an error
      if (hasError(response)) {
        if (response.type === 'MalformedJsonException') {
          alerts.addError({
            contentUrl: 'js/assets/malformed-json-error-message.html',
            context: response
          }, 12000);
        } else {
          alerts.addError(response.message);
        }
        vm.cancel();
      }
      $uibModalInstance.close(response);
    }

    /**
     * The form action target is not the same according to the asset type : css, js or img
     */
    function updateSavingAction(type) {
      vm.assetSavingAction = urlPrefixForLocalAsset + type;
    }

    function isExisting(asset) {
      function hasSameTypeAndName(asset, item) {
        return asset.type === item.type && item.name === asset.name;
      }

      function onScope(mode, asset) {
        return !angular.isDefined(asset.scope) || // when scope is not defined, it's the same of current artifact
          asset.scope === mode;
      }

      return asset && (assets || [])
          .filter(onScope.bind(null, mode))
          .some(hasSameTypeAndName.bind(null, asset));
    }

    function getWarningMessage(asset) {
      let display = {
        name: asset.name,
        type: assetsService.getType(asset.type).value
      };
      if (asset.type === 'img') {
        return gettextCatalog.getString('An {{type}} asset named <em>{{ name }}</em> is already added to assets.', display);
      }
      return gettextCatalog.getString('A {{type}} asset named <em>{{ name }}</em> is already added to assets.', display);
    }
  }

})();
