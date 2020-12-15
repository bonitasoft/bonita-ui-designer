const htmlreplace = require('gulp-html-replace');
const gulp = require('gulp');
const config = require('./config.js');

let timestamp = config.timestamp;

/**
 * Index file
 */

function index_dev() {
  return gulp.src('app/index.html')
    .pipe(gulp.dest(config.paths.dev));
}

function index_e2e() {
  return gulp.src('app/index.html')
    .pipe(htmlreplace({
      'js': 'js/page-builder-' + timestamp + '.min.js',
      'vendors': 'js/vendors-' + timestamp + '.min.js',
      'css': 'css/page-builder-' + timestamp + '.min.css',
      'e2e': 'js/e2e.js'
    }))
    .pipe(gulp.dest(config.paths.test));
}

exports.e2e = index_e2e;
exports.dev = index_dev;
