const gulp = require('gulp');
const config = require('./config.js');

let paths = config.paths;

function locales() {
  return gulp.src(paths.locales)
    .pipe(gulp.dest(paths.dist + '/locales'));
}

function font() {
  return gulp.src(paths.assets.fonts)
    .pipe(gulp.dest(paths.dist + '/fonts'));
}

function ace() {
  return gulp.src(paths.assets.ace)
    .pipe(gulp.dest(paths.dist + '/js'));
}

function images() {
  return gulp.src(paths.assets.images)
    .pipe(gulp.dest(paths.dist + '/img'));
}

function licences() {
  return gulp.src(paths.assets.licences)
    .pipe(gulp.dest(paths.dist));
}

function favicon() {
  return gulp.src(paths.assets.favicon)
    .pipe(gulp.dest(paths.dist));
}

exports.copy = gulp.series(font, ace, images, licences, favicon, locales);
