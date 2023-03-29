const paths = {
  dev: 'build/dev',
  test: 'build/test',
  dist: 'build/dist',
  vendor: [
    'node_modules/jquery/dist/jquery.min.js',
    'node_modules/moment/min/moment.min.js',
    'node_modules/angular/angular.min.js',
    'node_modules/angular-sanitize/angular-sanitize.min.js',
    'node_modules/angular-ui-router/release/angular-ui-router.min.js',
    'node_modules/angular-recursion/angular-recursion.min.js',
    'node_modules/angular-bootstrap/ui-bootstrap-tpls.min.js',
    'node_modules/angular-ui-validate/dist/validate.js',
    'node_modules/angular-ui-ace/src/ui-ace.js',
    'node_modules/ace-builds/src-min-noconflict/ace.js',
    'node_modules/ace-builds/src-min-noconflict/ext-language_tools.js',
    'node_modules/ace-builds/src-min-noconflict/ext-searchbox.js',
    'node_modules/angular-cookies/angular-cookies.min.js',
    'node_modules/ngUpload/ng-upload.min.js',
    'node_modules/bonita-js-components/dist/bonita-lib-tpl.min.js',
    'node_modules/angular-gettext/dist/angular-gettext.min.js',
    'node_modules/stomp-websocket/lib/stomp.min.js',
    'node_modules/sockjs/sockjs.min.js',
    'node_modules/mousetrap/mousetrap.js',
    'node_modules/mousetrap/plugins/global-bind/mousetrap-global-bind.js',
    'node_modules/mousetrap/plugins/pause/mousetrap-pause.js',
    'node_modules/angular-moment/angular-moment.min.js',
    'node_modules/angular-dynamic-locale/tmhDynamicLocale.min.js',
    'node_modules/jsSHA/src/sha1.js',
    'node_modules/identicon.js/pnglib.js',
    'node_modules/identicon.js/identicon.js',
    'node_modules/angular-sha/src/angular-sha.js',
    'node_modules/angular-switcher/dist/angular-switcher.min.js',
    'node_modules/ngstorage/ngStorage.min.js',
    'node_modules/angular-resizable/angular-resizable.min.js',
    'node_modules/angular-animate/angular-animate.min.js',
    'node_modules/@webcomponents/webcomponentsjs/webcomponents-bundle.js',
    'node_modules/@webcomponents/webcomponentsjs/custom-elements-es5-adapter.js',
    'node_modules/@bonitasoft/query-selector/lib/query-selector.es5.min.js',
    'node_modules/angular-filter/dist/angular-filter.min.js'
  ],
  css: [
    'node_modules/angular-switcher/dist/angular-switcher.min.css',
    'node_modules/angular-resizable/angular-resizable.min.css'
  ],
  jsFolder: 'app/js',
  js: [
    'app/js/**/*.js'
  ],
  templates: [
    'app/js/**/*.html'
  ],
  less: [
    'app/**/*.less'
  ],
  e2e: [
    'node_modules/angular-mocks/angular-mocks.js',
    'test/e2e/config/*.js',
    'test/e2e/polyfill/dnd.js'
  ],
  tests: 'test/**/*.spec.js',
  testFiles: 'test/**/*.js',
  testFolder: 'test',
  karma: process.cwd() + '/test/karma.conf.js',
  assets: {
    fonts: [
      'app/fonts/*.*',
      'node_modules/font-awesome/fonts/*.*',
      'node_modules/bootstrap/fonts/*.*'
    ],
    ace: [
      'node_modules/ace-builds/src-min-noconflict/{mode,worker}-{html,javascript,json,css}.js'
    ],
    licences: [
      'licences/**/*.*'
    ],
    images: [
      'app/img/*.*'
    ],
    icons: [
      'app/img/*.svg'
    ],
    fontIconTemplate: 'app/css/icons.css',
    favicon: [
      'app/favicon.ico'
    ]
  },
  locales: [
    'en',
    'es-ES',
    'fr',
    'ja',
    'pt-br'
  ].map(function(lang) {
    return 'node_modules/angular-i18n/angular-locale_' + lang.toLowerCase() + '.js';
  })
};

const banner = [
  '/**',
  ' * Copyright (C) 2015 Bonitasoft S.A.',
  ' * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble',
  ' * This program is free software: you can redistribute it and/or modify',
  ' * it under the terms of the GNU General Public License as published by',
  ' * the Free Software Foundation, either version 2.0 of the License, or',
  ' * (at your option) any later version.',
  ' * This program is distributed in the hope that it will be useful,',
  ' * but WITHOUT ANY WARRANTY; without even the implied warranty of',
  ' * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the',
  ' * GNU General Public License for more details.',
  ' * You should have received a copy of the GNU General Public License',
  ' * along with this program. If not, see <http://www.gnu.org/licenses/>.',
  ' */',
  ''].join('\n');

exports.paths = paths;
exports.banner = banner;
exports.timestamp = Date.now();
exports.protractor = { port: process.env.UID_PROTRACTOR_PORT || 12001 };
