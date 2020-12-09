const jshint = require('gulp-jshint');
const Server = require('karma').Server;

module.exports = function(gulp, config) {

  let paths = config.paths;

  gulp.task('jshint:test', function () {
    return gulp.src(paths.testFiles)
      .pipe(jshint())
      .pipe(jshint.reporter('jshint-stylish'))
      .pipe(jshint.reporter('fail'));
  });

  /**
   * unit tests once and exit
   */
  gulp.task('test', gulp.series('jshint:test', function _test(done) {
    return new Server({
      configFile: config.paths.karma,
      singleRun: true
    }, done).start();
  }));

  /**
   * unit tests in autowatch mode
   */
  gulp.task('test:watch', gulp.series('jshint:test', function test_watch(done) {
    return  new Server({
      configFile: config.paths.karma,
      singleRun: false
    }, done).start();
  }));
};
