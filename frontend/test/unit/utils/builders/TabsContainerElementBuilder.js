class TabsContainerElementBuilder {

  constructor() {
    this.tabs = [];
  }

  static aTabsContainer() {
    return new TabsContainerElementBuilder();
  }

  withTab(tab) {
    this.tabs.push(tab);
    return this;
  }
}

export default TabsContainerElementBuilder.aTabsContainer;