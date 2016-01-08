var jshint = require('gulp-jshint');
var Server = require('karma').Server;

module.exports = function(gulp, config) {

  var paths = config.paths;

  gulp.task('jshint:test', function () {
    return gulp.src(paths.testFiles)
      .pipe(jshint())
      .pipe(jshint.reporter('jshint-stylish'))
      .pipe(jshint.reporter('fail'));
  });

  /**
   * unit tests once and exit
   */
  gulp.task('test', ['jshint:test'], function (done) {
    return new Server({
      configFile: config.paths.karma,
      singleRun: true
    }, done).start();
  });

  /**
   * unit tests in autowatch mode
   */
  gulp.task('test:watch', ['jshint:test'], function (done) {
    return  new Server({
      configFile: config.paths.karma,
      singleRun: false
    }, done).start();
  });
};
