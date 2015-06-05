var plumber = require('gulp-plumber');
var merge = require('merge-stream');
var replace = require('gulp-replace');
var order = require('gulp-order');
var gulpIf = require('gulp-if');
var ngAnnotate = require('gulp-ng-annotate');
var uglify = require('gulp-uglify');
var less = require('gulp-less');
var autoPrefixer = require('gulp-autoprefixer');
var csso = require('gulp-csso');
var jshint = require('gulp-jshint');
var html2js = require('gulp-ng-html2js');
var sourcemaps = require('gulp-sourcemaps');
var gettext = require('gulp-angular-gettext');
var concat = require('gulp-concat');
var htmlreplace = require('gulp-html-replace');
var rename = require('gulp-rename');
var utils = require('gulp-util');
var del = require('del');
var ddescriber = require('./ddescriber.js');
var gettextWidget = require('./gettext-widget.js');

module.exports = function(gulp, config) {
  var paths = config.paths;
  var timestamp = config.timestamp;

  gulp.task('build', ['jshint', 'assets', 'pot', 'dist:css', 'dist:js', 'dist:vendors', 'index:dist']);

  gulp.task('bundle', ['bundle:vendors', 'bundle:js', 'bundle:css']);

  /**
   * Clean the directories created by tasks in this file
   */
  gulp.task('clean', function(done){
    return del('build', done);
  });

  /**
   * Check for ddescribe and iit
   */
  gulp.task('ddescriber', function () {
    return gulp.src(paths.tests)
      .pipe(ddescriber());
  });

  /**
   * Assets
   */
  gulp.task('assets', ['assets:font', 'assets:ace', 'assets:images', 'assets:licences', 'assets:favicon']);

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
   * Translate application
   */
  gulp.task('pot', ['pot:widget', 'pot:front']);

  gulp.task('pot:front', function () {
    var files = [paths.templates, paths.js].reduce(function(files, arr) {
      return files.concat(arr);
    }, [] );
    return gulp.src(files)
      .pipe(gettext.extract('lang-template.pot', {}))
      .pipe(gulp.dest('build/po'));
  });

  gulp.task('pot:widgets', function () {
    var files = [
      '../../community/backend/webapp/src/main/resources/widgets/**/*.json',
      '../../subscription/backend/src/main/resources/widgets/**/*.json'
    ];
    return gulp.src(files)
      .pipe(plumber())
      .pipe( gettextWidget.prepare() )
      .pipe(concat('widgets.json', {newLine: ','}))
      .pipe( gettextWidget.extract() )
      .pipe(gulp.dest('build/po'));
  });



  /**
   * Compile css
   */
  gulp.task('bundle:css', function () {
    return gulp.src('app/less/main.less')
      .pipe(plumber())
      .pipe(less())
      .pipe(replace('bower_components/font-awesome/fonts', 'fonts'))
      .pipe(autoPrefixer({
        browsers: ['ie >= 9', '> 1%']
      }))
      .pipe(gulp.dest(paths.dev + '/css'));
  });

  gulp.task('dist:css', ['bundle:css'], function(){
    return gulp.src(paths.dev + '/css/main.css')
      .pipe(csso())
      .pipe(rename('page-builder-' + timestamp + '.min.css'))
      .pipe(gulp.dest(paths.dist + '/css'));
  });

  /**
   * bundle JS
   * concat generated templates and javascript files
   */
  gulp.task('bundle:js', function() {
    var tpl = gulp.src(paths.templates)
      .pipe(plumber())
      .pipe(html2js({
        moduleName: 'pb.templates',
        prefix: 'js/'
      }));

    var app = gulp.src(paths.js)
      .pipe(plumber())
      .pipe(ngAnnotate({
        'single_quotes': true,
        add: true
      }));

    return merge(app, tpl)
      .pipe(order([
        '**/*.module.js',
        '**/*.js'
      ]))
      .pipe(sourcemaps.init())
      .pipe(concat('app.js'))
      .pipe(sourcemaps.write('.'))
      .pipe(gulp.dest(paths.dev + '/js'));
  });

  gulp.task('jshint', function(){
    return gulp.src(paths.js)
      .pipe(jshint())
      .pipe(jshint.reporter('jshint-stylish'))
      .pipe(jshint.reporter('fail'));
  });

  gulp.task('dist:js', ['bundle:js'], function(){
    return gulp.src(paths.dev + '/js/app.js')
      .pipe(rename('page-builder-' + timestamp + '.min.js'))
      .pipe(replace('\'%debugMode%\'', !utils.env.dist))
      .pipe(uglify({output: { 'ascii_only': true }}))   // preserve ascii unicode characters such as \u226E
      .pipe(gulp.dest(paths.dist + '/js'));
  });

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

  gulp.task('dist:vendors', ['bundle:vendors'], function(){

    return gulp.src(paths.dev + '/js/vendors.js')
      .pipe(rename('vendors-' + timestamp + '.min.js'))
      .pipe(gulp.dest(paths.dist + '/js'));
  });

  /**
   * Index
   */
  gulp.task('index:dist', function () {
    return gulp.src('app/index-dev.html')
      .pipe(htmlreplace({
        'js': 'js/page-builder-' + timestamp + '.min.js',
        'vendors': 'js/vendors-' + timestamp + '.min.js',
        'css': 'css/page-builder-' + timestamp + '.min.css'
      }))
      .pipe(rename('index.html'))
      .pipe(gulp.dest(paths.dist));
  });
};
