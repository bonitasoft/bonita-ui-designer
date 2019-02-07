describe('repeat container', function() {

  beforeEach(function() {
    browser.get('/bonita/preview/page/no-app-selected/repeatInputWithValidation/');
  });

  it('should display 3 containers with input fields and buttons', function() {

    var inputs = $$('input').filter(function(elem) {
      return elem.isDisplayed();
    });
    expect(inputs.count()).toBe(3);
    expect(inputs.get(0).getAttribute('value')).toBe('aaaa.aaaabonitasoft.com');
    expect(inputs.get(1).getAttribute('value')).toBe('bbbb.bbbbbonitasoft.com');

    var forms = $$('form').filter(function(elem) {
      return elem.isDisplayed();
    });

    expect(forms.count()).toBe(1);
  });

  it('should display a message if the state is invalid', function() {

    enableValidation();

    var emailValidation = element.all(by.css('.text-danger'));
    expect(emailValidation.get(0).getText()).toBe('This is not a valid email');
    expect(emailValidation.count()).toBe(2);
  });

  it('should be able to delete elements from the list respecting the order', function() {

    enableValidation();

    var buttons = $$('button').filter(function(elem) {
      return elem.isDisplayed();
    });
    expect(buttons.count()).toBe(3);

    buttons.get(0).click();

    var inputs = $$('input').filter(function(elem) {
      return elem.isDisplayed();
    });
    expect(inputs.count()).toBe(2);
    expect(inputs.get(0).getAttribute('value')).toBe('bbbb.bbbbbonitasoft.coma');
  });

  function enableValidation() {
     var inputs = $$('input').filter(function(elem) {
      return elem.isDisplayed();
    });

    inputs.each(function(input) {
      input.sendKeys('a');
    });
  }

});
