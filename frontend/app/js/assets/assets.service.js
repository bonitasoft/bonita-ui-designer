(function () {

  angular.module('pb.assets').service('assetsService', function () {

    'use strict';

    var types = [
      { key: 'js', label: 'JavaScript'},
      { key: 'css', label: 'CSS'},
      { key: 'img', label: 'Images'}
    ];

    var sources = [ 'External', 'Local' ];

    /**
     * By default all the assets are displayed
     */
    function initFilterMap() {
      return types.map(function (obj) {
        obj.filter = true;
        return obj;
      });
    }

    /**
     * Asset types
     */
    function getTypes() {
      return types;
    }

    /**
     * Asset palces
     */
    function getSources() {
      return sources;
    }

    /**
     * Asset palces
     */
    function getExternalSource() {
      return sources[0];
    }

    /**
     * Return the label for a type
     */
    function getTypeLabel(key) {
      var type = types.filter(function (element) {
        return element.key === key;
      })[0];

      return type ? type.label : '';
    }

    /**
     * Convert asset object in object for the html form
     */
    function assetToForm(asset) {
      if(!asset){
        return {
          type : types[0].key,
          source : 'External'
        };
      }
      //An asset is identified by name and type. If user choose to change them we need to delete
      //the old asset and we need the old name and type
      return {
        name : asset.name,
        type : asset.type,
        source : isExternal(asset) ? 'External' : 'Local',
        oldname : asset.name,
        oldtype : asset.type
      };
    }

    /**
     * Convert html form asset in business object which can be sent to the backend
     */
    function formToAsset(formAsset) {
      var asset = {
        type : formAsset.type
      };
      if(formAsset.source=== 'External'){
        asset.name = formAsset.name;
      }
      return asset;
    }

    /**
     * External asset are URL
     */
    function isExternal(asset) {
      return asset.name.indexOf('http') === 0;
    }

    return {
      initFilterMap: initFilterMap,
      isExternal: isExternal,
      getSources:getSources,
      getExternalSource:getExternalSource,
      getTypeLabel:getTypeLabel,
      getTypes : getTypes,
      assetToForm : assetToForm,
      formToAsset : formToAsset
    };
  });

})();