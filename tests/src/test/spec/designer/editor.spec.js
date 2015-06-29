describe('UI designer: editor', function () {

  beforeEach(function () {
    browser.get('/designer/#/en/pages/empty');
  });

  it('should open the empty page', function(){
    var elements = element.all(by.tagName('component'));
    expect(elements.count()).toBe(0);
  });

});
