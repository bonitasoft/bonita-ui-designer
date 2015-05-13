angular.module('pb.services').factory('widgetFactory', function(commonParams) {

  'use strict';

  function createContainerWidget () {
    return {
      container: true,
      custom: false,
      id: 'container',
      type: 'container',
      icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#424251" d="M50,6.7V3.8h-1v2.9H50z M50,11.4V8.6h-1v2.9H50z M50,16.2v-2.9h-1v2.9H50z M49,20h1v-1.9h-1v1V20zM44.1,20h2.9v-1h-2.9V20z M39.2,20h2.9v-1h-2.9V20z M34.3,20h2.9v-1h-2.9V20z M29.4,20h2.9v-1h-2.9V20z M24.5,20h2.9v-1h-2.9V20zM19.6,20h2.9v-1h-2.9V20z M14.7,20h2.9v-1h-2.9V20z M9.8,20h2.9v-1H9.8V20z M4.9,20h2.9v-1H4.9V20z M0,18.1V20h2.9v-1H1v-1H0zM0,13.3v2.9h1v-2.9H0z M0,8.6v2.9h1V8.6H0z M0,3.8v2.9h1V3.8H0z M2.9,0H0v1.9h1V1h2V0z M7.8,0H4.9v1h2.9V0z M12.7,0H9.8v1h2.9V0zM17.6,0h-2.9v1h2.9V0z M22.5,0h-2.9v1h2.9V0z M27.5,0h-2.9v1h2.9V0z M32.4,0h-2.9v1h2.9V0z M37.3,0h-2.9v1h2.9V0z M42.2,0h-2.9v1h2.9V0z M47.1,0h-2.9v1h2.9V0z M50,0h-1v1v1h1V0z"/></svg>',
      name: 'Container',
      order: -2,
      properties: commonParams.getDefinitions().concat([
        {
          name: 'repeatedCollection',
          label: 'Repeat contents',
          caption: 'Select an array over which contents will be repeated over. Use `$item` in components inside this container to use current iteration.',
          type: 'string'
        }
      ])
    };
  }

  function createTabsContainerWidget () {
    return {
      container: true,
      custom: false,
      id: 'tabsContainer',
      type: 'tabsContainer',
      icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#424251" d="M50,11.4V8.6h-1v2.9H50z M50,16.2v-2.9h-1v2.9H50z M49,20h1v-1.9h-1v1V20z M44.1,20h2.9v-1h-2.9V20zM39.2,20h2.9v-1h-2.9V20z M34.3,20h2.9v-1h-2.9V20z M29.4,20h2.9v-1h-2.9V20z M24.5,20h2.9v-1h-2.9V20z M19.6,20h2.9v-1h-2.9V20zM14.7,20h2.9v-1h-2.9V20z M9.8,20h2.9v-1H9.8V20z M4.9,20h2.9v-1H4.9V20z M0,18.1V20h2.9v-1H1v-1H0z M0,13.3v2.9h1v-2.9H0zM0,8.6v2.9h1V8.6H0z M0,1v5.7h1V1H0z M15.7,0H1v1h14.7V0z M16.7,1h-1v4.8h2v-1h-1V1z M22.5,4.8h-2.9v1h2.9V4.8z M27.5,4.8h-2.9v1h2.9V4.8z M32.4,4.8h-2.9v1h2.9V4.8z M37.3,4.8h-2.9v1h2.9V4.8z M42.2,4.8h-2.9v1h2.9V4.8z M47.1,4.8h-2.9v1h2.9V4.8z M50,4.8h-1v1v1h1V4.8z"/><path fill="#868695" d="M34.3,1h-1v3.8h1V1z M18.6,1h14.7V0H18.6V1z M18.6,4.8V1h-1v3.8H18.6z"/></svg>',
      order: -1,
      name: 'Tabs container',
      properties: commonParams.getDefinitions()
    };
  }

  function createFormContainerWidget () {
    return {
      container: true,
      custom: false,
      id: 'formContainer',
      type: 'formContainer',
      icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#424251" d="M5 19h3v1H5v-1zm5-18h3V0h-3v1zM5 1h3V0H5v1zm5 19h3v-1h-3v1zm5 0h3v-1h-3v1zm5 0h3v-1h-3v1zM15 1h3V0h-3v1zM1 9H0v2h1V9zM0 2h1V1h2V0H0v2zm23-2h-3v1h3V0zM1 4H0v3h1V4zm0 14H0v2h3v-1H1v-1zm0-5H0v3h1v-3zM27 0h-2v1h2V0zm22 19v1h1v-1.9h-1v.9zm-24 1h2v-1h-2v1zM44 1h3V0h-3v1zm5 15h1v-3h-1v3zm0-16v2h1V0h-1zm0 7h1V4h-1v3zm0 4h1V9h-1v2zm-5 9h3v-1h-3v1zM39 1h3V0h-3v1zM29 1h3V0h-3v1zm0 19h3v-1h-3v1zm5 0h3v-1h-3v1zm0-19h3V0h-3v1zm5 19h3v-1h-3v1zm-9-6H18v4h12v-4z"/><path fill="#A8A8B7" d="M45 2v4H18V2h27zM18 12h27V8H18v4zm4 5h4v-2h-4v2zM7 5h9V3H7v2zm-2 6h7V9H5v2zm8 0h3V9h-3v2z"/></svg>',
      name: 'Form container',
      order: 0,
      properties: commonParams.getDefinitions().concat([
        {
          name: 'action',
          label: 'Url',
          caption: 'The URI of a program that processes the form information',
          type: 'string'
        },
        {
          name: 'method',
          label: 'Method',
          caption: 'The HTTP method to submit the form GET/POST',
          type: 'choice',
          choiceValues: [
            'GET',
            'POST'
          ]
        }
      ])
    };
  }

  return {
    createTabsContainerWidget: createTabsContainerWidget,
    createContainerWidget: createContainerWidget,
    createFormContainerWidget: createFormContainerWidget
  };
});
