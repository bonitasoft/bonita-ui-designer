const gulp = require('gulp');
const build = require('./gulp/build.js');
const test = require('./gulp/test.js');
const e2e = require('./gulp/e2e.js');
const dev = require('./gulp/dev.js');
const serve = require('./gulp/serve.js');

exports.default = gulp.series(build.clean, test.checkCompleteness, build.buildAll);
exports.test = test.run;
exports.testWatch = test.watch;
exports.e2e = e2e.run;
exports.pot = build.pot;
exports.serve = dev.serve;
exports.serveDist = serve.dist;
exports.serveE2e = serve.e2e;
exports.checkEslint = build.checkEslint;
exports.fixEsLint = build.fixEsLint;
exports.clean = build.clean;

