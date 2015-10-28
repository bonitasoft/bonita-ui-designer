var buildWidget = require('./widget-builder/src/index');
var karma = require('karma').server;

module.exports = function(gulp, config) {

  var paths = config.paths;

  /**
   * Task to run unit tests.
   */
  gulp.task('test', ['test:widgets'], function () {
    return karma.start({
      configFile: paths.karma.configFile,
      singleRun: true
    });
  });

  /**
   * Task to run unit tests TDD style.
   */
  gulp.task('test:watch', ['test:widgets', 'test:watch:widgets'], function () {
    return karma.start({
      configFile: paths.karma.configFile,
      singleRun: false
    });
  });

  /**
   * Task to build widget directives for tests.
   */
  gulp.task('test:widgets', function () {
    return gulp.src(paths.widgetsJson)
      .pipe(buildWidget())
      .pipe(gulp.dest('target/widget-directives'));
  });

  /**
   * Task to build widget directives on change.
   */
  gulp.task('test:watch:widgets', function () {
    gulp.watch(paths.widgets, ['test:widgets']);
  });
};
