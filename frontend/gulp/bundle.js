module.exports = function (gulp, config) {

  /**
   * Concatenate e2e libs
   */
  gulp.task('bundle:e2e', function () {
    return gulp.src(paths.e2e)
      .pipe(plumber())
      .pipe(order([
        '**/*.module.js',
        '**/*.js'
      ]))
      .pipe(babel())
      .pipe(concat('e2e.js'))
      .pipe(gulp.dest(paths.test + '/js'));
  });

};
