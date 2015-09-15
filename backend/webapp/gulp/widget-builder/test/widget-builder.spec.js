import fs from 'fs';
import buildWidget from '../src/index.js';
import vfs from 'vinyl-fs';
import jsesc from 'jsesc';
import {Writable} from 'stream';
import {should} from 'chai';
should();

let resources = {
  controller: __dirname + '/pbWidget/pbWidget.ctrl.js',
  template: __dirname + '/pbWidget/pbWidget.tpl.html',
  json: __dirname + '/pbWidget/pbWidget.json',
  directive: __dirname + '/pbWidget/pbWidget.js'
};

let controller = jsesc(fs.readFileSync(resources.controller));
let template = jsesc(fs.readFileSync(resources.template));
let directive = `angular.module('bonitasoft.ui.widgets')
  .directive('pbWidget', function() {
    return {
      controllerAs: 'ctrl',
      controller: ${controller},
      template: '${jsesc(template)}'
    };
  });
`;

describe('buildWidget', () => {

  it('should build the widget directive', (done) => {

    vfs.src(resources.json)
      .pipe(buildWidget())
      .pipe(assertThat({
        when: (file) => file.path === resources.directive,
        then: (file) => String(file.contents).should.equal(directive),
        done
      }));
  });

  it('should inject widget controller in the json descriptor', (done) => {

    vfs.src(resources.json)
      .pipe(buildWidget())
      .pipe(assertThat({
        when: (file) => file.path === resources.json,
        then: (file) => JSON.parse(file.contents).controller.should.equal(controller),
        done
      }));
  });

  it('should inject widget template in the json descriptor', (done) => {

    vfs.src(resources.json)
      .pipe(buildWidget())
      .pipe(assertThat({
        when: (file) => file.path === resources.json,
        then: (file) => JSON.parse(file.contents).template.should.equal(template),
        done
      }));
  });

  it('should copy widget assets', (done) => {

    vfs.src(resources.json)
      .pipe(buildWidget())
      .pipe(assertThat({
        when: (file) => file.path.indexOf('assets') > 0,
        then: (file) => true,
        done
      }));
  });

  it('should keep base file when listing assets', (done) => {

    vfs.src(__dirname + '/pbWidget/*.json')
      .pipe(buildWidget())
      .pipe(assertThat({
        when: (file) => file.path.indexOf('assets') > 0,
        then: (file) => file.base.should.equal(__dirname + '/pbWidget/'),
        done
      }));
  });
});

function assertThat(assertion) {
  let stream = new Writable({objectMode: true});
  stream._write = (file, encoding, callback) => {
    if (assertion.when(file)) {
      assertion.then(file);
      assertion.done();
    }
    return callback();
  };
  return stream;
}
