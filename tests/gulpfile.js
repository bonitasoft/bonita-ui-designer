/* jshint node:true */
var gulp = require('gulp');
var ddescriber = require("../frontend/gulp/ddescriber.js");
var protractor = require('gulp-protractor').protractor;
var connect = require('connect');
var http = require('http');

var fileuploadMiddleware = require('./src/test/fixtures/middleware/fileupload.middleware');
var apiUserMiddleware = require('./src/test/fixtures/middleware/api-user.middleware');

var proxy = require('http-proxy')
  .createProxyServer({
    target: {
      host: 'localhost',
      port: 8083
    }
  }).on('error', function (e) {
    console.error(e);
  });

var config = {
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
  var proxy = require('http-proxy')
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
var registerDdescriberTask = function(gulp, config) {
  gulp.task('ddescriber', function () {
    return gulp.src(config.paths.specs)
      .pipe(ddescriber());
  });
};

var registerTestTask = function(gulp, config) {
  gulp.task('test', function() {
    var app = connect();

    app.use(fileuploadMiddleware);
    app.use(apiUserMiddleware);
    app.use(creatproxyMiddleware(config.tomcat.port));

    var server = http.createServer(app).listen(config.protractor.port);
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
  });
};

var registerTasks = function(gulp, config) {

  registerDdescriberTask(gulp, config);
  registerTestTask(gulp, config);

  gulp.task('default', ['ddescriber'], function() {
    gulp.start(['test']);
  });

};

registerTasks(gulp, config);

module.exports = {
  config: config,
  registerTasks: registerTasks
};
