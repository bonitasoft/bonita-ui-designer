var through = require('through');
var Handlebars = require('handlebars');
var fs = require('fs');
var jsesc = require('jsesc');

function getDirectiveTemplate() {
  return Handlebars.compile(String(fs.readFileSync('src/main/resources/templates/widgetDirectiveTemplate.hbs.js')));
}

module.exports = function buildDirective() {
  return through(function (file) {
    var context = JSON.parse(file.contents);
    context.escapedTemplate = jsesc(context.template);
    file.path = file.path.replace('.json', '.js');
    file.contents = new Buffer(getDirectiveTemplate()(context));
    this.queue(file);
  });
};
