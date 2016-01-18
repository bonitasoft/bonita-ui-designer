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
    .module('app.route', ['tmh.dynamicLocale'])
    .config((tmhDynamicLocaleProvider) => {
      tmhDynamicLocaleProvider.localeLocationPattern('locales/angular-locale_{{locale}}.js');
    })
    .constant('appStates', {
      'designer': {
        abstract: true,
        url: '/:lang',
        template: '<ui-view></ui-view>',
        resolve: { /* @ngInject */
          language: function($stateParams, gettextCatalog, tmhDynamicLocale) {
            var languages = {
              'en': { lang: 'en' },
              'fr': { lang: 'fr', file: 'lang-template-fr-FR.json' },
              'es': { lang: 'es-ES', file: 'lang-template-es-ES.json' },
              'es-ES': { lang: 'es-ES', file: 'lang-template-es-ES.json' },
              'ja': { lang: 'ja', file: 'lang-template-ja-JP.json' }
            };
            // narrow down which language is used or use en
            var language = languages[Object.keys(languages).reduce(function(previous, current) {
              return $stateParams.lang === current ? current : previous;
            })];
            gettextCatalog.setCurrentLanguage(language.lang);
            if (language !== languages.en) {
              tmhDynamicLocale.set(language.lang);
              return gettextCatalog.loadRemote('i18n/' + language.file);
            }
          }
        }
      },
      'designer.home': {
        url: '/home',
        views: {
          '@designer': {
            controller: 'HomeCtrl',
            templateUrl: 'js/home/home.html'
          }
        }
      },
      'designer.layout': {
        url: '/layouts/:id',
        /* @ngInject */
        controller: ($state, $stateParams) => $state.go('designer.page', $stateParams, { location: false })
      },
      'designer.form': {
        url: '/forms/:id',
        /* @ngInject */
        controller: ($state, $stateParams) => $state.go('designer.page', $stateParams, { location: false })
      },
      'designer.page': {
        url: '/pages/:id',
        resolve: {
          /* @ngInject */
          artifact: (editorService, pageRepo, $stateParams) => editorService.initialize(pageRepo, $stateParams.id),
          /* @ngInject */
          artifactRepo: pageRepo => pageRepo,
          /* @ngInject */
          mode: () => 'page'
        },
        views: {
          // main view (ui-view in index.html)
          '@designer': {
            controller: 'EditorCtrl',
            templateUrl: 'js/editor/editor.html'
          },
          //  sub view named in editor.html
          'data@designer.page': {
            controller: 'DataCtrl',
            templateUrl: 'js/editor/data-panel/data.html'
          },
          'header@designer.page': {
            controller: 'EditorHeaderCtrl',
            controllerAs: 'vm',
            templateUrl: 'js/editor/header/header.html'
          }
        }
      },
      'designer.page.asset': {
        views: {
          //  sub view named in editor.html
          'data@designer.page': {
            controller: 'AssetCtrl',
            controllerAs: 'vm',
            templateUrl: 'js/assets/page-assets.html'
          }
        }
      },
      'designer.page.preview': {
        url: '/preview',
        views: {
          '@designer': {
            controller: 'PreviewCtrl',
            templateUrl: 'js/preview/preview.html'
          }
        },
        resolve: {
          /* @ngInject */
          iframeParameters: function($stateParams) {
            return {
              url: 'preview/page',
              id: $stateParams.id
            };
          },
          // injects the correct repo
          /* @ngInject */
          artifactRepo: function(pageRepo) {
            return pageRepo;
          }
        }
      },
      'designer.widget': {
        url: '/widget/:id',
        resolve: {
          /* @ngInject */
          artifact: function(widgetRepo, $stateParams) {
            return widgetRepo.load($stateParams.id).then(function(response) {
              return response.data;
            });
          },
          /* @ngInject */
          artifactRepo: function(widgetRepo) {
            return widgetRepo;
          },
          /* @ngInject */
          mode: function() {
            return 'widget';
          }
        },
        views: {
          // main view (ui-view in index.html)
          '@designer': {
            controller: 'CustomWidgetEditorCtrl',
            templateUrl: 'js/custom-widget/custom-widget-editor.html'
          },
          //  sub view named in editor.html
          'asset@designer.widget': {
            controller: 'AssetCtrl',
            controllerAs: 'vm',
            templateUrl: 'js/assets/widget-assets.html'
          }
        }
      }
    });
})();
