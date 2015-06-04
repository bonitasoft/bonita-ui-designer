var ddescriber = require("../../../frontend/gulp/ddescriber.js");
var concat = require('gulp-concat');
var ngAnnotate = require('gulp-ng-annotate');
var uglify = require('gulp-uglify');
var sourcemaps = require('gulp-sourcemaps');
var html2js = require('gulp-ng-html2js');
var merge = require('merge-stream');
var inlineJSON = require('./utils.js').inlineJSON;

module.exports = function(gulp, config) {

  var paths = config.paths;

  gulp.task('build', ['generator', 'widgets']);

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
    return gulp.src(paths.widgets.concat('!**/*.ctrl.js', '!**/*.tpl.html'))
      .pipe(inlineJSON())
      .pipe(gulp.dest(paths.dest.json));
  });

  /**
   * js task, concatenate and minimify vendor js files
   */
  gulp.task('generator', ['generator:js', 'generator:css', 'generator:fonts', 'vendor']);

  gulp.task('vendor', function () {
    return gulp.src(paths.vendor)
      .pipe(concat('vendor.min.js'))
      .pipe(uglify())
      .pipe(gulp.dest(paths.dest.vendors));
  });

  gulp.task('generator:css', function () {
    return gulp.src(paths.css)
      .pipe(gulp.dest(paths.dest.css));
  });

  gulp.task('generator:fonts', function () {
    return gulp.src(paths.fonts)
      .pipe(gulp.dest(paths.dest.fonts));
  });

  /**
   * js task, concatenate and minimify js files
   */
  gulp.task('generator:js', function () {
    var tpl = gulp.src(paths.templates)
      .pipe(html2js({
        moduleName: 'pb.generator.templates'
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
      .pipe(gulp.dest(paths.dest.js));
  });
};
