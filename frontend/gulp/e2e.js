const mkdirp = require('mkdirp');
const protractor = require('gulp-protractor').protractor;

module.exports = function(gulp, config) {

  let serveE2e = require('./serve.js')(gulp, config).serveE2e;
  require('./build.js')(gulp, config);

  let paths = config.paths;

  gulp.task('e2e:ReportScafold', function(done) {
    mkdirp('build/reports/e2e-tests', done);
  });

  /**
   * e2e Tests
   */
  gulp.task('e2e', gulp.series('e2e:ReportScafold', 'build', 'bundle:e2e', 'index:e2e', function _e2e() {
    let server = serveE2e(paths);

    return gulp.src(["../test/e2e/spec/*.spec.js"])
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
  }));

};
