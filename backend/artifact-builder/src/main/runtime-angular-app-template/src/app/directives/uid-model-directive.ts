import { Directive, Input, OnInit, ViewContainerRef, TemplateRef } from '@angular/core';
import { Model, ModelFactory, Variables } from '@bonitasoft/ui-designer-context-binding';
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
    private _context: UidModelContext = new UidModelContext();

    @Input('uid-model') set uidModel(model: string) {
        this._context.uidModel = model;
    }

    // Properties input
    @Input('model') uidContext: any = {};

    private variablesAccessors: Object = {};
    private modelFactory!: ModelFactory;

    constructor(private templateRef: TemplateRef<any>,
                private viewContainerRef: ViewContainerRef) {
    }

    ngOnInit(): void {
        let uidVariables: Variables = variableModel.get(this._context.uidModel);
        this.modelFactory = new ModelFactory(new Map(Object.entries(uidVariables)));
        let variableAccessors = this.modelFactory.createVariableAccessors();

        this.variablesAccessors = {
            $implicit: variableAccessors,
        };
        this.viewContainerRef.createEmbeddedView(this.templateRef, this.variablesAccessors);
    }

    getModelFactory() {
        return this.modelFactory;
    }

}

export class UidModelContext {
    public uidModel: any = null;
}
