const {widgets} = require('./subTasks/widgets');
const {runtime} = require('./subTasks/runtime');
const {pot} = require('./subTasks/pot');
const {task, parallel, src} = require('gulp');
const ddescriber = require('../../../frontend/gulp/ddescriber.js');
const {config} = require('./config');

/**
 * Build backend task
 */
task('build',parallel(runtime, 'jsonSchema', widgets, pot));


/**
 * Check for ddescribe and iit
 */
task('ddescriber', function () {
  return src(config.paths.tests)
    .pipe(ddescriber());
});
