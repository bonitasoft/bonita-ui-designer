const {series, src, dest} = require('gulp');
const config = require('./config');
const { buildWidget } = require('widget-builder/src/index.js');
var Server = require('karma').Server;


/**
 * Task to build widget directives for tests.
 */
function buildWidgets() {
  return src(config.paths.widgetsJson)
    .pipe(buildWidget())
    .pipe(dest('target/widget-directives'));
}

/**
 * Task to run unit tests.
 */
const run = series(buildWidgets, function _test(done) {
  return test(done);
});

const datepicker = series(buildWidgets, function _datepicker(done) {
  process.argv.push('--specs=src/test/javascript/spec/widgets/pbDatePicker.spec.js');
  return test(done);
});

function test(done, watch) {
  return new Server({
    configFile: config.paths.karma.configFile,
    singleRun: !watch
  }, function (exitCode) {
    exitCode ? process.exit(exitCode) : done();
  }).start();
}

exports.run = run;
exports.datepicker = datepicker;
