function pass(message) {
  return {
    pass: true,
    message: message
  };
}

function fail(message) {
  return {
    pass: false,
    message: message
  };
}

export default {
  elementMatchers: {

    toHaveClass: function() {
      return {
        compare: function(actual, expected) {
          var element = angular.element(actual);
          if (element.hasClass(expected)) {
            return pass('expected to have class [' + expected + ']');
          } else {
            return fail('expected to have class [' + expected + '] but got [' + actual[0].classList.toString() + ']');
          }
        }
      };
    }
  }
};
