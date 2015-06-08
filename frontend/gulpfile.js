/* jshint node:true */
/* jshint node:true */
var gulp = require('gulp');

var paths = {
  dev: 'build/dev',
  test: 'build/test',
  dist: 'build/dist',
  vendor: [
    'app/bower_components/jquery/dist/jquery.min.js',
    'app/bower_components/moment/min/moment.min.js',
    'app/bower_components/ace-builds/src-min-noconflict/ace.js',
    'app/bower_components/ace-builds/src-min-noconflict/ext-language_tools.js',
    'app/bower_components/angular/angular.min.js',
    'app/bower_components/angular-sanitize/angular-sanitize.min.js',
    'app/bower_components/angular-ui-router/release/angular-ui-router.min.js',
    'app/bower_components/angular-recursion/angular-recursion.min.js',
    'app/bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
    'app/bower_components/angular-ui-ace/ui-ace.min.js',
    'app/bower_components/ngUpload/ng-upload.min.js',
    'app/bower_components/bonita-js-components/dist/bonita-lib-tpl.min.js',
    'app/bower_components/angular-gettext/dist/angular-gettext.min.js',
    'app/bower_components/stomp-websocket/lib/stomp.min.js',
    'app/bower_components/sockjs/sockjs.min.js',
    'app/bower_components/keymaster/keymaster.js',
    'app/bower_components/angular-moment/angular-moment.min.js'
  ],
  js: [
    'app/js/**/*.js'
  ],
  templates: [
    'app/js/**/*.html'
  ],
  less: [
    'app/less/**/*.less'
  ],
  e2e: [
    'app/bower_components/angular-mocks/angular-mocks.js',
    'test/e2e/config/e2eConfig.js',
    'test/e2e/polyfill/dnd.js'
  ],
  tests: 'test/**/*.spec.js',
  assets: {
    fonts: [
      'app/fonts/*.*',
      'app/bower_components/font-awesome/fonts/*.*',
      'app/bower_components/bootstrap/fonts/*.*'
    ],
    ace: [
      'app/bower_components/ace-builds/src-min-noconflict/{mode,worker}-{html,javascript,json}.js'
    ],
    licences: [
      'licences/**/*.*'
    ],
    images: [
      'app/img/*.*'
    ],
    favicon: [
      'app/favicon.ico'
    ]
  }
};

var banner = [
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

var config = {
  paths: paths,
  banner: banner,
  timestamp: Date.now()
};

require('./gulp/build.js')(gulp, config);
require('./gulp/test.js')(gulp, config);
require('./gulp/e2e.js')(gulp, config);
require('./gulp/dev.js')(gulp, config);
require('./gulp/serve.js')(gulp, config);

/**
 * Aliasing dev task
 */
gulp.task('serve', function() {
  gulp.start('dev');
});

gulp.task('default', ['clean', 'ddescriber'], function() {
  gulp.start(['test', 'build']);
});

module.exports = {
  paths: paths
};
