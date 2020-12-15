const plumber = require('gulp-plumber');
const merge = require('merge-stream');
const replace = require('gulp-replace');
const order = require('gulp-order');
const gulpIf = require('gulp-if');
const ngAnnotate = require('gulp-ng-annotate-patched');
const uglify = require('gulp-uglify-es').default;
const less = require('gulp-less');
const csso = require('gulp-csso');
const jshint = require('gulp-jshint');
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
const jscs = require('gulp-jscs');
const babel = require('gulp-babel');
const gulp = require('gulp');
const assets = require('./assets.js');
const config = require('./config.js');

let paths = config.paths;
let timestamp = config.timestamp;

/**
 * Clean the directories created by tasks in this file
 */
function clean(done) {
  return del('build', done);
}

/**
 * Compile css
 */
function bundle_icons() {
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

function bundle_css() {
  let lessPipe = gulp.src('app/less/main.less')
    .pipe(plumber())
    .pipe(less())
    .pipe(replace('../../bower_components/font-awesome/fonts', '../fonts'));
  // TODO see package.json browserslist
  /*.pipe(autoPrefixer({
      browsers: ['ie >= 9', '> 1%']
    }));*/
  let cssPipe = gulp.src(paths.css);
  return merge(lessPipe, cssPipe).pipe(concat('main.css'))
    .pipe(gulp.dest(paths.dev + '/css'));
}

const dist_css = gulp.series(bundle_icons, bundle_css, function _dist_css() {
    return gulp.src(paths.dev + '/css/*.css')
      .pipe(base64())
      .pipe(concat('page-builder.css'))
      .pipe(csso())
      .pipe(rename('page-builder-' + timestamp + '.min.css'))
      .pipe(gulp.dest(paths.dist + '/css'));
  });

function bundle_html() {
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
const bundle_js = gulp.series(bundle_html, function _bundle_js() {
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

function checkJshint() {
  function notMinified(file) {
    return !/(src-min|\.min\.js)/.test(file.path);
  }

  return gulp.src(paths.js)
    .pipe(gulpIf(notMinified, jshint()))
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(jshint.reporter('fail'))
    .pipe(gulpIf(notMinified, jscs()))
    .pipe(jscs.reporter())
    .pipe(jscs.reporter('fail'));
}

function checkJscs() {
  return gulp.src(paths.jsFolder + '/**/*.js')
    .pipe(jscs({fix: true}))
    .pipe(jscs.reporter())
    .pipe(jscs.reporter('fail'))
    .pipe(gulp.dest(paths.jsFolder));
}

function jscs_test() {
  return gulp.src(paths.testFiles)
    .pipe(jscs({fix: true}))
    .pipe(jscs.reporter())
    .pipe(jscs.reporter('fail'))
    .pipe(gulp.dest(paths.testFolder));
}

const dist_js = gulp.series(bundle_js, function _dist_js() {
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
function bundle_vendors() {
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

const dist_vendors = gulp.series(bundle_vendors, function _dist_vendors() {
    return gulp.src(paths.dev + '/js/vendors.js')
      .pipe(rename('vendors-' + timestamp + '.min.js'))
      .pipe(gulp.dest(paths.dist + '/js'));
  });

/**
 * Index
 */
function index_dist() {
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

const pot = gulp.series(bundle_html, function _pot() {
  let files = [paths.dev + '/html/**/*.html', paths.js].reduce(function (files, arr) {
    return files.concat(arr);
  }, []);
  return gulp.src(files)
    .pipe(gettext.extract('lang-template.pot', {}))
    .pipe(gulp.dest('build/po'));
});


exports.clean = clean;
exports.buildAll =  gulp.series(checkJshint, assets.copy, pot, dist_css, dist_js, dist_vendors, index_dist);
exports.bundle = gulp.series(bundle_vendors, bundle_js, bundle_css, bundle_icons);
exports.bundle_js = bundle_js;
exports.bundle_css = bundle_css;
exports.bundle_icons = bundle_icons;
exports.pot = pot;
