const {task, series, src, dest} = require('gulp');
const {config} = require('./config');
const { buildWidget } = require('widget-builder/src/index.js');
var Server = require('karma').Server;


/**
 * Task to build widget directives for tests.
 */
task('test:widgets', function() {
  return src(config.paths.widgetsJson)
    .pipe(buildWidget())
    .pipe(dest('target/widget-directives'));
});

/**
 * Task to run unit tests.
 */
task('test', series('test:widgets', function(done) {
  return test(done);
}));

task('test:datepicker', series('test:widgets', function(done) {
  process.argv.push('--specs=src/test/javascript/spec/widgets/pbDatePicker.spec.js');
  return test(done);
}));

function test(done, watch) {
  return new Server({
    configFile: config.paths.karma.configFile,
    singleRun: !watch
  }, function (exitCode) {
    if(exitCode === 0){
      done();
    } else {
      process.exit(exitCode);
    }
  }).start();
}
