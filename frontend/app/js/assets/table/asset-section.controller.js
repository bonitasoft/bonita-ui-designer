/*******************************************************************************
 * Copyright (C) 2009, 2019 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
(function() {

  angular
    .module('bonitasoft.designer.assets')
    .controller('AssetSectionCtrl', AssetSectionCtrl);

  function AssetSectionCtrl($scope, assetsService, gettextCatalog) {
    var vm = this;
    vm.key = $scope.id;
    vm.type = $scope.type;
    vm.scopeFilter = $scope.scopeFilter;
    vm.isBaseFramework = assetsService.isBaseFramework;
    vm.getEmptyAssetMessage = getEmptyAssetMessage;
    vm.getAssetsCounterByTypeForCurrentScope = getAssetsCounterByTypeForCurrentScope;
    vm.redirectTo = redirectTo;

    function redirectTo(url) {
      window.open(url, '_blank');
    }

    // Details row
    vm.isExternal = assetsService.isExternal;
    vm.isPageAsset = assetsService.isPageAsset;

    function getEmptyAssetMessage(type, value) {

      if ($scope.assets.length === 0) {
        return gettextCatalog.getString('No {{label}} asset.', { label: value.label });
      }

      if (vm.scopeFilter.page.filter && !vm.scopeFilter.widget.filter) {
        return gettextCatalog.getString('No {{label}} asset at page level.', { label: value.label });
      }

      if (!vm.scopeFilter.page.filter && vm.scopeFilter.widget.filter) {
        return gettextCatalog.getString('No {{label}} asset at widget level.', { label: value.label });
      }
      if (!vm.scopeFilter.page.filter && !vm.scopeFilter.widget.filter) {
        return gettextCatalog.getString('No {{label}} asset (no scope selected).', { label: value.label });
      }
    }

    function getAssetsCounterByTypeForCurrentScope() {
      let assetNbrCurrentScope = $scope.assets.length;
      let assetNbrTotal = $scope.assetsCount;

      if (assetNbrCurrentScope === assetNbrTotal) {
        return assetNbrCurrentScope;
      } else {
        return gettextCatalog.getString('{{currentLength}} of {{totalNumber}}', { currentLength: assetNbrCurrentScope, totalNumber: assetNbrTotal });
      }
    }
  }

})();
