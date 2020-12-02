var plumber = require('gulp-plumber');
var concat = require('gulp-concat');
var htmlreplace = require('gulp-html-replace');
var rename = require('gulp-rename');
var mkdirp = require('mkdirp');
var babel = require('gulp-babel');
var protractor = require('gulp-protractor').protractor;
var order = require('gulp-order');

module.exports = function(gulp, config) {

  var serveE2e = require('./serve.js')(gulp, config).serveE2e;
  require('./build.js')(gulp, config);

  var paths = config.paths;
  var timestamp = config.timestamp;

  gulp.task('e2e:ReportScafold', function(done) {
    mkdirp('build/reports/e2e-tests', done);
  });

  /**
   * e2e Tests
   */
  gulp.task('e2e', gulp.series('e2e:ReportScafold', 'build', 'bundle:e2e', 'index:e2e'), function () {
    var server = serveE2e(paths);

    return gulp.src([])
      .pipe(protractor({
        configFile: 'test/e2e/protractor.conf.js',
        args: ['--baseUrl', 'http://localhost:' + config.protractor.port]
      }))
      .on('error', function (e) {
        throw e;
      })
      .on('end', function () {
        server.close();
      });
  });

};
