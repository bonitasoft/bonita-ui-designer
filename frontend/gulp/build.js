const plumber = require('gulp-plumber');
const merge = require('merge-stream');
const replace = require('gulp-replace');
const order = require('gulp-order');
const gulpIf = require('gulp-if');
const ngAnnotate = require('gulp-ng-annotate-patched');
const uglify = require('gulp-uglify-es').default;
const less = require('gulp-less');
//const autoPrefixer = require('gulp-autoprefixer');
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
const ddescriber = require('./ddescriber.js');
const header = require('gulp-header');
const iconfont = require('gulp-iconfont');
const iconfontCss = require('gulp-iconfont-css');
const base64 = require('gulp-base64');
const jscs = require('gulp-jscs');
const babel = require('gulp-babel');


module.exports = function (gulp, config) {
  let paths = config.paths;
  let timestamp = config.timestamp;

  /**
   * Clean the directories created by tasks in this file
   */
  gulp.task('clean', function (done) {
    return del('build', done);
  });

  /**
   * Check for ddescribe and iit
   */
  gulp.task('ddescriber', function () {
    return gulp.src(paths.tests)
      .pipe(ddescriber());
  });

  gulp.task('assets:locales', function () {
    return gulp.src(paths.locales)
      .pipe(gulp.dest(paths.dist + '/locales'));
  });

  gulp.task('assets:font', function () {
    return gulp.src(paths.assets.fonts)
      .pipe(gulp.dest(paths.dist + '/fonts'));
  });

  gulp.task('assets:ace', function () {
    return gulp.src(paths.assets.ace)
      .pipe(gulp.dest(paths.dist + '/js'));
  });

  gulp.task('assets:images', function () {
    return gulp.src(paths.assets.images)
      .pipe(gulp.dest(paths.dist + '/img'));
  });

  gulp.task('assets:licences', function () {
    return gulp.src(paths.assets.licences)
      .pipe(gulp.dest(paths.dist));
  });

  gulp.task('assets:favicon', function () {
    return gulp.src(paths.assets.favicon)
      .pipe(gulp.dest(paths.dist));
  });

  /**
   * Compile css
   */
  gulp.task('bundle:icons', function () {
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
  });

  gulp.task('bundle:css', function () {
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
  });

  gulp.task('dist:css', gulp.series('bundle:icons', 'bundle:css', function dist_css() {
    return gulp.src(paths.dev + '/css/*.css')
      .pipe(base64())
      .pipe(concat('page-builder.css'))
      .pipe(csso())
      .pipe(rename('page-builder-' + timestamp + '.min.css'))
      .pipe(gulp.dest(paths.dist + '/css'));
  }));

  gulp.task('bundle:html', function () {
    let options = {
      loose: true //preserve one whitespace, otherwise that breaks the UI
    };

    return gulp.src(paths.templates)
      .pipe(minifyHTML(options))
      .pipe(gulp.dest(paths.dev + '/html'));
  });

  /**
   * bundle JS
   * concat generated templates and javascript files
   */
  gulp.task('bundle:js', gulp.series('bundle:html', function bundle_js() {
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
  }));

  gulp.task('jshint', function () {
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
  });

  gulp.task('jscs', function () {
    return gulp.src(paths.jsFolder + '/**/*.js')
      .pipe(jscs({fix: true}))
      .pipe(jscs.reporter())
      .pipe(jscs.reporter('fail'))
      .pipe(gulp.dest(paths.jsFolder));
  });

  gulp.task('jscs:test', function () {
    return gulp.src(paths.testFiles)
      .pipe(jscs({fix: true}))
      .pipe(jscs.reporter())
      .pipe(jscs.reporter('fail'))
      .pipe(gulp.dest(paths.testFolder));
  });

  gulp.task('dist:js', gulp.series('bundle:js', function dist_js() {
    return gulp.src(paths.dev + '/js/app.js')
      .pipe(rename('page-builder-' + timestamp + '.min.js'))
      .pipe(replace('\'%debugMode%\'', !utils.env.dist))
      .pipe(uglify({output: {'ascii_only': true}}))   // preserve ascii unicode characters such as \u226E
      .pipe(header(config.banner))
      .pipe(gulp.dest(paths.dist + '/js'));
  }));

  /**
   * Concatenate js vendor libs
   */
  gulp.task('bundle:vendors', function () {
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
  });

  gulp.task('dist:vendors', gulp.series('bundle:vendors', function dist_vendors() {

    return gulp.src(paths.dev + '/js/vendors.js')
      .pipe(rename('vendors-' + timestamp + '.min.js'))
      .pipe(gulp.dest(paths.dist + '/js'));
  }));

  /**
   * Index
   */
  gulp.task('index:dist', function () {
    return gulp.src('app/index.html')
      .pipe(htmlreplace({
        'js': 'js/page-builder-' + timestamp + '.min.js',
        'vendors': 'js/vendors-' + timestamp + '.min.js',
        'css': 'css/page-builder-' + timestamp + '.min.css'
      }))
      .pipe(gulp.dest(paths.dist));
  });

  /**
   * Assets
   */
  gulp.task('assets', gulp.series('assets:font', 'assets:ace', 'assets:images', 'assets:licences', 'assets:favicon', 'assets:locales'));

  /**
   * Translate application
   */

  gulp.task('pot', gulp.series('bundle:html', function pot() {
    let files = [paths.dev + '/html/**/*.html', paths.js].reduce(function (files, arr) {
      return files.concat(arr);
    }, []);
    return gulp.src(files)
      .pipe(gettext.extract('lang-template.pot', {}))
      .pipe(gulp.dest('build/po'));
  }));

  gulp.task('build', gulp.series('jshint', 'assets', 'pot', 'dist:css', 'dist:js', 'dist:vendors', 'index:dist'));

  gulp.task('bundle', gulp.series('bundle:vendors', 'bundle:js', 'bundle:css', 'bundle:icons'));

};
