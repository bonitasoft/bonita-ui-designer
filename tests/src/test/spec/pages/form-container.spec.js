describe('formContainer', function() {

  /**
   * The test sets up a label, with inputs bound to its properties.
   * We can play with its visibility, its alignment, its text...
   */
  beforeEach(function() {
    browser.get('/designer/preview/page/formContainer/');
  });

  it('should display the $form name', function() {
    var form = $('form');
    var paragraph = form.element(by.css('pb-text'));
    expect(form.getAttribute('name')).toBe('$form');
    expect(paragraph.getText()).toContain('$error');
    expect(paragraph.getText()).toContain('$name');
    expect(paragraph.getText()).toContain('$valid');
    expect(paragraph.getText()).toContain('$invalid');
  });
});
