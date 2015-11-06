var buildWidget = require('./widget-builder/src/index');
var karma = require('karma').server;

module.exports = function(gulp, config) {

  var paths = config.paths;

  function test(watch) {
    return karma.start({
      configFile: paths.karma.configFile,
      singleRun: !watch
    });
  }

  /**
   * Task to run unit tests.
   */
  gulp.task('test', ['test:widgets'], function() {
    return test();
  });

  gulp.task('test:datepicker', ['test:widgets'], function() {
    process.argv.push("--specs=src/test/javascript/spec/widgets/pbDatePicker.spec.js");
    return test();
  });

  /**
   * Task to run unit tests TDD style.
   */
  gulp.task('test:watch', ['test:widgets', 'test:watch:widgets'], function() {
    return test(true);
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
