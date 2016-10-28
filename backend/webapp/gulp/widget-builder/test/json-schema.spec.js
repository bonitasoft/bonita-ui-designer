import vfs from 'vinyl-fs';
import path from 'path';
import { jsonSchema } from '../src/index';
import {expect} from 'chai';

function toObject(file) {
  return JSON.parse(file.contents.toString());
}

describe('Json schema converter', () => {

  let widgetJson = path.join(__dirname, '/pbWidget/pbWidget.json');

  it('should create a schema with an "object" type and the widget id', (done) => {
    let stream = vfs.src(widgetJson)
      .pipe(jsonSchema());

    stream.on('data', (file) => {
      let schema = toObject(file);

      expect(schema.type).to.equal('object');
      expect(schema.id).to.equal('pbWidget');
      done();
    });
  });

  it('should populate object properties with default ones', (done) => {
    let stream = vfs.src(widgetJson)
      .pipe(jsonSchema());

    stream.on('data', (file) => {
      let schema = toObject(file);

      expect(schema.properties).to.have.property('cssClasses')
        .that.is.eql({type: 'string'});
      expect(schema.properties).to.have.property('hidden')
        .that.is.eql({type: 'boolean', default: false});
      done();
    });
  });

  it('should populate object properties depending on widget properties types', (done) => {
    let stream = vfs.src(widgetJson)
      .pipe(jsonSchema());

    stream.on('data', (file) => {
      let schema = toObject(file);

      expect(schema).to.have.deep.property('properties.disabled.type', 'boolean');
      expect(schema).to.have.deep.property('properties.label.type', 'string');
      expect(schema).to.have.deep.property('properties.text.type', 'string');
      expect(schema).to.have.deep.property('properties.minLength.type', 'number');
      done();
    });
  });

  it('should populate object properties default values whenever it exists', (done) => {
    let stream = vfs.src(widgetJson)
      .pipe(jsonSchema());

    stream.on('data', (file) => {
      let schema = toObject(file);

      expect(schema).not.to.have.deep.property('properties.label.default');
      expect(schema).to.have.deep.property('properties.disabled.default', false);
      expect(schema).to.have.deep.property('properties.text.default', 'Default value');
      done();
    });
  });

  it('should populate object properties with enum when widget property is a choice', (done) => {
    let stream = vfs.src(widgetJson)
      .pipe(jsonSchema());

    stream.on('data', (file) => {
      let schema = toObject(file);

      expect(schema).to.have.deep.property('properties.collectionPosition')
        .that.is.eql({enum: ['First', 'Item', 'Last'], default: 'Last'});
      done();
    });
  });

  it('should populate object properties with enum when widget property is a choice and choice values are objects', (done) => {
    let stream = vfs.src(widgetJson)
      .pipe(jsonSchema());

    stream.on('data', (file) => {
      let schema = toObject(file);

      expect(schema).to.have.deep.property('properties.chartType')
        .that.is.eql({enum: ['Doughnut', 'Pie', 'PolarArea', 'Bar', 'Line', 'Radar'], default: 'Doughnut'});
      done();
    });
  });

});
