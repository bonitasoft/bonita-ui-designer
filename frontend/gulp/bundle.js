const plumber = require('gulp-plumber');
const concat = require('gulp-concat');
const babel = require('gulp-babel');
const order = require('gulp-order');

module.exports = function (gulp, config) {

  /**
   * Concatenate e2e libs
   */
  gulp.task('bundle:e2e', function () {
    return gulp.src(config.paths.e2e)
      .pipe(plumber())
      .pipe(order([
        '**/*.module.js',
        '**/*.js'
      ]))
      .pipe(babel())
      .pipe(concat('e2e.js'))
      .pipe(gulp.dest(config.paths.test + '/js'));
  });

};
