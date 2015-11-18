module.exports = function(gulp, config) {

  require('./serve.js')(gulp, config);

  var paths = config.paths;

  /**
   * Index file
   */
  gulp.task('index:dev', function () {
    return gulp.src('app/index.html')
      .pipe(gulp.dest(config.paths.dev));
  });

  gulp.task('watch', function() {
    gulp.watch(paths.js, ['bundle:js']);
    gulp.watch(paths.templates, ['bundle:js']);
    gulp.watch(['app/index.html'], ['index:dev']);
    gulp.watch(paths.less, ['bundle:css']);
    gulp.watch(paths.images, ['bundle:icons']);
  });

  gulp.task('dev', ['clean', 'watch'], function() {
    config.devMode = true;
    return gulp.start('serve:dev');
  });

};
