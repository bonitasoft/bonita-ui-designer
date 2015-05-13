(function() {

  'use strict';

  angular
    .module('app.route', [])
    .constant('appStates', {
      'designer': {
        abstract: true,
        url: '/:lang',
        template: '<ui-view></ui-view>',
        resolve: { /* @ngInject */
          language: function($stateParams, gettextCatalog) {
            var languages = {
              'en': {lang: 'en', file: 'english.json'},
              'fr': {lang: 'fr', file: 'francais.json'}
            };
            // narrow down which language is used or use en
            var language = languages[Object.keys(languages).reduce(function(previous, current) {
              return $stateParams.lang === current ? current : previous;
            })];
            gettextCatalog.setCurrentLanguage(language.lang);
            return gettextCatalog.loadRemote('i18n/' + language.file);
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
      'designer.page': {
        url: '/pages/:id',
        resolve: {
          /* @ngInject */
          artifact: function(whiteboard, pageRepo, $stateParams) {
            return whiteboard.initialize(pageRepo, $stateParams.id);
          },
          /* @ngInject */
          artifactRepo: function(pageRepo) {
            return pageRepo;
          },
          /* @ngInject */
          mode: function() {
            return 'page';
          }
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
          'palette@designer.page': {
            controller: 'PaletteCtrl',
            templateUrl: 'js/editor/palette/palette.html'
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
        url: '/widget/:widgetId',
        controller: 'CustomWidgetEditorCtrl',
        resolve: {
          /* @ngInject */
          widget: function(widgetRepo, $stateParams) {
            return widgetRepo.getById($stateParams.widgetId).then(function(response) {
              return response.data;
            });
          }
        },
        templateUrl: 'js/custom-widget/custom-widget-editor.html'
      }
    });
})();
