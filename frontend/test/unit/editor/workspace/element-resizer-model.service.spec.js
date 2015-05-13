describe('factory elementResizerModel', function() {

  var factory;

  beforeEach(module('pb.factories'));
  beforeEach(inject(function ($injector) {
    factory = $injector.get('elementResizerModel');
  }));

  it('should set the key isResisableComponent to false', function() {
    expect(factory.isResisableComponent).toBe(false);
  });

  it('should change the value of isResisableComponent via a method', function() {
    expect(factory.isResisableComponent).toBe(false);
    factory.isResizable(true);
    expect(factory.isResisableComponent).toBe(true);
    factory.isResizable(false);
    expect(factory.isResisableComponent).toBe(false);
  });

  it('should toggleVisibility on an element', function() {
    factory.left = document.createElement('DIV');
    factory.left.style.visibility = 'visible';

    factory.right = document.createElement('DIV');
    factory.right.style.visibility = 'hidden';

    factory.toggleVisibility('right');
    expect(factory.left.style.visibility).toBe('hidden');
    expect(factory.right.style.visibility).toBe('visible');
    factory.toggleVisibility('right');
    expect(factory.left.style.visibility).toBe('hidden');
    expect(factory.right.style.visibility).toBe('visible');

    factory.toggleVisibility('left');
    expect(factory.left.style.visibility).toBe('visible');
    expect(factory.right.style.visibility).toBe('hidden');
    factory.toggleVisibility('right');
    expect(factory.left.style.visibility).toBe('hidden');
    expect(factory.right.style.visibility).toBe('visible');

  });

  it('should compute the size of cols', function() {
    factory.bootstrapWidth = 10;
    factory.computeCols(2);
    expect(factory.bootstrapNewWidth).toBe(8);
  });


  it('should set a size in px to an element', function() {

    factory.right = document.createElement('DIV');
    factory.resize('right', 2);
    expect(factory.right.style.width).toBe('2px');
    factory.resize('right', 258);
    expect(factory.right.style.width).toBe('258px');
  });


});
