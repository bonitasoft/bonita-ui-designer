const {src, parallel, dest} = require('gulp');
const concat = require('gulp-concat');
const gulpIf = require('gulp-if');
const config = require('../config');
const uglify = require('gulp-uglify-es').default;
const html2js = require('gulp-ng-html2js');
const plumber = require('gulp-plumber');
const order = require('gulp-order');
const sourcemaps = require('gulp-sourcemaps');
const ngAnnotate = require('gulp-ng-annotate');
const babel = require('gulp-babel');
const merge = require('merge-stream');
const strip = require('gulp-strip-comments');
const cleanCSS = require('gulp-clean-css');

/**
 * js task, concatenate and minimify vendor js files
 */
function notMinified(file) {
  return !/(src-min|\.min\.js)/.test(file.path);
}

function vendor() {
  return src(config.paths.vendor)
    .pipe(concat('vendor.min.js'))
    .pipe(strip())
    .pipe(gulpIf(notMinified, uglify().on('error', console.error)))
    .pipe(dest(config.paths.dest.vendors));
}

function runtimeCss(){
  return src(config.paths.css)
    .pipe(cleanCSS({level: {1: {specialComments: false}}}))
    .pipe(dest(config.paths.dest.css));
}

function runtimeFonts(){
  return src(config.paths.fonts)
    .pipe(dest(config.paths.dest.fonts));
}

function runtimeJs(done){
  let tpl = src(config.paths.templates)
    .pipe(html2js({
      moduleName: 'bonitasoft.ui.templates'
    }));

  let app = src(config.paths.runtime)
    .pipe(plumber())
    .pipe(order([
      '**/*.module.js',
      '**/*.js'
    ]))
    .pipe(sourcemaps.init())
    .pipe(babel())
    .pipe(ngAnnotate({
      'single_quotes': true,
      add: true
    }));

  merge(app, tpl)
    .pipe(concat('runtime.min.js'))
    .pipe(strip())
    .pipe(uglify().on('error', console.error))
    .pipe(sourcemaps.write('.'))
    .pipe(dest(config.paths.dest.js));
  done();
}

exports.vendor = vendor;
exports.runtimeCss = runtimeCss;
exports.runtimeJs = runtimeJs;
exports.copy = parallel(vendor,runtimeCss,runtimeFonts,runtimeJs);

