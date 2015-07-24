/* jshint node:true */
var gulp = require('gulp');
var ddescriber = require("../frontend/gulp/ddescriber.js");
var protractor = require('gulp-protractor').protractor;
var connect = require('connect');
var multiparty = require('multiparty');
var http = require('http');

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

function matchUpload(req) {
  return /API\/formFileUpload/.test(req.url);
}

function uploadMiddleware(req, res, next){
  var form = new multiparty.Form();
  var filename;

  if( !matchUpload(req) ) {
    next();
    return;
  }

  form.on('error', function(error){
    console.log('Error parsing form', error.stack);
    res.writeHead(500, {'content-type': 'text/plain'});
    res.end(JSON.stringify(error));
  });

  form.on('part', function(part){
    if (part.filename) {
      filename = part.filename;
    }
    part.resume();
  });

  form.on('close', function() {
    res.statusCode = 200;
    res.setHeader( 'content-type', 'text/html' );
    res.write(JSON.stringify({
      filename: filename,
      tempPath: '1234.file'
    }));
    res.end();
  });

  form.parse(req);
}

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

    // use upload middleware to mock /API/formFileUpload
    app.use(uploadMiddleware);
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
