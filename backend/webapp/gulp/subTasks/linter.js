const { src, dest } = require('gulp');
const config = require('../config');
const eslint = require('gulp-eslint');

/**
 * Eslint task
 */
function lint() {
  return src(config.paths.runtime)
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
}

function lintFix() {
  return src(config.paths.runtime)
    .pipe(eslint({fix:true}))
    .pipe(eslint.format())
    .pipe(dest(file => file.base))
    .pipe(eslint.failAfterError());
}

exports.lint = lint;
exports.lintFix = lintFix;
