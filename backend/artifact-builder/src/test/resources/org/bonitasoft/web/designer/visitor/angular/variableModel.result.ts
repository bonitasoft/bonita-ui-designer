const propertiesValues = {
    'page-id':  {"foo":{"type":"constant","value":["bar"],"displayValue":"bar","exposed":false}},
    get: function (entry: string): any {
        for (const [key, value] of Object.entries(this)) {
            if (key === entry) {
                return value;
            }
        }
    }
};

export default propertiesValues;
