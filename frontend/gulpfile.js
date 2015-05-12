/* jshint node:true */
var http = require('http');
var gulp = require('gulp');
var rimraf = require('gulp-rimraf');
var ngAnnotate = require('gulp-ng-annotate');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var less = require('gulp-less');
var autoPrefixer = require('gulp-autoprefixer');
var utils = require('gulp-util');
var csso = require('gulp-csso');
var rename = require('gulp-rename');
var htmlreplace = require('gulp-html-replace');
var replace = require('gulp-replace');
var jshint = require('gulp-jshint');
var protractor = require('gulp-protractor').protractor;
var ngHtml2Js = require('gulp-ng-html2js');
var gettext = require('gulp-angular-gettext');
var mkdirp = require('mkdirp');
var connect = require('connect');
var runSequence = require('run-sequence');
var browserSync = require('browser-sync');
var serveStatic = require('serve-static');
var timestamp = Date.now();
var karma = require('karma').server;
var sourcemaps = require('gulp-sourcemaps');
var order = require('gulp-order');

var paths = {
  vendor: [
    'app/bower_components/jquery/dist/jquery.min.js',
    'app/bower_components/moment/min/moment.min.js',
    'app/bower_components/ace-builds/src-min-noconflict/ace.js',
    'app/bower_components/ace-builds/src-min-noconflict/ext-language_tools.js',
    'app/bower_components/angular/angular.min.js',
    'app/bower_components/angular-sanitize/angular-sanitize.min.js',
    'app/bower_components/angular-ui-router/release/angular-ui-router.min.js',
    'app/bower_components/angular-recursion/angular-recursion.min.js',
    'app/bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
    'app/bower_components/angular-ui-ace/ui-ace.min.js',
    'app/bower_components/ngUpload/ng-upload.min.js',
    'app/bower_components/bonita-js-components/dist/bonita-lib-tpl.min.js',
    'app/bower_components/angular-gettext/dist/angular-gettext.min.js',
    'app/bower_components/stomp-websocket/lib/stomp.min.js',
    'app/bower_components/sockjs/sockjs.min.js',
    'app/bower_components/keymaster/keymaster.js',
    'app/bower_components/angular-moment/angular-moment.min.js'
  ],
  js: [
    'app/js/**/*.js',
    'build/templates/**/*.tpl.js'
  ]
};

/**
 * Clean the directories created by tasks in this file
 */
gulp.task('clean', function () {
  return gulp.src(['./build'], {read: false})
    .pipe(rimraf());
});

/**
 * Run unit tests once and exit
 */
gulp.task('test', function (done) {
  return karma.start({
    configFile: __dirname + '/test/karma.conf.js',
    singleRun: true
  }, done);
});

/**
 * Run unit tests in autowatch mode
 */
gulp.task('test-watch', function (done) {
  return karma.start({
    configFile: __dirname + '/test/karma.conf.js',
    singleRun: false
  }, done);
});

/**
 * Convert partials into js
 */
gulp.task('html2js', function () {
  return gulp.src(['app/**/*.html', '!app/index-dev.html', '!app/bower_components/**/*.html'])
    .pipe(ngHtml2Js({
      moduleName: 'pb'
    }))
    .pipe(rename(function (file) {
      file.extname = '.tpl.js';
      return file;
    }))
    .pipe(gulp.dest('build/templates'));
});

gulp.task('ace', function () {
  gulp.src('app/bower_components/ace-builds/src-min-noconflict/{mode,worker}-{html,javascript,json}.js', {buffer: false})
    .pipe(gulp.dest('.tmp/js'))
    .pipe(gulp.dest('build/dist/js'));
});

/**
 * Concatenate js vendor libs
 */
gulp.task('vendor', function () {
  gulp.src(paths.vendor)
    .pipe(sourcemaps.init())
    .pipe(concat('vendor.min.js'))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest('.tmp/js'));
});

/**
 * Generates the final page-builder--<timestamp>.min.js file by concatenating already minified libraries
 * with uglified JS files. The JS files are jshinted by this process and an error causes the task to fail
 */
gulp.task('js', ['ace', 'html2js'], function () {
  gulp.src(paths.js)
    .pipe(sourcemaps.init())
    .pipe(order([
      '**/*.module.js',
      '**/*.js'
    ]))
    .pipe(jshint())
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(jshint.reporter('fail'))
    .pipe(ngAnnotate({
      'single_quotes': true,
      add: true
    }))
    .pipe(concat('page-builder-' + timestamp + '.min.js'))
    .pipe(replace('\'%debugMode%\'', !utils.env.dist))
    .pipe(uglify())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest('build/dist/js'));
});

gulp.task('css', ['font'], function () {
  return gulp.src('app/less/main.less')
    .pipe(less())
    .on('error', handleError)
    .pipe(autoPrefixer({
      browsers: ['ie >= 9', '> 1%']
    }))
    .pipe(gulp.dest('.tmp/css'));
});

gulp.task('font', function () {
  return gulp.src(['app/bower_components/font-awesome/fonts/*.*', 'app/bower_components/bootstrap/fonts/*.*'])
    .pipe(gulp.dest('.tmp/fonts'));
});

gulp.task('licences', function () {
  return gulp.src('licences/**/*.*')
    .pipe(gulp.dest('build/dist'));
});

gulp.task('images', function () {
  return gulp.src('app/img/*.*')
    .pipe(gulp.dest('build/dist/img'));
});

gulp.task('favicon', function () {
  return gulp.src('app/favicon.ico')
    .pipe(gulp.dest('build/dist'));
});

function handleError(err) {
  console.error(err.toString());
  this.emit('end');
}

/**
 * watch less files
 */
gulp.task('watch', ['css'], function () {
  gulp.watch('app/less/**/*.less', ['css']);
});

/**
 * Generates index.html (for production), index-e2e.html (used by e2e tests, containing the production JS and CSS files
 * and the mock JS files), and index-e2e-dev.html (containing the original JS files and the mock JS files, useful to
 * debug the e2e tests)
 */
gulp.task('html', ['css', 'vendor'], function () {
  var vendorFile = 'vendor-' + timestamp + '.min.js';

  gulp.src(['.tmp/fonts/*.*'])
    .pipe(gulp.dest('build/dist/fonts'));

  gulp.src('.tmp/js/vendor.min.js')
    .pipe(rename(vendorFile))
    .pipe(gulp.dest('build/dist/js'));

  gulp.src('.tmp/css/main.css')
    .pipe(replace('bower_components/font-awesome/fonts', 'fonts'))
    .pipe(csso())
    .pipe(rename('page-builder-' + timestamp + '.min.css'))
    .pipe(gulp.dest('build/dist/css'));

  gulp.src('app/index-dev.html')
    .pipe(htmlreplace({
      'vendor': 'js/' + vendorFile,
      'css': 'css/page-builder-' + timestamp + '.min.css',
      'js': 'js/page-builder-' + timestamp + '.min.js'
    }))
    .pipe(rename('index.html'))
    .pipe(gulp.dest('build/dist'));

  gulp.src('app/index-dev.html')
    .pipe(htmlreplace({
      'vendor': '/js/' + vendorFile,
      'css': '/css/page-builder-' + timestamp + '.min.css',
      'js': '/js/page-builder-' + timestamp + '.min.js',
      'e2e': [
        '/bower_components/angular-mocks/angular-mocks.js',
        '/config/e2eConfig.js',
        '/polyfill/dnd.js'
      ]
    }))
    .pipe(rename('index-e2e.html'))
    .pipe(gulp.dest('build/dist'));

  gulp.src('app/index-dev.html')
    .pipe(htmlreplace({
      'vendor': '/js/' + vendorFile,
      'e2e': [
        '/bower_components/angular-mocks/angular-mocks.js',
        '/config/e2eConfig.js',
        '/polyfill/dnd.js']
    }, true))
    .pipe(rename('index-e2e-dev.html'))
    .pipe(gulp.dest('build/dist'));
});

var proxy = require('http-proxy')
  .createProxyServer({
    target: {
      host: 'localhost',
      port: 8080
    }
  }).on('error', function (e) {
    console.error(e);
  });

/* proxyMiddleware forwards static file requests to BrowserSync server
 and forwards dynamic requests to our real backend */
function proxyMiddleware(req, res, next) {

  function matchStaticFile(req) {
    return /\.(html|css|js|png|jpg|jpeg|gif|ico|xml|rss|txt|eot|svg|ttf|woff|map)(\?((r|v|rel|rev)=[\-\.\w]*)?)?$/.test(req.url);
  }

  function matchGenerator(req) {
    return req.url.lastIndexOf('/generator', 0) === 0 || req.url.lastIndexOf('/widgets', 0) === 0;
  }

  if (matchStaticFile(req) && !matchGenerator(req)) {
    next();
  } else {
    proxy.web(req, res);
  }
}

function browserSyncInit(baseDir, files, startPath, browser) {
  browser = browser || 'default';

  browserSync.instance = browserSync.init(files, {
    startPath: startPath || '/index.html',
    server: {
      baseDir: baseDir,
      middleware: proxyMiddleware
    },
    browser: browser
  });

}

/**
 * This task is not working with the WebSocket connection, but SockJS falls back on long-polling
 * so the live reload in preview still work
 */
gulp.task('serve', ['ace', 'vendor', 'watch'], function () {
  browserSyncInit([
      'app',
      '.tmp'
    ], [
      '.tmp/css/*.css',
      'app/**/*.html',
      'app/js/**/*.js'
    ],
    '/index-dev.html');
});

gulp.task('serve:dist', function () {
  browserSyncInit('build/dist', [], 'index.html');
});

/**
 * Start a server serving all files needed by end to end tests
 */
function serveE2e() {
  var app = connect();
  app.use(serveStatic('build/dist', {
    index: 'index-e2e.html'
  }));
  app.use(serveStatic('app'));
  app.use(serveStatic('test/e2e'));
  app.use(function (req, res, next) {
    req.url = '/index-e2e.html';
    next();
  });
  app.use(serveStatic('build/dist'));
  var server = http.createServer(app);
  server.listen(12001);

  console.log('Server started http://localhost:12001');
  return server;
}

gulp.task('serve:e2e', ['build'], function () {
  return serveE2e();
});

gulp.task('e2e', ['build'], function (done) {
  var server = serveE2e();
  mkdirp('build/reports/e2e-tests', function (err) {
    if (err) {
      done(err);
    } else {
      gulp.src(['test/e2e/spec/**/*.js'])
        .pipe(protractor({
          configFile: 'test/e2e/protractor.conf.js',
          args: ['--baseUrl', 'http://localhost:12001']
        }))
        .on('error', function (e) {
          throw e;
        })
        .on('end', function () {
          server.close();
          done();
        });
    }
  });
});

/**
 * Translate application
 */
gulp.task('pot', function () {
  return gulp.src(['app/partials/**/*.html', 'app/js/**/*.html', 'app/js/**/*.js'])
    .pipe(gettext.extract('lang-template.pot', {}))
    .pipe(gulp.dest('build/po/'));
});

gulp.task('i18n', ['pot'], function () {
  return gulp.src(['po/**/*.po'])
    .pipe(gulp.dest('build/dist/i18n/'));
});

gulp.task('build', ['js', 'css', 'html', 'images', 'favicon', 'i18n', 'licences']);

gulp.task('default', function (callback) {
  runSequence('clean', 'test', 'build', callback);
});
