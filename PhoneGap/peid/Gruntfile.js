module.exports = function (grunt) {

    require('load-grunt-tasks')(grunt)

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        includeSource: {
            options: {
                basePath: 'www',
                baseUrl: '',
                templates: {
                    js: '<script src="{filePath}"></script>',
                    css: '<link rel="stylesheet" type="text/css" href="{filePath}" />',
                }
            },
            myTarget: {
                files: {
                    'www/index.html': 'www/index.tpl.html'
                }
            }
        },
        watch: {
            scripts: {
                files: ['www/js/**/*.js', 'www/css/**/*.css', 'www/index.tpl.html'],
                tasks: ['includeSource'],
                options: {
                    spawn: false,
                    interrupt: true,
                    debounceDelay: 250
                }
            }
        },
        shell: {
            runTestServer: {                                // Task
                command: 'npm test',
                options: {
                    async: true
                }
            },
            runIonicServer: {
                command: 'ionic serve',
                options: {
                    async: true
                }
            }

        }
    });

    // Default task(s).
    grunt.registerTask('default', ['shell:runTestServer', 'shell:runIonicServer', 'watch']);

};
