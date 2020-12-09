const htmlreplace = require('gulp-html-replace');

module.exports = function (gulp, config) {

  let timestamp = config.timestamp;

  /**
   * Index file
   */
  gulp.task('index:dev', function () {
    return gulp.src('app/index.html')
      .pipe(gulp.dest(config.config.paths.dev));
  });

  gulp.task('index:e2e', function () {
    return gulp.src('app/index.html')
      .pipe(htmlreplace({
        'js': 'js/page-builder-' + timestamp + '.min.js',
        'vendors': 'js/vendors-' + timestamp + '.min.js',
        'css': 'css/page-builder-' + timestamp + '.min.css',
        'e2e': 'js/e2e.js'
      }))
      .pipe(gulp.dest(config.paths.test));
  });

};
