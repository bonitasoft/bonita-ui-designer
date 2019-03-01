/*******************************************************************************
 * Copyright (C) 2009, 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
(function() {
  'use strict';
  angular
    .module('bonitasoft.designer.assets')
    .directive('assetSection', assetSection);

  function assetSection() {
    return {
      restrict: 'E',
      scope: {
        id: '=',
        type: '=',
        searchTerm: '=',
        scopeFilter: '=',
        assets: '=',
        assetsCount: '=',
        onDelete: '&',
        deactivateAsset: '&',
        incrementOrderAsset: '&',
        decrementOrderAsset: '&',
        openAssetEditPopup: '&',
        openAssetPreviewPopup: '&',
        isEditable: '&',
        isViewable: '&',
        getAssetUrl: '&'
      },
      templateUrl: 'js/assets/table/asset-section.html',
      controller: 'AssetSectionCtrl',
      controllerAs: 'vm'
    };
  }

}());
