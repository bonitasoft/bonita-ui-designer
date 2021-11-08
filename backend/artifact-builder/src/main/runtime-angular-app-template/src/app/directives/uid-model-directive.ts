import { Directive, Input, OnInit, ViewContainerRef, TemplateRef } from '@angular/core';
import { Model, Variables } from '@bonitasoft/ui-designer-context-binding';
import variableModel from '../../assets/variableModel';
/**
 * Allow us to declare a directive to setup a structural directive like following
 * ex:
 * <div *reference="'uuid'; let properties;"></div>
 */
@Directive({
    selector: '[uid-model]'
})
export class uidModel implements OnInit {
    @Input('uid-model') uidModel: string = '';

    // Properties input
    @Input('model') uidContext: any = {};

    private variablesAccessors: Object = {};
    private modelCtrl: Model;

    constructor(private templateRef: TemplateRef<any>,
        private viewContainerRef: ViewContainerRef) {
        this.modelCtrl = new Model();
    }

    ngOnInit(): void {
        let uidVariables: Variables = variableModel.get(this.uidModel);

        this.modelCtrl.fillVariableAccessors(new Map(Object.entries(uidVariables)));

        this.variablesAccessors = {
            $implicit: this.modelCtrl.variableAccessors,
        };
        this.viewContainerRef.createEmbeddedView(this.templateRef, this.variablesAccessors);
    }

    getVariableAccessors() {
        return this.modelCtrl.variableAccessors;
    }
}
