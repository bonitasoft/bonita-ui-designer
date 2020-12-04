module.exports = function(gulp, config) {

  require('./serve.js')(gulp, config);
  require('./index.js')(gulp, config);
  var log = require('fancy-log');

  var paths = config.paths;

  gulp.task('watch', function(done) {
    gulp.watch(paths.js, gulp.series('bundle:js'));
    gulp.watch(paths.templates, gulp.series('bundle:js'));
    gulp.watch(['app/index.html'], gulp.series('index:dev'));
    gulp.watch(paths.less, gulp.series('bundle:css'));
    gulp.watch(paths.assets.icons, gulp.series('bundle:icons'));
    done();
  });

  gulp.task('setting:dev', function _dev(done) {
    config.devMode = true;
    done();
  });

  gulp.task('dev', gulp.series('clean', 'watch', 'setting:dev', 'serve:dev'));

};
