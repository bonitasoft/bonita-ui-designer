const eslint = require('gulp-eslint');
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
    let contents = file.contents.toString();
    let err = null;

    if (/.*ddescribe|iit|fit|fdescribe/.test(contents)) {
      err = new Error('\033[31mddescribe or iit present in file ' + file.path + '\033[0m');
    }
    cb(err, file);
  });
}

function eslintTest() {
  return gulp.src(paths.testFiles)
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(eslint.failAfterError())
    .pipe(gulp.dest(paths.testFolder));
}

/**
 * unit tests once and exit
 */
const run = gulp.series(eslintTest, function _test(done) {
  return new Server({
    configFile: config.paths.karma,
    singleRun: true
  }, function (exitCode) {
    exitCode ? process.exit(exitCode) : done();
  }).start();
});

/**
 * unit tests in autowatch mode
 */
const watch = gulp.series(eslintTest, function testWatch(done) {
  return new Server({
    configFile: config.paths.karma,
    singleRun: false
  }, function (exitCode) {
    exitCode ? process.exit(exitCode) : done();
  }).start();
});

exports.checkCompleteness = checkCompleteness;
exports.watch = watch;
exports.run = run;
