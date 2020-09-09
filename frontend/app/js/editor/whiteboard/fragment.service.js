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

  class FragmentService {
    constructor(properties, components, gettext, pageElementFactory, whiteboardComponentWrapper) {
      this.properties = properties;
      this.components = components;
      this.gettext = gettext;
      this.pageElementFactory = pageElementFactory;
      this.whiteboardComponentWrapper = whiteboardComponentWrapper;
    }

    createPaletteItem(item) {
      var extended = this.properties.addCommonPropertiesTo(item);
      return {
        component: extended,
        sectionOrder: 3,
        sectionName: this.gettext('fragments'),
        init: this.whiteboardComponentWrapper.wrapFragment.bind(null, extended),
        create: parentRow => this.createFragment(extended, parentRow)
      };
    }

    register(fragments) {
      var fragmentItems = fragments.filter(fragment => !fragment.status || fragment.status.compatible).map(fragment => this.createPaletteItem(fragment));
      this.components.register(fragmentItems);
      return fragmentItems;
    }

    createFragment(definition, parentRow) {
      var fragment = this.pageElementFactory.createFragmentElement(definition);
      this.whiteboardComponentWrapper.wrapFragment(definition, fragment, parentRow);
      return fragment;
    }

  }

  angular
    .module('bonitasoft.designer.editor.whiteboard')
    .service('fragmentService', (properties, components, gettext, pageElementFactory, whiteboardComponentWrapper) => new FragmentService(properties, components, gettext, pageElementFactory, whiteboardComponentWrapper));
})();
