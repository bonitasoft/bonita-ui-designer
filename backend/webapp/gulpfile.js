require('./gulp/subTasks/linter');
require('./gulp/build');
require('./gulp/dev');
require('./gulp/test');
const {task,series} = require('gulp');

/**
 * Default task
 * Run by 'npm run build' called by maven build
 * You can found this task in gulp/build.js file
 */
task('default', series('ddescriber','build'));

/**
 * aliasing dev task
 * You can found this task in gulp/dev.js file
 */
task('serve',series('dev'));



