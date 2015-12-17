describe('input', function() {

  /**
   * The test sets up a input, with inputs bound to its properties.
   * We can play with its visibility, its value, its label display, etc...
   */
  beforeEach(function() {
    browser.get('/designer/preview/page/input/');
  });

  it('should display an input if isDisplayed is valued', function() {
    expect($$('pb-input').count()).toBe(10);

    $$('input').get(0).sendKeys('1');

    expect($$('pb-input').count()).toBe(11);

    // when we remove the value from the input, the input should disappear
    $$('input').get(1).clear();

    expect($$('pb-input').count()).toBe(10);
  });

  it('should display a readonly input if readOnly is valued', function() {
    $$('input').first().sendKeys('1');
    var pbInput = $$('input').first();
    expect(pbInput.getAttribute('readonly')).toBeNull();

    // when we value the read only input, the input should be readonly
    var isReadonlyInput = $$('input').get(2);
    isReadonlyInput.sendKeys('1');

    expect(pbInput.getAttribute('readonly')).toBe('true');

    // when we remove the value from the input, the input should not be readonly
    isReadonlyInput.clear();

    expect(pbInput.getAttribute('readonly')).toBeNull();
  });

  it('should display a label next to the input', function() {

    $$('input').first().sendKeys('1');
    var pbInputLabels = $$('pb-input').first().all(by.tagName('label'));
    expect(pbInputLabels.count()).toBe(1);

    // when we value the label input, the label should be shown
    var isLabelDisplayedInput = $$('input').get(3);
    isLabelDisplayedInput.sendKeys('1');
    expect(pbInputLabels.count()).toBe(0);

    // when we remove the value from the input, the label should disappear
    isLabelDisplayedInput.clear();

    expect(pbInputLabels.count()).toBe(1);
  });

  it('should display a label next to the input with a custom text', function() {
    $$('input').first().sendKeys('1');
    var pbInputLabels = $$('pb-input').first().all(by.tagName('label'));
    expect(pbInputLabels.count()).toBe(1);

    // when we value the label value input, the label text should change
    var labelAlignmentInput = $$('input').get(4);
    labelAlignmentInput.sendKeys('hello');

    var label = $$('pb-input').first().element(by.tagName('label'));
    expect(label.getText()).toBe('hello');
  });


  it('should display a label next to the input with a custom alignment', function() {
    $$('input').first().sendKeys('1');
    var pbInputLabels = $$('pb-input').first().all(by.tagName('label'));
    expect(pbInputLabels.count()).toBe(1);

    // when we value the label alignment input, the label alignment should change
    var labelAlignmentInput = $$('input').get(5);
    labelAlignmentInput.sendKeys('left');

    var label = $$('pb-input').first().element(by.tagName('label'));
    expect(label.getAttribute('class')).toContain('col-xs-4');

    labelAlignmentInput.clear().sendKeys('top');
    expect(label.getAttribute('class')).toContain('col-xs-12');
  });

  it('should change the value if the model is updated', function() {
    $$('input').first().sendKeys('1');
    var pbInput = $$('input').first();
    expect(pbInput.getAttribute('value')).toBe('');

    // when we value the model input, the input value should be updated
    var modelInput = $$('input').get(6);
    modelInput.sendKeys('hello');

    expect(pbInput.getAttribute('value')).toBe('hello');

    // when we remove the value from the input, the value should disappear
    modelInput.clear();

    expect(pbInput.getAttribute('value')).toBe('');
  });

  it('should change the placeholder', function() {
    $$('input').first().sendKeys('1');
    var pbInput = $$('input').first();
    expect(pbInput.getAttribute('placeholder')).toBe('');

    // when we value the placeholder input, the input placeholder should be updated
    var placeholderInput = $$('input').get(7);
    placeholderInput.sendKeys('hello');

    expect(pbInput.getAttribute('placeholder')).toBe('hello');

    // when we remove the value from the input, the placeholder should disappear
    placeholderInput.clear();

    expect(pbInput.getAttribute('placeholder')).toBe('');
  });

  it('should change the type', function() {
    var pbInput = $$('input').last();
    pbInput.sendKeys('password');
    expect(pbInput.getAttribute('type')).toBe('password');
  });

});
