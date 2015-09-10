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
(function () {

  angular.module('bonitasoft.designer.assets')
    .provider('assetsService', function () {

      var types = [
        {key: 'js', value: 'JavaScript', filter: true, widget: true, template: 'js/assets/generic-asset-form.html'},
        {key: 'css', value: 'CSS', filter: true, widget: true, template: 'js/assets/generic-asset-form.html'},
        {key: 'img', value: 'Image', filter: true, widget: true, template: 'js/assets/generic-asset-form.html'}
      ];

      return {
        registerType: function (type) {
          types.push(type);
        },

        $get: function (gettextCatalog) {
          'use strict';

          var source = {
            external: {key: 'external', value: gettextCatalog.getString('External')},
            local: {key: 'local', value: gettextCatalog.getString('Local')}
          };

          /**
           * Asset types
           */
          function getTypes() {
            return types;
          }

          /**
           * Asset sources
           */
          function getSources() {
            return source;
          }

          /**
           * Convert asset object in object for the html form
           */
          function assetToForm(asset) {
            if (!asset) {
              return {
                type: types[0].key,
                source: source.external.key
              };
            }
            //An asset is identified by name and type. If user choose to change them we need to delete
            //the old asset and we need the old name and type
            return {
              id: asset.id,
              name: asset.name,
              type: asset.type,
              source: isExternal(asset) ? source.external.key : source.local.key,
              oldname: asset.name,
              oldtype: asset.type
            };
          }

          /**
           * Convert html form asset in business object which can be sent to the backend
           */
          function formToAsset(formAsset) {
            var asset = {
              id: formAsset.id,
              type: formAsset.type
            };
            if (formAsset.source === source.external.key) {
              asset.name = formAsset.name;
            }
            return asset;
          }

          /**
           * External asset are URL
           */
          function isExternal(asset) {
            return asset.source === source.external.key || (asset.name && (asset.name.indexOf('http:') === 0 || asset.name.indexOf('https:') === 0));
          }

          /**
           * Page asset
           */
          function isPageAsset(asset) {
            return !asset.componentId;
          }

          function createFilters() {
            return getTypes()
              .map(function transformToFilter(type) {
                return {
                  key: type.key,
                  value: {
                    label: type.value,
                    value: type.filter
                  }
                };
              })
              .reduce(function createObject(filters, filter) {
                filters[filter.key] = filter.value;
                return filters;
              }, {});
          }

          function getAssetTypesByMode(mode) {
            var types = getTypes();
            if (mode === 'widget') {
              return types.filter(function filterWidgetOnly(type) {
                return type.widget;
              });
            }
            return types;
          }

          function createFormAssetTemplates() {
            return getTypes()
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

          return {
            isExternal: isExternal,
            isPageAsset: isPageAsset,
            getSources: getSources,
            getTypes: getTypes,
            getFilters: createFilters,
            assetToForm: assetToForm,
            formToAsset: formToAsset,
            getAssetTypesByMode: getAssetTypesByMode,
            getFormTemplates: createFormAssetTemplates
          };
        }
      };
    });
})();
