const { src, dest, task } = require('gulp');
const {config} = require('../config');
const eslint = require('gulp-eslint');

/**
 * Eslint task
 */
task('lint', () => {
  return src(config.paths.runtime)
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
});

task('lintFix', () => {
  return src(config.paths.runtime)
    .pipe(eslint({fix:true}))
    .pipe(eslint.format())
    .pipe(dest(file => file.base))
    .pipe(eslint.failAfterError());
});
