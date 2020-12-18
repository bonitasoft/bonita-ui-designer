const mkdirp = require('mkdirp');
const protractor = require('gulp-protractor').protractor;
const gulp = require('gulp');
const config = require('./config.js');
const serve = require('./serve.js');
const build = require('./build.js');
const bundle = require('./bundle.js');
const index = require('./index.js');
let paths = config.paths;

function e2eReportScafold(done) {
  mkdirp('build/reports/e2e-tests', done);
}

/**
 * e2e Tests
 */

const run = gulp.series(e2eReportScafold, build.buildAll, bundle.e2e, index.e2e, function _e2e() {
  let server = serve.serverE2e(paths);

  return gulp.src(['../test/e2e/spec/*.spec.js'])
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

exports.run = run;
