// Used only for middleware manual testing.

// run `node server.js` or `nodemon server.js` to have live reload
// then you can access to you test middleware on http://localhost:3000
// ex: http://localhost:3000/API/identity/user?s=walt

const connect = require('connect');
const http = require('http');
const userApi = require('./api-user.middleware');

const app = connect();

app.use(userApi);

http.createServer(app).listen(3000);

