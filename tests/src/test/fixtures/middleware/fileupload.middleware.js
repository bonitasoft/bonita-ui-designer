var multiparty = require('multiparty');

const matchUpload = (req) => /API\/formFileUpload/.test(req.url);

function uploadMiddleware(req, res, next) {
  var form = new multiparty.Form();
  var filename;

  if (!matchUpload(req)) {
    next();
    return;
  }

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
    res.statusCode = 200;
    res.setHeader('content-type', 'text/html');
    res.write(JSON.stringify({
      filename: filename,
      tempPath: '1234.file'
    }));
    res.end();
  });

  form.parse(req);
}

module.exports = uploadMiddleware;
