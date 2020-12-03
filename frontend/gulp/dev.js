module.exports = function(gulp, config) {

  require('./serve.js')(gulp, config);
  require('./index.js')(gulp, config);
  const { watch } = require('gulp');

  var paths = config.paths;

  gulp.task('watch', function() {
    gulp.watch(paths.js, gulp.series('bundle:js'));
    gulp.watch(paths.templates, gulp.series('bundle:js'));
    gulp.watch(['app/index.html'], gulp.series('index:dev'));
    gulp.watch(paths.less, gulp.series('bundle:css'));
    gulp.watch(paths.assets.icons, gulp.series('bundle:icons'));
  });

  gulp.task('serve:dev', gulp.series('bundle', 'assets', 'index:dev', function serve_dev() {
    browserSyncInit(paths.dev, [
      paths.dev + '/**/*.js',
      paths.dev + '/**/*.css'
    ], 'index.html');
  }));

  gulp.task('dev', gulp.series('clean', 'watch', function _dev() {
    config.devMode = true;
    return gulp.start('serve:dev');
  }));

};
