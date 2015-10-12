var path = require('path');

describe('upload', function() {

  var filePath =  path.resolve(__dirname, '../../fixtures/greeeen.jpg');

  beforeEach(function() {
    browser.get('/designer/preview/page/upload/');
  });

  it('should upload a file', function() {
    var fileInput = $('.file-upload-input');
    var input = $('input[type="text"]');
    var button = $('.input-group-btn .btn');
    var clear = $('.file-upload-clear');
    var p = $('pb-text');

    fileInput.sendKeys(filePath);

    // Switch back to Default Content due to iframe posting
    // this will avoid "Angular is not defined" error
    browser.switchTo().defaultContent();

    browser.wait(function() {
      return  input.getAttribute('value').then(function(text){
        return /greeeen\.jpg/.test(text)
      });
    }, 1000);

    expect(input.getAttribute('value')).toBe('greeeen.jpg');
    expect(p.getText()).toBe('{ "filename": "greeeen.jpg", "tempPath": "1234.file" }');
    clear.click();
    expect(input.getAttribute('value')).toBe('');
    expect(p.getText()).toBe('{}');
  })

});
