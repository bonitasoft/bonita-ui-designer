const {{resourceName}} = {
{{#each resources}}
    '{{ @key }}':  {{ json this }},
{{/each}}
    get: function (entry: string): any {
        for (const [key, value] of Object.entries(this)) {
            if (key === entry) {
                return value;
            }
        }
    }
};

export default {{resourceName}};
