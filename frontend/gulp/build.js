const plumber = require('gulp-plumber');
const merge = require('merge-stream');
const replace = require('gulp-replace');
const order = require('gulp-order');
const gulpIf = require('gulp-if');
const ngAnnotate = require('gulp-ng-annotate-patched');
const uglify = require('gulp-uglify-es').default;
const less = require('gulp-less');
const csso = require('gulp-csso');
const html2js = require('gulp-ng-html2js');
const minifyHTML = require('gulp-minify-html');
const sourcemaps = require('gulp-sourcemaps');
const gettext = require('gulp-angular-gettext');
const concat = require('gulp-concat');
const htmlreplace = require('gulp-html-replace');
const rename = require('gulp-rename');
const utils = require('gulp-util');
const del = require('del');
const header = require('gulp-header');
const iconfont = require('gulp-iconfont');
const iconfontCss = require('gulp-iconfont-css');
const base64 = require('gulp-base64');
const babel = require('gulp-babel');
const eslint = require('gulp-eslint');

const gulp = require('gulp');
const assets = require('./assets.js');
const config = require('./config.js');

let paths = config.paths;
let timestamp = config.timestamp;

/**
 * Clean the directories created by tasks in this file
 */
function clean(done) {
  // clean standard widgets uid
  del(paths.assets.resourceWidgets + '/uid*/**', {force:true});
  return del('build', done);
}

/**
 * Compile css
 */
function bundleIcons() {
  return gulp.src(paths.assets.icons)
    .pipe(iconfontCss({
      fontName: 'bonita-ui-designer',
      path: paths.assets.fontIconTemplate,
      targetPath: '../css/icons.css',
      fontPath: '../font/'
    }))
    .pipe(iconfont({
      fontName: 'bonita-ui-designer',
      centerHorizontally: true,
      normalize: true,
      prependUnicode: true
    }))
    .pipe(gulp.dest(paths.dev + '/font'));
}

function bundleCss() {
  let lessPipe = gulp.src('app/less/main.less')
    .pipe(plumber())
    .pipe(less())
    .pipe(replace('../../node_modules/font-awesome/fonts', '../fonts'));

  let cssPipe = gulp.src(paths.css);

  return merge(lessPipe, cssPipe).pipe(concat('main.css'))
    .pipe(gulp.dest(paths.dev + '/css'));
}

const distCss = gulp.series(bundleIcons, bundleCss, function _distCss() {
  return gulp.src(paths.dev + '/css/*.css')
    .pipe(base64())
    .pipe(concat('page-builder.css'))
    .pipe(csso())
    .pipe(rename('page-builder-' + timestamp + '.min.css'))
    .pipe(gulp.dest(paths.dist + '/css'));
});

function bundleHtml() {
  let options = {
    loose: true //preserve one whitespace, otherwise that breaks the UI
  };

  return gulp.src(paths.templates)
    .pipe(minifyHTML(options))
    .pipe(gulp.dest(paths.dev + '/html'));
}

/**
 * bundle JS
 * concat generated templates and javascript files
 */
const bundleJs = gulp.series(bundleHtml, function _bundleJs() {
  let tpl = gulp.src(paths.dev + '/html/**/*.html')
  //if errorHandler set to true, on error, pipe will not break
    .pipe(plumber({errorHandler: config.devMode}))
    .pipe(html2js({
      moduleName: 'bonitasoft.designer.templates',
      prefix: 'js/'
    }));

  let js = gulp.src(paths.js)
  //if errorHandler set to true, on error, pipe will not break
    .pipe(plumber({errorHandler: config.devMode}))
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

  return merge(js, tpl)
    .pipe(concat('app.js'))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.dev + '/js'));
});

function checkEslint() {
  function notMinified(file) {
    return !/(src-min|\.min\.js)/.test(file.path);
  }

  return gulp.src(paths.js)
    .pipe(gulpIf(notMinified, eslint()))
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
}

function fixEsLint() {
  function notMinified(file) {
    return !/(src-min|\.min\.js)/.test(file.path);
  }

  return gulp.src(paths.js)
    .pipe(gulpIf(notMinified, eslint({fix:true})))
    .pipe(eslint.format())
    .pipe(gulp.dest(file => file.base))
    .pipe(eslint.failAfterError());
}

const distJs = gulp.series(bundleJs, function _distJs() {
  return gulp.src(paths.dev + '/js/app.js')
    .pipe(rename('page-builder-' + timestamp + '.min.js'))
    .pipe(replace('\'%debugMode%\'', !utils.env.dist))
    .pipe(uglify({output: {'ascii_only': true}}))   // preserve ascii unicode characters such as \u226E
    .pipe(header(config.banner))
    .pipe(gulp.dest(paths.dist + '/js'));
});

/**
 * Concatenate js vendor libs
 */
function bundleVendors() {
  function notMinified(file) {
    return !/(src-min|\.min\.js)/.test(file.path);
  }

  return gulp.src(paths.vendor)
    .pipe(plumber())
    .pipe(sourcemaps.init())
    .pipe(gulpIf(notMinified, uglify()))
    .pipe(concat('vendors.js'))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.dev + '/js'));
}

const distVendors = gulp.series(bundleVendors, function _distVendors() {
  return gulp.src(paths.dev + '/js/vendors.js')
    .pipe(rename('vendors-' + timestamp + '.min.js'))
    .pipe(gulp.dest(paths.dist + '/js'));
});

/**
 * Index
 */
function indexDist() {
  return gulp.src('app/index.html')
    .pipe(htmlreplace({
      'js': 'js/page-builder-' + timestamp + '.min.js',
      'vendors': 'js/vendors-' + timestamp + '.min.js',
      'css': 'css/page-builder-' + timestamp + '.min.css'
    }))
    .pipe(gulp.dest(paths.dist));
}


/**
 * Translate application
 */

const pot = gulp.series(bundleHtml, function _pot() {
  let files = [paths.dev + '/html/**/*.html', paths.js].reduce(function (files, arr) {
    return files.concat(arr);
  }, []);
  return gulp.src(files)
    .pipe(gettext.extract('lang-template.pot', {}))
    .pipe(gulp.dest('build/po'));
});


exports.clean = clean;
exports.buildAll =  gulp.series(checkEslint, assets.copy, pot, distCss, distJs, distVendors, indexDist);
exports.bundle = gulp.series(bundleVendors, bundleJs, bundleCss, bundleIcons);
exports.bundleJs = bundleJs;
exports.bundleCss = bundleCss;
exports.bundleIcons = bundleIcons;
exports.pot = pot;
exports.fixEsLint = fixEsLint;
exports.checkEslint = checkEslint;
