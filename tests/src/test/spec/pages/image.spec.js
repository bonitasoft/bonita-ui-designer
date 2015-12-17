describe('image', function() {

  beforeEach(function() {
    browser.get('/designer/preview/page/image/');
  });

  it('should display a an image', function() {
    var images = $$('pb-image img');

    expect(images.get(0).getAttribute('src')).toBe("http://localhost:8086/designer/preview/page/image/assets/img/bonitaLogo.png");
    expect(images.get(0).getAttribute('alt')).toContain("Bonita Logo");
    expect(images.get(1).getAttribute('src')).toBe("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSc7PIQ0-qveL_4Q2Wwl8xkaT-OnRsAewpUOHutYtZlhVBvjwJ3");
    expect(images.get(1).getAttribute('alt')).toContain("a test");

  });


});
