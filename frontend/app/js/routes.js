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

  'use strict';

  angular
    .module('bonitasoft.designer.route', ['tmh.dynamicLocale', 'ui.router'])
    .config((tmhDynamicLocaleProvider, $stateProvider) => {
      tmhDynamicLocaleProvider.localeLocationPattern('locales/angular-locale_{{locale}}.js');

      $stateProvider.state('designer', {
        abstract: true,
        url: '/:lang',
        template: '<ui-view></ui-view>',
        resolve: {
          /* @ngInject */
          language: function ($stateParams, gettextCatalog, tmhDynamicLocale) {
            var languages = {
              'en': {lang: 'en'},
              'fr': {lang: 'fr', files: ['lang-template-fr-FR.json', 'widgets-lang-template-fr-FR.json']},
              'es': {lang: 'es-ES', files: ['lang-template-es-ES.json', 'widgets-lang-template-es-ES.json']},
              'es-ES': {lang: 'es-ES', files: ['lang-template-es-ES.json', 'widgets-lang-template-es-ES.json']},
              'ja': {lang: 'ja', files: ['lang-template-ja-JP.json', 'widgets-lang-template-ja-JP.json']},
              'ja-JP': {lang: 'ja', files: ['lang-template-ja-JP.json', 'widgets-lang-template-ja-JP.json']},
              'pt': {lang: 'pt-BR', files: ['lang-template-pt-BR.json', 'widgets-lang-template-pt-BR.json']},
              'pt-BR': {lang: 'pt-BR', files: ['lang-template-pt-BR.json', 'widgets-lang-template-pt-BR.json']},
              'pt_BR': {lang: 'pt-BR', files: ['lang-template-pt-BR.json', 'widgets-lang-template-pt-BR.json']}
            };
            // narrow down which language is used or use en
            var language = languages[Object.keys(languages).reduce(function (previous, current) {
              return $stateParams.lang === current ? current : previous;
            })];
            gettextCatalog.setCurrentLanguage(language.lang);
            if (language !== languages.en) {
              tmhDynamicLocale.set(language.lang);
              language.files.forEach(file => gettextCatalog.loadRemote('i18n/' + file));
            }
          }
        }
      });

      $stateProvider.state('designer.home', {
        url: '/home',
        views: {
          '@designer': {
            controller: 'HomeCtrl',
            templateUrl: 'js/home/home.html'
          }
        }
      });

      $stateProvider.state('designer.layout', {
        url: '/layouts/:id',
        /* @ngInject */
        controller: ($state, $stateParams) => $state.go('designer.page', $stateParams, {location: false})
      });

      $stateProvider.state('designer.form', {
        url: '/forms/:id',
        /* @ngInject */
        controller: ($state, $stateParams) => $state.go('designer.page', $stateParams, {location: false})
      });

      $stateProvider.state('designer.page', {
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
          'data@designer.page': {
            controller: 'DataCtrl',
            templateUrl: 'js/editor/bottom-panel/data-panel/data.html'
          },
          'header@designer.page': {
            controller: 'EditorHeaderCtrl',
            controllerAs: 'vm',
            templateUrl: 'js/editor/header/header.html'
          }
        }
      });

      $stateProvider.state('designer.page.asset', {
        views: {
          //  sub view named in editor.html
          'data@designer.page': {
            controller: 'AssetCtrl',
            controllerAs: 'vm',
            templateUrl: 'js/assets/page-assets.html'
          }
        },
        resolve: {
          assetRepo: (AssetRepository, pageRepo) => new AssetRepository(pageRepo.baseUrl)
        }
      });

      $stateProvider.state('designer.page.webResources', {
        views: {
          'data@designer.page': {
            controller: 'WebResourcesCtrl',
            controllerAs: 'ctrl',
            templateUrl: 'js/editor/bottom-panel/web-resources-panel/webResources-panel.html'
          },
        },
        resolve: {
          artifactRepo: pageRepo => pageRepo,
        }
      });

      $stateProvider.state('designer.preview', {
        url: '/preview/:mode/:id?resolution',
        views: {
          '@designer': {
            controller: 'PreviewCtrl',
            templateUrl: 'js/preview/preview.html'
          }
        },
        resolve: {
          /* @ngInject */
          iframeParameters: $stateParams => ({url: `preview/${$stateParams.mode}`, id: $stateParams.id}),
          /* @ngInject */
          mode: $stateParams => $stateParams.mode,
          // injects the correct repo
          /* @ngInject */
          artifactRepo: (repositories, mode) => repositories.get(mode)
        }
      });

      $stateProvider.state('designer.widget', {
        url: '/widget/:id',
        resolve: {
          /* @ngInject */
          artifact: function (widgetRepo, $stateParams, migration, alerts, $q) {
            let id = $stateParams.id;
            if (!id.startsWith('custom')) {
              return widgetRepo.load(id).then(function (response) {
                return response.data;
              });
            } else {
              return widgetRepo.migrationStatus(id)
                .then((response) => {
                  return migration.handleMigrationStatus(id, response.data).then(() => {
                    if (response.data.migration) {
                      widgetRepo.migrate(id).then(response => migration.handleMigrationNotif(id, response.data));
                    }
                  });
                })
                .then(() => {
                  return widgetRepo.load(id);
                })
                .then((response) => {
                  return response.data;
                })
                .catch((error) => {
                  if (error.message && error.status !== 422) {
                    alerts.addError(error.message);
                  }
                  return $q.reject(error);
                });
            }
          },
          /* @ngInject */
          artifactRepo: function (widgetRepo) {
            return widgetRepo;
          },
          /* @ngInject */
          mode: function () {
            return 'widget';
          },
          assetRepo: (AssetRepository, widgetRepo) => new AssetRepository(widgetRepo.baseUrl)
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
          },
          'data@designer.widget': {
            controller: 'WebResourcesCtrl',
            controllerAs: 'ctrl',
            templateUrl: 'js/editor/bottom-panel/web-resources-panel/webResources-panel.html'
          }
        }
      });

      $stateProvider.state('designer.fragment', {
        url: '/fragments/:id',
        resolve: {
          /* @ngInject */
          artifact: function (editorService, fragmentRepo, $stateParams) {
            return editorService.initialize(fragmentRepo, $stateParams.id);
          },
          // injects the correct repo for a page or a fragment
          /* @ngInject */
          artifactRepo: function (fragmentRepo) {
            return fragmentRepo;
          },
          /* @ngInject */
          mode: function () {
            return 'fragment';
          }
        },
        views: {
          // main view (ui-view in index.html)
          '@designer': {
            controller: 'EditorCtrl',
            templateUrl: 'js/editor/editor.html'
          },
          //  sub view named in editor.html
          'data@designer.fragment': {
            controller: 'DataCtrl',
            templateUrl: 'js/editor/bottom-panel/data-panel/data.html'
          },
          'header@designer.fragment': {
            controller: 'EditorHeaderCtrl',
            controllerAs: 'vm',
            templateUrl: 'js/editor/header/header.html'
          }
        }
      });
      $stateProvider.state('designer.fragment.webResources', {
        views: {
          'data@designer.fragment': {
            controller: 'WebResourcesCtrl',
            controllerAs: 'ctrl',
            templateUrl: 'js/editor/bottom-panel/web-resources-panel/webResources-panel.html'
          },
        },
        resolve: {
          artifactRepo: fragmentRepo => fragmentRepo,
        }
      });
    })
  ;
})();
