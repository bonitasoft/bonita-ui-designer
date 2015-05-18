var gulp = require('gulp');
var karma = require('karma').server;

module.exports = function(gulp, config) {

  require('./build.js')(gulp, config);

  /**
   * unit tests once and exit
   */
  gulp.task('test', ['bundle:js', 'bundle:vendors'], function (done) {
    return karma.start({
      configFile: __dirname + '/../test/karma.conf.js',
      singleRun: true
    }, done);
  });

  /**
   * unit tests in autowatch mode
   */
  gulp.task('test:watch', ['bundle:js', 'bundle:vendors'], function (done) {
    return karma.start({
      configFile: __dirname + '/../test/karma.conf.js',
      singleRun: false
    }, done);
  });
};
