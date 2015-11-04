angular
  .module('pageDataMock',[])
  .value('pageJson', {
    rows: [
      [
        { type: 'component', widgetId: 'pbLabel' },
        { type: 'component', widgetId: 'pbInput' }
      ],

      [
        { type: 'component', widgetId: 'pbLabel' },
        { type: 'component', widgetId: 'pbTitle' }
      ],

      [
        {
          type: 'container',
          rows: [
            [
              { type: 'component', widgetId: 'pbParagraph' }
            ]
          ]
        }
      ],

      [
        {
          type: 'tabContainer',
          $$openedTab: {
            container: {
              rows: [
                [
                  { type: 'component', widgetId: 'pbInput' }
                ]
              ]
            }
          },
          tabs: [
            {
              container: {
                rows: [
                  [
                    { type: 'component', widgetId: 'pbInput' }
                  ]
                ]
              }
            },
            {
              container: {
                rows: [
                  [
                    { type: 'component', widgetId: 'pbInput' },
                    { type: 'component', widgetId: 'pbInput' },
                    { type: 'component', widgetId: 'pbInput' }
                  ]
                ]
              }
            }
          ]
        }
      ]
    ]
  });
