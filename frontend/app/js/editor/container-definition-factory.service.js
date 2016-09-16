/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.editor')
    .factory('containerDefinitionFactory', containerDefinitionFactory);

  function containerDefinitionFactory(gettext) {

    return {
      createTabsContainerWidget: createTabsContainerWidget,
      createContainerWidget: createContainerWidget,
      createFormContainerWidget: createFormContainerWidget
    };

    // using gettext to add key to catalog that will be later translated in a template
    function createContainerWidget() {
      return {
        container: true,
        custom: false,
        id: 'container',
        type: 'container',
        icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#fff" d="M50,6.7V3.8h-1v2.9H50z M50,11.4V8.6h-1v2.9H50z M50,16.2v-2.9h-1v2.9H50z M49,20h1v-1.9h-1v1V20zM44.1,20h2.9v-1h-2.9V20z M39.2,20h2.9v-1h-2.9V20z M34.3,20h2.9v-1h-2.9V20z M29.4,20h2.9v-1h-2.9V20z M24.5,20h2.9v-1h-2.9V20zM19.6,20h2.9v-1h-2.9V20z M14.7,20h2.9v-1h-2.9V20z M9.8,20h2.9v-1H9.8V20z M4.9,20h2.9v-1H4.9V20z M0,18.1V20h2.9v-1H1v-1H0zM0,13.3v2.9h1v-2.9H0z M0,8.6v2.9h1V8.6H0z M0,3.8v2.9h1V3.8H0z M2.9,0H0v1.9h1V1h2V0z M7.8,0H4.9v1h2.9V0z M12.7,0H9.8v1h2.9V0zM17.6,0h-2.9v1h2.9V0z M22.5,0h-2.9v1h2.9V0z M27.5,0h-2.9v1h2.9V0z M32.4,0h-2.9v1h2.9V0z M37.3,0h-2.9v1h2.9V0z M42.2,0h-2.9v1h2.9V0z M47.1,0h-2.9v1h2.9V0z M50,0h-1v1v1h1V0z"/></svg>',
        name: 'Container',
        description: gettext('Group of widgets used to define the arrangement of the page elements. Its content can be repeated over an array'),
        order: -2,
        properties: [
          {
            name: 'repeatedCollection',
            label: gettext('Collection'),
            help: gettext('Number of array elements defines the number of times the container structure is repeated. Array data is available to widgets in the container'),
            caption: gettext('Repeat container content. Variable of type array'),
            icon: {
              className: 'fa fa-list-ul'
            },
            type: 'string',
            bond: 'variable'
          }
        ]
      };
    }

    function createTabsContainerWidget() {
      return {
        container: true,
        custom: false,
        id: 'tabsContainer',
        type: 'tabsContainer',
        icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#fff" d="M50,11.4V8.6h-1v2.9H50z M50,16.2v-2.9h-1v2.9H50z M49,20h1v-1.9h-1v1V20z M44.1,20h2.9v-1h-2.9V20zM39.2,20h2.9v-1h-2.9V20z M34.3,20h2.9v-1h-2.9V20z M29.4,20h2.9v-1h-2.9V20z M24.5,20h2.9v-1h-2.9V20z M19.6,20h2.9v-1h-2.9V20zM14.7,20h2.9v-1h-2.9V20z M9.8,20h2.9v-1H9.8V20z M4.9,20h2.9v-1H4.9V20z M0,18.1V20h2.9v-1H1v-1H0z M0,13.3v2.9h1v-2.9H0zM0,8.6v2.9h1V8.6H0z M0,1v5.7h1V1H0z M15.7,0H1v1h14.7V0z M16.7,1h-1v4.8h2v-1h-1V1z M22.5,4.8h-2.9v1h2.9V4.8z M27.5,4.8h-2.9v1h2.9V4.8z M32.4,4.8h-2.9v1h2.9V4.8z M37.3,4.8h-2.9v1h2.9V4.8z M42.2,4.8h-2.9v1h2.9V4.8z M47.1,4.8h-2.9v1h2.9V4.8z M50,4.8h-1v1v1h1V4.8z"/><path fill="#CBD5E1" d="M34.3,1h-1v3.8h1V1z M18.6,1h14.7V0H18.6V1z M18.6,4.8V1h-1v3.8H18.6z"/></svg>',
        order: -1,
        name: 'Tabs container',
        description: gettext('Multiple groups of widgets, each group in a tab'),
      };
    }

    function createFormContainerWidget() {
      return {
        container: true,
        custom: false,
        id: 'formContainer',
        type: 'formContainer',
        icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 20"><path fill="#fff" d="M5 19h3v1H5v-1zm5-18h3V0h-3v1zM5 1h3V0H5v1zm5 19h3v-1h-3v1zm5 0h3v-1h-3v1zm5 0h3v-1h-3v1zM15 1h3V0h-3v1zM1 9H0v2h1V9zM0 2h1V1h2V0H0v2zm23-2h-3v1h3V0zM1 4H0v3h1V4zm0 14H0v2h3v-1H1v-1zm0-5H0v3h1v-3zM27 0h-2v1h2V0zm22 19v1h1v-1.9h-1v.9zm-24 1h2v-1h-2v1zM44 1h3V0h-3v1zm5 15h1v-3h-1v3zm0-16v2h1V0h-1zm0 7h1V4h-1v3zm0 4h1V9h-1v2zm-5 9h3v-1h-3v1zM39 1h3V0h-3v1zM29 1h3V0h-3v1zm0 19h3v-1h-3v1zm5 0h3v-1h-3v1zm0-19h3V0h-3v1zm5 19h3v-1h-3v1zm-9-6H18v4h12v-4z"/><path fill="#CBD5E1" d="M45 2v4H18V2h27zM18 12h27V8H18v4zm4 5h4v-2h-4v2zM7 5h9V3H7v2zm-2 6h7V9H5v2zm8 0h3V9h-3v2z"/></svg>',
        name: 'Form container',
        order: 0,
        description: gettext('Container used for a form. Eases validation'),
      };
    }
  }

})();
