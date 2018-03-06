const users = require('./users');
const url = require('url');
const queryString = require('query-string');

const match = (req) => /API\/identity\/user/.test(req.url);

const parseParams = (req) => queryString.parse(url.parse(req.url).search);

const middleware = (req, res, next) => {

  if (!match(req)) {
    next();
    return;
  }

  const params = parseParams(req);
  if (params.s) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(users.filter(params.s)));
  } else {
    next();
  }

};

module.exports = middleware;
