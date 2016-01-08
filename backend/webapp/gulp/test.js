var buildWidget = require('./widget-builder/src/index');
var Server = require('karma').Server;

module.exports = function(gulp, config) {

  var paths = config.paths;

  function test(done, watch) {
    return new Server({
      configFile: paths.karma.configFile,
      singleRun: !watch
    }, done).start();
  }

  /**
   * Task to run unit tests.
   */
  gulp.task('test', ['test:widgets'], function(done) {
    return test(done);
  });

  gulp.task('test:datepicker', ['test:widgets'], function(done) {
    process.argv.push('--specs=src/test/javascript/spec/widgets/pbDatePicker.spec.js');
    return test(done);
  });

  /**
   * Task to run unit tests TDD style.
   */
  gulp.task('test:watch', ['test:widgets', 'test:watch:widgets'], function(done) {
    return test(done, true);
  });

  /**
   * Task to build widget directives for tests.
   */
  gulp.task('test:widgets', function() {
    return gulp.src(paths.widgetsJson)
      .pipe(buildWidget())
      .pipe(gulp.dest('target/widget-directives'));
  });

  /**
   * Task to build widget directives on change.
   */
  gulp.task('test:watch:widgets', function() {
    gulp.watch(paths.widgets, ['test:widgets']);
  });
};
