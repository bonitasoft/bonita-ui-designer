'use strict';

var gulp = require('gulp');

var paths = {
  templates: [
    'src/main/generator/templates/*.html'
  ],
  generator: ['src/main/generator/**/*.js'],
  vendor: [
    'bower_components/angular/angular.min.js',
    'bower_components/angular-sanitize/angular-sanitize.min.js',
    'bower_components/ngUpload/ng-upload.min.js',
    'bower_components/angular-messages/angular-messages.min.js',
    'bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
    'bower_components/angular-gettext/dist/angular-gettext.min.js',
  ],
  fonts: [
    'bower_components/bootstrap/dist/fonts/*.*'
  ],
  css: [
    'bower_components/bootstrap/dist/css/bootstrap.min.css',
    'src/main/generator/css/**.css'
  ],
  widgets: ['src/main/resources/widgets/**/*.*'],
  widgetsJson: ['src/main/resources/widgets/**/*.json'],
  karma: { configFile: __dirname + '/src/test/javascript/karma.conf.js' },
  tests: ['src/test/**/*.spec.js'],

  dest: {
    vendors: 'target/classes/META-INF/resources/generator/js',
    css: 'target/classes/META-INF/resources/generator/css',
    fonts: 'target/classes/META-INF/resources/generator/fonts',
    js: 'target/classes/META-INF/resources/generator/js',
    json: 'target/classes/widgets'
  }
};

var config = {
  paths: paths,
  javaArgs : ''
};

require('./gulp/build.js')(gulp, config);
require('./gulp/dev.js')(gulp, config);
require('./gulp/test.js')(gulp, config);

/**
 * aliasing dev task
 */
gulp.task('serve', function() {
  return gulp.start('dev');
});

/**
 * Default task
 * Run by 'npm run build' called by maven build
 */
gulp.task('default', ['ddescriber', 'test'], function() {
  return gulp.start('build');
});

module.exports = {
  paths: paths
};
