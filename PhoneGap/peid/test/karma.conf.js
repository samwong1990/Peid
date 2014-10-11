module.exports = function(config){
  config.set({

    basePath : '../',

    files : [
      'www/lib/ionic/js/ionic.bundle.js',
      'www/lib/angular-mocks/angular-mocks.js',
      'www/lib/lodash/dist/lodash.js',
      'www/lib/lodash-contrib/dist/lodash-contrib.js',
      'www/lib/parse-js-sdk/lib/parse-1.2.19.min.js',
      'www/lib/ionic-rating/ionic-rating.min.js',
      'www/js/**/*.js',
      'test/unit/**/*.js'
    ],

    preprocessors : {
      'www/js/**/*.js': ['coverage']
    },

    autoWatch : true,

    frameworks: ['jasmine'],

    browsers : ['Chrome'],

    plugins : [
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-jasmine',
            'karma-junit-reporter',
            'karma-coverage'
            ],

    reporters: ['progress', 'junit', 'coverage'],

    coverageReporter: {
      reporters:[
        {type: 'html', dir:'test_out/'},
        {type: 'text'},
        {type: 'text-summary'}
      ],
    },

    junitReporter : {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    }

  });
};
