module.exports = function(gulp, config) {

  require('./serve.js')(gulp, config);
  require('./index.js')(gulp, config);

  var paths = config.paths;

  gulp.task('watch', function() {
    gulp.watch(paths.js, ['bundle:js']);
    gulp.watch(paths.templates, ['bundle:js']);
    gulp.watch(['app/index.html'], ['index:dev']);
    gulp.watch(paths.less, ['bundle:css']);
    gulp.watch(paths.images, ['bundle:icons']);
  });

  gulp.task('serve:dev', gulp.series('bundle', 'assets', 'index:dev'), function () {
    browserSyncInit(paths.dev, [
      paths.dev + '/**/*.js',
      paths.dev + '/**/*.css'
    ], 'index.html');
  });

  gulp.task('dev', gulp.series('clean', 'watch'), function() {
    config.devMode = true;
    return gulp.start('serve:dev');
  });

};
