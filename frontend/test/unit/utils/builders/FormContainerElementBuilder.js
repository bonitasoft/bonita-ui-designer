export default class FormContainerElementBuilder {

  constructor() {
    this.container = {
      rows: []
    };
  }

  static aFormContainer() {
    return new FormContainerElementBuilder();
  }

  withRow(row) {
    this.container.rows.push(row);
    return this;
  }
}
