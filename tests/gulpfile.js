/* jshint node:true */
const gulp = require('gulp');
const protractor = require('gulp-protractor').protractor;
const connect = require('connect');
const http = require('http');
const through = require("through2");
const fileuploadMiddleware = require('./src/test/fixtures/middleware/fileupload.middleware');
const apiUserMiddleware = require('./src/test/fixtures/middleware/api-user.middleware');

const proxy = require('http-proxy')
  .createProxyServer({
    target: {
      host: 'localhost',
      port: 8083
    }
  }).on('error', function (e) {
    console.error(e);
  });

const config = {
  paths: {
    specs: ['src/test/**/*.spec.js']
  },
  protractor: {
    port: 8086
  },
  tomcat: {
    port: 8083
  }
};

/* proxyMiddleware forwards all requests to tomcat server
 except fake upload request */
function creatproxyMiddleware(port) {
  const proxy = require('http-proxy')
    .createProxyServer({
      target: {
        host: 'localhost',
        port: port
      }
    }).on('error', function (e) {
      console.error(e);
    });

  return function(req, res, next) {
    proxy.web(req, res);
  }
}

/**
 * Check for ddescribe and iit
 */

function checkSingleTest() {
  return through.obj(function (file, enc, cb) {
    let contents = file.contents.toString();
    let err = null;

    if (/.*ddescribe|iit|fit|fdescribe/.test(contents)) {
      err = new Error('\033[31mddescribe or iit present in file ' + file.path + '\033[0m');
    }
    cb(err, file);
  });
}

function checkTestsCompleteness() {
    return gulp.src(config.paths.specs)
      .pipe(checkSingleTest());
}

function test() {
  let app = connect();

  app.use(fileuploadMiddleware);
  app.use(apiUserMiddleware);
  app.use(creatproxyMiddleware(config.tomcat.port));

  let server = http.createServer(app).listen(config.protractor.port);
  console.log('Server started http://localhost:%d', config.protractor.port);
  return gulp.src(config.paths.specs)
    .pipe(protractor({
      configFile: 'protractor.conf.js'
    }))
    .on('error', function (e) {
      console.log(e);
      throw e;
    })
    .on('end', function () {
      console.log('close upload server');
      server.close();
    });
}

exports.default = gulp.series(checkTestsCompleteness, test);
exports.checkTestsCompleteness = checkTestsCompleteness;
exports.test = test;
