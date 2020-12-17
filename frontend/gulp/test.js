const jshint = require('gulp-jshint');
const Server = require('karma').Server;
const config = require('./config.js');
const gulp = require('gulp');

let paths = config.paths;

const through = require("through2");

/**
 * Check for ddescribe and iit
 */
function checkCompleteness() {
  return gulp.src(paths.tests)
    .pipe(checkSingleTest());
}

function checkSingleTest() {
  return through.obj(function (file, enc, cb) {
    var contents = file.contents.toString();
    var err = null;

    if (/.*ddescribe|iit|fit|fdescribe/.test(contents)) {
      err = new Error('\033[31mddescribe or iit present in file ' + file.path + '\033[0m');
    }
    cb(err, file);
  });
}

function jshint_test() {
  return gulp.src(paths.testFiles)
    .pipe(jshint())
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(jshint.reporter('fail'));
}

/**
 * unit tests once and exit
 */
const run = gulp.series(jshint_test, function _test(done) {
  return new Server({
    configFile: config.paths.karma,
    singleRun: true
  }, done).start();
});

/**
 * unit tests in autowatch mode
 */
const watch = gulp.series(jshint_test, function test_watch(done) {
  return new Server({
    configFile: config.paths.karma,
    singleRun: false
  }, done).start();
});

exports.checkCompleteness = checkCompleteness;
exports.watch = watch;
exports.run = run;
