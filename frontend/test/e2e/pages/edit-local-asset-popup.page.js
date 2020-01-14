(() => {
  'use strict';

  class EditLocalAssetPopUp {
    get fileContent() {
      return element(by.css('.EditAssetPopUp .ace_content')).getText();
    }

    set fileContent(content) {
      element(by.css('.EditAssetPopUp .ace_text-input')).sendKeys(content);
    }

    save() {
      element(by.cssContainingText('.EditAssetPopUp .modal-footer button', 'Save')).click();
    }

    dismiss() {
      element(by.css('.modal-footer')).element(by.css('.EditAssetPopUp-dismissBtn')).click();
    }

    get dismissBtn() {
      return element(by.css('.modal-footer')).element(by.css('.EditAssetPopUp-dismissBtn'));
    }

    isOpen() {
      return element(by.css('.EditAssetPopUp')).isPresent();
    }
  }

  module.exports = EditLocalAssetPopUp;

})();
