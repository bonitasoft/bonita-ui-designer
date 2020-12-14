const gulp = require('gulp');
const serve = require('./serve.js');
const index = require('./index.js');
const config = require('./config.js');
const build = require('./build.js');

let paths = config.paths;

function watch(done) {
  gulp.watch(paths.js, gulp.series(build.bundleJs));
  gulp.watch(paths.templates, gulp.series(build.bundleJs));
  gulp.watch(['app/index.html'], gulp.series(index.dev));
  gulp.watch(paths.less, gulp.series(build.bundleCss));
  gulp.watch(paths.assets.icons, gulp.series(build.bundleIcons));
  done();
}

function setting(done) {
  config.devMode = true;
  done();
}

exports.serve = gulp.series(build.clean, watch, setting, serve.dev);

