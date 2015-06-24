/* jshint node:true */
var gulp = require('gulp');
var ddescriber = require("../../community/frontend/gulp/ddescriber.js");
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

/**
 * Check for ddescribe and iit
 */
gulp.task('ddescriber', function () {
  return gulp.src('src/test/**/*.spec.js')
    .pipe(ddescriber());
});

gulp.task('test', function() {
  var app = connect();

  // use upload middleware to mock /API/formFileUpload

  app.use(uploadMiddleware);
  app.use(proxyMiddleware);

  var server = http.createServer(app).listen(8086);
  console.log('Server started http://localhost:8086');
  return gulp.src(['src/test/**/*.spec.js'])
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

gulp.task('default', ['ddescriber'], function() {
  gulp.start(['test']);
});

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
function proxyMiddleware(req, res, next) {
  proxy.web(req, res);
}
