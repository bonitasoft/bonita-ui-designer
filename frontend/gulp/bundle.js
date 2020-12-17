const plumber = require('gulp-plumber');
const concat = require('gulp-concat');
const babel = require('gulp-babel');
const order = require('gulp-order');
const gulp = require('gulp');
const config = require('./config.js');


/**
 * Concatenate e2e libs
 */
function bundle_e2e() {
  return gulp.src(config.paths.e2e)
    .pipe(plumber())
    .pipe(order([
      '**/*.module.js',
      '**/*.js'
    ]))
    .pipe(babel())
    .pipe(concat('e2e.js'))
    .pipe(gulp.dest(config.paths.test + '/js'));
}


exports.e2e = bundle_e2e;
