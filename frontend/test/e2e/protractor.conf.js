/* globals exports, process */
// needed for ES6 to work in protractor <_<
require('babel-core/register');

const capabilities = {
  browserName: 'chrome',
  chromeOptions: {
    args: [
      '--window-size=1920,1080'
    ]
  }
};

//Specify binary path to enable windows build
if(process.platform.indexOf('win') === 0) {
  capabilities.chromeOptions.binary = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
}

// activate chrome in headless mode
// see https://developers.google.com/web/updates/2017/04/headless-chrome
if (process.env.HEADLESS) {
  capabilities.chromeOptions.args = [
    ...capabilities.chromeOptions.args,
    '--headless',
    // Temporarily needed if running on Windows.
    '--disable-gpu',
    // We must disable the Chrome sandbox when running Chrome inside Docker
    '--no-sandbox'
  ];
}

exports.config = {
  directConnect: true,
  chromeDriver: `../../node_modules/webdriver-manager/selenium/chromedriver_2.38${process.platform.indexOf('win') === 0 ? '.exe' : ''}`,
  specs: [
    './spec/*.spec.js'
  ],
  capabilities,
  baseUrl: 'http://localhost:12001',
  onPrepare: function () {
    var jasmineReporters = require('jasmine-reporters');
    jasmine.getEnv().addReporter(
      new jasmineReporters.JUnitXmlReporter({
        savePath: 'build/reports/e2e-tests/',
        filePrefix: 'e2e',
        consolidateAll: true
      }));
  }
};
