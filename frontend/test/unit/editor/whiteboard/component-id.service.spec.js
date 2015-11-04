describe('componentFactory', function() {

  var componentId;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_componentId_) {
    componentId = _componentId_;
  }));

  it('should return an id for a given type', function() {
    expect(componentId.getNextId('aType')).toEqual('aType-0');
    expect(componentId.getNextId('anotherType')).toEqual('anotherType-0');
  });

  it('should return the incremented id for a given type', function() {
    expect(componentId.getNextId('component')).toEqual('component-0');
    expect(componentId.getNextId('component')).toEqual('component-1');
  });

});
