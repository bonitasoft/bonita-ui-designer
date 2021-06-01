const build = require('./gulp/build.js');
const dev = require('./gulp/dev.js');
const pot = require('./gulp/subTasks/pot');
const test = require('./gulp/test.js');
const {series} = require('gulp');

/**
 * Default task
 * Run by 'npm run build' called by maven build
 * You can found this task in gulp/build.js file
 */
exports.default = series(build.checkTestsCompleteness, build.buildAll);

exports.serve = dev.runServer;

exports.build = build.buildAll;

exports.pot = pot.copy;

exports.test = test.run;

exports.test_datepicker = test.datepicker;


