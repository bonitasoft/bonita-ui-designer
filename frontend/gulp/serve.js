var browserSync = require('browser-sync');
var connect = require('connect');
var serveStatic = require('serve-static');
var http = require('http');
var multiparty = require('multiparty');

module.exports = function (gulp, config) {

  require('./build.js')(gulp, config);

  var paths = config.paths;

  var staticProxyfiedFiles = [
    /^\/runtime\/.*/,               // http://localhost:8080/runtime/...
    /^\/widgets\/.*/,                 // http://localhost:8080/widgets/...
    /^\/.*\/assets\/.*/,      // http://localhost:8080/.../assets/....
    /^\/preview\/.*/
  ];

  /**
   * Web Server & livereload
   */
  var proxy = require('http-proxy')
    .createProxyServer({}).on('error', function (err, req, res) {
      res.writeHead(500, {
        'Content-Type': 'text/plain'
      });
      res.end(JSON.stringify(err));
      console.error(err);
    });

  /* proxyMiddleware forwards static file requests to BrowserSync server
   and forwards dynamic requests to our real backend */
  function proxyMiddleware(req, res, next) {

    function matchStaticFile(req) {
      return /\.(html|css|js|png|jpg|jpeg|gif|ico|xml|rss|txt|eot|svg|ttf|woff|map)(\?((r|v|rel|rev)=[\-\.\w]*)?)?$/.test(req.url);
    }

    function matchStaticProxyfied(req) {
      return staticProxyfiedFiles.some(function (regexp) {
        return req.url.match(regexp);
      });
    }

    if (matchStaticFile(req) && !matchStaticProxyfied(req)) {
      next();
    } else if (/^\/bonita\/.*/.test(req.url)) {
      proxy.web(req, res, { target: 'http://127.0.0.1:8081' });
    } else {
      proxy.web(req, res, { target: 'http://127.0.0.1:8080' });
    }
  }


  function uploadMiddleware(req, res, next) {
    if (/\/API\/formFileUpload$/.test(req.url)) {
      var form = new multiparty.Form();
      var filename;

      form.on('error', function (error) {
        console.log('Error parsing form', error.stack);
        res.writeHead(500, {'content-type': 'text/plain'});
        res.end(JSON.stringify(error));
      });

      form.on('part', function (part) {
        if (part.filename) {
          filename = part.filename;
        }
        part.resume();
      });

      form.on('close', function () {
        res.writeHead(200, {'content-type': 'text/plain'});
        res.write(JSON.stringify({
          filename: filename,
          tempPath: '1234.file'
        }));
        res.end();
      });

      form.parse(req);
    } else {
      next();
    }
  }

  function browserSyncInit(baseDir, files, startPath, browser) {
    browser = browser || 'default';

    browserSync.instance = browserSync.init(files, {
      startPath: startPath || '/index.html',
      server: {
        baseDir: baseDir,
        middleware: [
          uploadMiddleware,
          proxyMiddleware,
          serveStatic(paths.dist)
        ]
      },
      browser: browser
    });
  }

  function serveE2e() {
    var app = connect();
    app.use(serveStatic(paths.test, {
      index: 'index.html'
    }));

    app.use(uploadMiddleware);
    app.use(serveStatic(paths.dist));
    app.use(function (req, res, next) {
      req.url = '/index.html';
      next();
    });

    var server = http.createServer(app);
    server.listen(12001);

    console.log('Server started http://localhost:12001');
    return server;
  }


  /**
   * This task is not working with the WebSocket connection, but SockJS falls back on long-polling
   * so the live reload in preview still work
   */
  gulp.task('serve:dev', ['bundle', 'assets', 'index:dev'], function () {
    browserSyncInit(paths.dev, [
      paths.dev + '/**/*.js',
      paths.dev + '/**/*.css'
    ], 'index.html');
  });

  gulp.task('serve:dist', function () {
    browserSyncInit('build/dist', [
      paths.dist + '/**/*.js',
      paths.dist + '/**/*.css'
    ], 'index.html');
  });


  gulp.task('serve:e2e', ['build', 'bundle:e2e', 'index:e2e'], function () {
    return serveE2e(paths);
  });

  return {
    serveE2e: serveE2e,
    browserSyncInit: browserSyncInit
  }
};
