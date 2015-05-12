var PageEditor = require('../pages/editor.page.js');

describe('resizing components test', function() {

  var editor;

  describe('For a component', function() {

    beforeEach(function() {
      editor = PageEditor.get('empty');

      // add a label with width 1
      editor.addWidget('pbParagraph');
      editor.setWidth(4);


      // add a label with width 2
      editor.addWidget('pbParagraph');
      editor.setWidth(5);

      // add a label with width 3
      editor.addWidget('pbParagraph');
      editor.setWidth(3);

      expect(editor.components.count()).toBe(3);
    });

    it('should not allow resize when element is not selected', function() {
      var firstComponent = editor.components.first();
      expect(firstComponent.element(by.css('.element-resizer')).isPresent()).toBe(false);
    });

    it('should allow resize when element is selected', function() {
      var firstComponent = editor.components.first();
      firstComponent.click();
      expect(firstComponent.element(by.css('.element-resizer')).isPresent()).toBe(true);
    });

    it('should resize the component on the right (size increasing) ', function() {
      //The first component is selected
      var firstComponent = editor.components.first();
      firstComponent.click();

      var initialWidth = 0;
      // element is necessary to force the promise resolution
      element(firstComponent.getSize().then(function(size) {
        initialWidth = size.width;
      }));

      var resizer = firstComponent.element(by.css('.element-resizer'));

      browser
        .actions()
        //I press the button when I am on the resizer
        .mouseDown(resizer)
        .mouseMove(editor.components.last())
        .mouseUp()
        .perform();

      firstComponent.click();

      element(firstComponent.getSize().then(function(size) {
        //The size has to be increased
        expect(size.width).toBeGreaterThan(initialWidth);
      }));
    });

    it('should resize the component on the left (size decreasing) ', function() {
      //The first component is selected
      var firstComponent = editor.components.first();
      firstComponent.click();

      var initialWidth = 0;
      // element is necessary to force the promise resolution
      element(firstComponent.getSize().then(function(size) {
        initialWidth = size.width;
      }));

      var resizer = firstComponent.element(by.css('.element-resizer'));

      browser
        .actions()
        //I press the button when I am on the resizer
        .mouseDown(resizer, 1, 1)
        .mouseMove(firstComponent.element(by.css('.dropZone--right')), 0, 10)
        .mouseUp()
        .perform();

      firstComponent.click();

      element(firstComponent.getSize().then(function(size) {
        //The size has to be increased
        expect(size.width).toBeLessThan(initialWidth);
      }));
    });
  });

  describe('for a container', function() {

    beforeEach(function() {
      editor = PageEditor.get('empty');

      editor.addContainer();
      editor.setWidth(4);

      editor.addContainer();
      editor.setWidth(5);

      editor.addContainer();
      editor.setWidth(3);

      // The editor itself is a container
      expect(editor.containersInEditor.count()).toBe(3);
    });

    it('should not allow resize when element is not selected', function() {
      var firstContainer = editor.containersInEditor.first();
      expect(firstContainer.element(by.css('.element-resizer')).isPresent()).toBe(false);
    });

    it('should allow resize when element is selected', function() {
      var firstContainer = editor.containersInEditor.first();
      firstContainer.click();
      expect(firstContainer.element(by.css('.element-resizer')).isPresent()).toBe(true);
    });

    it('should resize the component on the right (size increasing) ', function() {
      //The first component is selected
      var firstContainer = editor.containersInEditor.first();
      firstContainer.click();

      var initialWidth = 0;
      // element is necessary to force the promise resolution
      element(firstContainer.getSize().then(function(size) {
        initialWidth = size.width;
      }));

      var resizer = firstContainer.element(by.css('.element-resizer'));

      browser
        .actions()
        //I press the button when I am on the resizer
        .mouseDown(resizer)
        .mouseMove(editor.containersInEditor.last())
        .mouseUp()
        .perform();

      firstContainer.click();

      element(firstContainer.getSize().then(function(size) {
        //The size has to be increased
        expect(size.width).toBeGreaterThan(initialWidth);
      }));
    });

    it('should resize the component on the left (size decreasing) ', function() {
      //The first component is selected
      var firstContainer = editor.containersInEditor.first();
      firstContainer.click();

      var initialWidth = 0;
      // element is necessary to force the promise resolution
      element(firstContainer.getSize().then(function(size) {
        initialWidth = size.width;
      }));

      var resizer = firstContainer.element(by.css('.element-resizer'));

      browser
        .actions()
        //I press the button when I am on the resizer
        .mouseDown(resizer)
        .mouseMove(firstContainer)
        .mouseUp()
        .perform();

      firstContainer.click();

      element(firstContainer.getSize().then(function(size) {
        //The size has to be increased
        expect(size.width).toBeLessThan(initialWidth);
      }));
    });
  });

});
