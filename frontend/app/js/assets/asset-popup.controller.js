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

  function AssetPopupCtrl($modalInstance, alerts, assetsService, artifactRepo, asset, mode, artifact) {

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

    vm.cancel = cancel;
    vm.isExternalAsset = assetsService.isExternal;
    vm.onComplete = onComplete;
    vm.saveExternalAsset = saveExternalAsset;
    vm.updateSavingAction = updateSavingAction;
    vm.assetSavingAction = urlPrefixForLocalAsset + 'js';

    /**
     * An external asset is saved by a $http call
     */
    function saveExternalAsset(data) {
      artifactRepo.createAsset(artifact.id, assetsService.formToAsset(data)).then($modalInstance.close);
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
        cancel();
      }
      $modalInstance.close(response);
    }

    /**
     * User clicked on Cancel button
     */
    function cancel() {
      $modalInstance.dismiss();
    }

    /**
     * The form action target is not the same according to the asset type : css, js or img
     */
    function updateSavingAction(type) {
      vm.assetSavingAction = urlPrefixForLocalAsset + type;
    }

  }

})();
