const propertiesValues = {
    'component-ref':  {"foo":{"type":"bar","value":"baz"}},
    get: function (entry: string): any {
        for (const [key, value] of Object.entries(this)) {
            if (key === entry) {
                return value;
            }
        }
    }
};

export default propertiesValues;
