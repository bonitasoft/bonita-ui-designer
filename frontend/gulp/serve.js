const browserSync = require('browser-sync');
const connect = require('connect');
const serveStatic = require('serve-static');
const http = require('http');
const multiparty = require('multiparty');
const gulp = require('gulp');
const assets = require('./assets.js');
const build = require('./build.js');
const index = require('./index.js');
const bundle = require('./bundle.js');
const config = require('./config.js');

let paths = config.paths;

let staticProxyfiedFiles = [
  /^\/runtime\/.*/,               // http://localhost:8080/runtime/...
  /^\/widgets\/.*/,                 // http://localhost:8080/widgets/...
  /^\/.*\/assets\/.*/,      // http://localhost:8080/.../assets/....
  /^\/preview\/.*/,
  /^\/apps\/.*\/theme\/.*/
];

/**
 * Web Server & livereload
 */
let proxy = require('http-proxy')
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

  if (req.url === '/' || matchStaticFile(req) && !matchStaticProxyfied(req)) {
    next();
  } else {
    proxy.web(req, res, {target: 'http://127.0.0.1:8080'});
  }
}


function uploadMiddleware(req, res, next) {
  if (/\/API\/formFileUpload$/.test(req.url)) {
    let form = new multiparty.Form();
    let filename;

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

function serverE2e() {
  let app = connect();
  app.use(serveStatic(paths.test, {
    index: 'index.html'
  }));

  app.use(uploadMiddleware);
  app.use(serveStatic(paths.dist));
  app.use(function (req, res, next) {
    req.url = '/index.html';
    next();
  });

  let server = http.createServer(app);
  server.listen(config.protractor.port);

  console.log('Server started http://localhost:' + config.protractor.port);
  return server;
}


/**
 * This task is not working with the WebSocket connection, but SockJS falls back on long-polling
 * so the live reload in preview still work
 */
const dev = gulp.series(build.bundle, assets.copy, index.dev, function _dev() {
  browserSyncInit(paths.dev, [
    paths.dev + '/**/*.js',
    paths.dev + '/**/*.css'
  ], 'index.html');
});

function dist() {
  browserSyncInit('build/dist', [
    paths.dist + '/**/*.js',
    paths.dist + '/**/*.css'
  ], 'index.html');
}


const e2e = gulp.series(build.buildAll, bundle.e2e, index.e2e, function _e2e() {
  return serverE2e(paths);
});

exports.serverE2e = serverE2e;
exports.dev = dev;
exports.dist = dist;
exports.e2e = e2e;
