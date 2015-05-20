'use strict';

var gulp = require('gulp');
var concat = require('gulp-concat');
var ngAnnotate = require('gulp-ng-annotate');
var uglify = require('gulp-uglify');
var shell = require('gulp-shell');
var sourcemaps = require('gulp-sourcemaps');
var fs = require('fs');
var inlineJSON = require('./src/build/widgets/inlineJSON');
var buildDirective = require('./src/build/widgets/buildDirective');
var karma = require('karma').server;
var ddescriber = require("../../frontend/gulp/ddescriber.js");

var merge = require('merge-stream');
var html2js = require('gulp-ng-html2js');

var paths = {
  htmlTemplates: [
    'src/main/resources/templates/*.html'
  ],
  generator: ['src/main/generator/**/*.js'],
  vendor: [
    'bower_components/jquery/dist/jquery.min.js',
    'bower_components/angular/angular.min.js',
    'bower_components/angular-sanitize/angular-sanitize.min.js',
    'bower_components/angular-messages/angular-messages.min.js',
    'bower_components/ng-file-upload/angular-file-upload.min.js',
    'bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
    'bower_components/lodash/lodash.min.js'
  ],
  fonts: [
    'bower_components/bootstrap/dist/fonts/*.*'
  ],
  css: [
    'bower_components/bootstrap/dist/css/bootstrap.min.css',
    'src/main/generator/css/**.css'
  ],
  JSONs: ['src/main/resources/widgets/**/*.json'],
  widgets: ['src/main/resources/widgets/**/*.js', 'src/main/resources/widgets/**/*.html', 'src/main/resources/widgets/**/*.json'],
  karma: { configFile: __dirname + '/src/test/javascript/karma.conf.js' }
};

/**
 * Task to run unit tests.
 */
gulp.task('test', ['widgets:directives'], function () {
  return karma.start({
    configFile: paths.karma.configFile,
    singleRun: true
  });
});

/**
 * Task to run unit tests TDD style.
 */
gulp.task('test-watch', ['widgets:directives', 'watch:widgets:directives'], function () {
  return karma.start({
    configFile: paths.karma.configFile,
    singleRun: false
  });
});

/**
 * Task to build widget directives for tests.
 */
gulp.task('widgets:directives', function () {
  return gulp.src(paths.JSONs)
    .pipe(inlineJSON())
    .pipe(buildDirective())
    .pipe(gulp.dest('target/widget-directives'));
});


/**
 * Task to build widget directives on change.
 */
gulp.task('watch:widgets:directives', function () {
  gulp.watch(paths.widgets, ['widgets:directives']);
});

/**
 * Serve task
 * Watch js and html files and launch jetty, without automatic reloading
 */
gulp.task('serve', ['widgets', 'generator', 'html', 'watch'], shell.task('mvn jetty:run -Djetty.reload=manual'));

gulp.task('generator:css', function () {
  return gulp.src(paths.css)
    .pipe(gulp.dest('target/classes/META-INF/resources/generator/css'));
});

gulp.task('generator:fonts', function () {
  return gulp.src(paths.fonts)
    .pipe(gulp.dest('target/classes/META-INF/resources/generator/fonts'));
});

/**
 * js task, concatenate and minimify vendor js files
 */
gulp.task('vendor', function () {
  return gulp.src(paths.vendor)
    .pipe(concat('vendor.min.js'))
    .pipe(uglify())
    .pipe(gulp.dest('target/classes/META-INF/resources/generator/js'));
});

/**
 * js task, concatenate and minimify js files
 */
gulp.task('generator', ['generator:css', 'generator:fonts', 'vendor'], function () {
  var tpl = gulp.src('src/main/generator/templates/*.html')
    .pipe(html2js({
      moduleName: 'org.bonitasoft.pagebuilder.generator.templates'
    }));

  var app = gulp.src(paths.generator)
    .pipe(ngAnnotate({
      single_quotes: true,
      add: true
    }));

  return merge(app, tpl)
    .pipe(sourcemaps.init())
    .pipe(concat('generator.min.js'))
    .pipe(uglify())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest('target/classes/META-INF/resources/generator/js'));
});

/**
 * html task, just updating the templates used by jetty
 */
gulp.task('html', function () {
  return gulp.src(paths.htmlTemplates).pipe(gulp.dest('target/classes/templates/'));
});

/**
 * Task to inline JSON and add them to the webapp for production.
 */
gulp.task('widgets', function () {
  return gulp.src(paths.JSONs)
    .pipe(inlineJSON())
    .pipe(gulp.dest('target/classes/widgets'));
});

/**
 * Task to inline json and build a widgets repository for development.
 */
gulp.task('widgets:repository', function () {
  // only copy widgets if the repository exist to let
  // the application create and build them the first time.
  if (fs.existsSync('target/widgets-repository')) {
    gulp.src(paths.JSONs)
      .pipe(inlineJSON())
      .pipe(gulp.dest('target/widgets-repository'));
  }
});

/**
 * Watch task.
 */
gulp.task('watch', function () {
  gulp.watch(paths.css, ['generator:css']);
  gulp.watch(paths.generator, ['generator']);
  gulp.watch(paths.widgets, ['widgets:repository', 'widgets:directives']);
  gulp.watch(paths.htmlTemplates, ['html']);
});

/**
 * Check for ddescribe and iit
 */
gulp.task('ddescriber', function () {
  return gulp.src('src/test/**/*.spec.js')
    .pipe(ddescriber());
});

/**
 * Default task
 * Run by 'npm run build' called by maven build
 */
gulp.task('default', ['ddescriber', 'test', 'generator', 'widgets']);
