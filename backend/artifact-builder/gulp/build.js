const widgets = require('./subTasks/widgets');
const runtime = require('./subTasks/runtime');
const linter = require('./subTasks/linter');
const pot = require('./subTasks/pot');
const {parallel, src} = require('gulp');
const config = require('./config');
const through = require("through2");

/**
 * Check for ddescribe and iit
 */
function checkTestsCompleteness() {
  return src(config.paths.tests)
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

exports.checkTestsCompleteness = checkTestsCompleteness;
exports.buildAll = parallel(linter.lint, runtime.copy, widgets.extractJsonSchema, widgets.copy, pot.copy);
