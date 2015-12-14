var ddescriber = require('../../../frontend/gulp/ddescriber.js');
var plumber = require('gulp-plumber');
var concat = require('gulp-concat');
var ngAnnotate = require('gulp-ng-annotate');
var uglify = require('gulp-uglify');
var sourcemaps = require('gulp-sourcemaps');
var html2js = require('gulp-ng-html2js');
var merge = require('merge-stream');
var gulpIf = require('gulp-if');
var order = require('gulp-order');
var jshint = require('gulp-jshint');
var jscs = require('gulp-jscs');
var babel = require('gulp-babel');

var gettextWidget = require('./gettext-widget.js');
var buildWidget = require('./widget-builder/src/index.js');

module.exports = function(gulp, config) {

  var paths = config.paths;

  gulp.task('build', ['runtime', 'widgets', 'pot']);
  gulp.task('lint', ['jshint', 'jscs:lint']);

  /**
   * Check for ddescribe and iit
   */
  gulp.task('ddescriber', function () {
    return gulp.src(paths.tests)
      .pipe(ddescriber());
  });

  /**
   * Task to inline add widgets to the webapp for production and inline templates in json file
   */
  gulp.task('widgets', function () {
    return gulp.src(paths.widgetsJson)
      .pipe(buildWidget())
      .pipe(gulp.dest(paths.dest.json));
  });

  gulp.task('pot', function () {
    return gulp.src(paths.widgetsJson)
      .pipe( gettextWidget.prepare() )
      .pipe(concat('widgets.json', {newLine: ','}))
      .pipe( gettextWidget.extract() )
      .pipe(gulp.dest('target/po'));
  });

  gulp.task('jshint', function () {
    return gulp.src(paths.runtime)
      .pipe(jshint())
      .pipe(jshint.reporter('jshint-stylish'))
      .pipe(jshint.reporter('fail'));
  });

  gulp.task('jscs:lint', function() {
    return gulp.src(paths.runtimeFolder + '/**/*.js')
      .pipe(jscs())
      .pipe(jscs.reporter())
      .pipe(jscs.reporter('fail'));
  });

  gulp.task('jscs:format', function () {
    return gulp.src(paths.runtimeFolder + '/**/*.js')
      .pipe(jscs({fix: true}))
      .pipe(jscs.reporter())
      .pipe(jscs.reporter('fail'))
      .pipe(gulp.dest(paths.runtimeFolder));
  });

  /**
   * js task, concatenate and minimify vendor js files
   */
  gulp.task('runtime', ['runtime:js', 'runtime:css', 'runtime:fonts', 'vendor']);

  gulp.task('vendor', function () {
    function notMinified(file) {
      return !/(src-min|\.min\.js)/.test(file.path);
    }
    return gulp.src(paths.vendor)
      .pipe(concat('vendor.min.js'))
      .pipe(gulpIf(notMinified, uglify()))
      .pipe(gulp.dest(paths.dest.vendors));
  });

  gulp.task('runtime:css', function () {
    return gulp.src(paths.css)
      .pipe(gulp.dest(paths.dest.css));
  });

  gulp.task('runtime:fonts', function () {
    return gulp.src(paths.fonts)
      .pipe(gulp.dest(paths.dest.fonts));
  });

  /**
   * js task, concatenate and minimify js files
   */
  gulp.task('runtime:js', function () {
    var tpl = gulp.src(paths.templates)
      .pipe(html2js({
        moduleName: 'bonitasoft.ui.templates'
      }));

    var app = gulp.src(paths.runtime)
      .pipe(plumber())
      .pipe(order([
        '**/*.module.js',
        '**/*.js'
      ]))
      .pipe(sourcemaps.init())
      .pipe(babel())
      .pipe(ngAnnotate({
        'single_quotes': true,
        add: true
      }));

    return merge(app, tpl)
      .pipe(concat('runtime.min.js'))
      .pipe(uglify())
      .pipe(sourcemaps.write('.'))
      .pipe(gulp.dest(paths.dest.js));
  });
};
