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

  angular
    .module('bonitasoft.designer.assets')
    .provider('assetsService', assetsServiceProvider);

  function assetsServiceProvider() {

    var types = [
      { key: 'css', value: 'CSS', filter: true, widget: true, template: 'js/assets/generic-asset-form.html', aceMode: 'css', orderable: true },
      { key: 'js', value: 'JavaScript', filter: true, widget: true, template: 'js/assets/generic-asset-form.html', aceMode: 'javascript', orderable: true },
      { key: 'img', value: 'Image', filter: true, widget: true, template: 'js/assets/generic-asset-form.html', orderable: false }
    ];

    return {
      registerType: (type) => types.push(type),
      $get: assetsService
    };

    function assetsService(gettextCatalog) {

      var sources = {
        external: { key: true, value: gettextCatalog.getString('External') },
        local: { key: false, value: gettextCatalog.getString('Local') }
      };

      var scopes = {
        page: { key: 'page', value: 'Page', filter: true },
        widget: { key: 'widget', value: 'Widget', filter: false },
        baseFramework: { key: 'baseFramework', value: 'Base Framework', filter: false }
      };

      let baseFrameworkAsset = [{
        active: true,
        external: false,
        name: 'bonitasoft/angular-1.3.21.min.js',
        order: -1,
        scope: 'baseFramework',
        url: 'http://www.bonitasoft.com/bos_redirect.php?bos_redirect_id=687&bos_redirect_product=bos&bos_redirect_major_version=7.9&bos_redirect_minor_version=',
        type: 'js'
      },{
        active: true,
        external: false,
        name: 'ndDialog-0.4.0.min.js',
        order: 0,
        scope: 'baseFramework',
        url: 'http://www.bonitasoft.com/bos_redirect.php?bos_redirect_id=737&bos_redirect_product=bos&bos_redirect_major_version=9.0&bos_redirect_minor_version=',
        type: 'js'
      },{
        active: true,
        external: false,
        name: 'bootstrap-3.4.1.css',
        order: -5,
        scope: 'baseFramework',
        url: 'http://www.bonitasoft.com/bos_redirect.php?bos_redirect_id=736&bos_redirect_product=bos&bos_redirect_major_version=&bos_redirect_minor_version=',
        type: 'css'
      },{
        active: true,
        external: false,
        name: 'ndDialog-0.4.0.css',
        order: -4,
        scope: 'baseFramework',
        url: 'http://www.bonitasoft.com/bos_redirect.php?bos_redirect_id=737&bos_redirect_product=bos&bos_redirect_major_version=9.0&bos_redirect_minor_version=',
        type: 'css'
      },{
        active: true,
        external: false,
        name: 'ndDialog-theme-default-0.4.0.css',
        order: -3,
        scope: 'baseFramework',
        url: 'http://www.bonitasoft.com/bos_redirect.php?bos_redirect_id=737&bos_redirect_product=bos&bos_redirect_major_version=9.0&bos_redirect_minor_version=',
        type: 'css'
      },
      {
        active: true,
        external: true,
        name: '../theme/theme.css',
        order: -2,
        scope: 'baseFramework',
        type: 'css'
      }];

      return {
        isExternal: isExternal,
        isPageAsset: isPageAsset,
        getSources: () => sources,
        getTypes: () => types,
        getScopes: () => scopes,
        getFiltersTypes: createFiltersTypes,
        assetToForm: assetToForm,
        formToAsset: formToAsset,
        getAssetTypesByMode: getAssetTypesByMode,
        getAssetUrl: getAssetUrl,
        getFormTemplates: createFormAssetTemplates,
        getBaseFrameworkAsset: () => baseFrameworkAsset,
        isBaseFramework: isBaseFramework,
        addWidgetAssetsToPage,
        removeAssetsFromPage,
        getType
      };

      /**
       * Convert asset object in object for the html form
       */
      function assetToForm(asset) {
        if (!asset) {
          return {
            type: types[0].key,
            external: true
          };
        }

        //An asset is identified by name and type. If user choose to change them we need to delete
        //the old asset and we need the old name and type
        return {
          id: asset.id,
          name: asset.name,
          type: asset.type,
          external: isExternal(asset),
          order: asset.order,
          oldname: asset.name,
          oldtype: asset.type,
          scope: asset.scope || ''
        };
      }

      /**
       * Convert html form asset in business object which can be sent to the backend
       */
      function formToAsset(formAsset, scope) {
        var asset = {
          id: formAsset.id,
          type: formAsset.type,
          order: formAsset.order,
          scope: scope
        };
        if (formAsset.external) {
          asset.name = formAsset.name;
          asset.external = true;
        }
        return asset;
      }

      /**
       * External asset are URL
       */
      function isExternal(asset) {
        return asset.external;
      }

      /**
       * Page asset
       */
      function isPageAsset(asset) {
        return asset && asset.scope === 'page';
      }

      /**
       * BaseFramework asset
       */
      function isBaseFramework(asset) {
        return asset && asset.scope === 'baseFramework';
      }

      function createFiltersTypes() {
        return types
          .map(function transformToFilter(type) {
            return {
              key: type.key,
              value: {
                label: type.value,
                value: type.filter,
                orderable: type.orderable
              }
            };
          })
          .reduce(function createObject(filters, filter) {
            filters[filter.key] = filter.value;
            return filters;
          }, {});
      }

      function getAssetTypesByMode(mode) {
        if (mode === 'widget') {
          return types.filter(function filterWidgetOnly(type) {
            return type.widget;
          });
        }
        return types;
      }

      function createFormAssetTemplates() {
        return types
          .map(function transformToTemplate(type) {
            return {
              key: type.key,
              value: type.template
            };
          })
          .reduce(function createObject(templates, template) {
            templates[template.key] = template.value;
            return templates;
          }, {});
      }

      function addWidgetAssetsToPage(widget, page) {
        page.assets = (widget && widget.$$widget.assets || [])
          .map((asset) => {
            // these operations should be done on backend side. to be deleted while backend side is homogeneous
            asset.componentId = asset.componentId || widget.id;
            asset.scope = asset.scope || 'widget';
            return asset;
          })
          .filter((asset) => notIn(asset, page.assets))
          .concat(page.assets);
      }

      function notIn(asset, array) {
        return !array.some((item) =>
          item.name === asset.name &&
          item.type === asset.type &&
          item.componentId === asset.componentId);
      }

      function removeAssetsFromPage(widget, page) {
        page.assets = page.assets.filter((asset) =>  asset.componentId !== widget.id);
      }

      function getAssetUrl(asset, component) {
        //Url depends on the nature of component
        //In custom widget editor, component is a widget
        if (component.type === 'widget') {
          return `rest/widgets/${component.id}/assets/${asset.type}/${asset.name}`;
        }
        //In page editor widget id is stored in asset.componentId if the asset scope is WIDGET
        else if (asset.scope === 'widget') {
          return `rest/widgets/${asset.componentId}/assets/${asset.type}/${asset.name}`;
        }
        //The last case is to see a page asset
        return `rest/pages/${component.id}/assets/${asset.type}/${asset.name}`;

      }

      function getType(key) {
        return types.filter(type =>  type.key === key)[0];
      }

    }
  }

})();
