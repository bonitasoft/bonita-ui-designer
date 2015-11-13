export default class Home {

  static get() {
    browser.get('#/');
    return new Home();
  }

  getListedPageNames() {
    return $$('home-pages .Artifact-info').map((element) => element.getText());
  }

  getListedWidgetNames() {
    return $$('home-widgets .Artifact-info').map((element) => element.getText());
  }

  search(term) {
    $('input.Home-SearchBox').clear().sendKeys(term);
  }
}
