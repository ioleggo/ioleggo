'use strict';

Hyphenator.config({hyphenchar:'|', minwordlength:3});

var ioLeggoServices = angular.module('ioLeggoServices', ['ngResource']);

ioLeggoServices.factory('Stories', function($resource){
    return $resource('/stories/');
});

ioLeggoServices.factory('Story', function($http) {
  var Story = {
    get: function(group, title, callback) {
        var promise;
        promise = $http.get('/story/?group='+group+'&title='+title).then(function (response) {
          callback(response.data);
        });
      return promise;
    },

    isNewLine: function(word) {
        if (word !== undefined && word.match(/<\s*\w.*?>/g) != null) {
            return true;
        } else {
            return false;
        }
    },

    load: function(group, title, callback) {
        // We may use an array of objects instead of a bi-dimensional array but this way it's easier for the controller and directive
        var words = [[],[]];
        var syllables = [];
        Story.get(group, title, function(story) {
            var storyWords = story.match(/\S+/g);
            if (storyWords !== undefined) {
                storyWords.forEach(function(word, index, array) {
                if (Story.isNewLine(word)) {
                    words[0].push(word);
                    words[1].push(0);
                    syllables.push(word);
                } else {
                    //To handle correctly hyphenation of words containing '
                    var i = word.indexOf("'");
                    var bar = [];
                    if (i==-1) {
                        bar.push(Hyphenator.hyphenate(word, 'it').split('|'));
                    } else {
                        bar.push(Hyphenator.hyphenate(word.substring(0, i+1), 'it').split('|'));
                        bar.push(Hyphenator.hyphenate(word.substring(i+1, word.length), 'it').split('|'));
                    }
                    //Hyphenator introduces some unforeseen (buggy?) "_undefined"
                    bar.forEach(function(syllable) {
                        var cleanedSyllable = [];
                        if ($.isArray(syllable)) {
                          syllable.forEach(function(s) {
                            if (s.indexOf("_undefined") > -1) {
                                cleanedSyllable.push(s.replace("_undefined", ""));
                            } else {
                                cleanedSyllable.push(s);
                            }
                          });
                          words[0].push(cleanedSyllable.join(''));
                          words[1].push(cleanedSyllable.length);
                          syllables = syllables.concat(cleanedSyllable);
                          //Not the last word
                          if (index < storyWords.length-1) {
                              words[0].push('&#32;<wbr>');
                              words[1].push(0);
                              syllables.push('&#32;<wbr>');
                          }
                        }
                    });
                }
            });
            }
            callback(words, syllables);
        });
    }
  };
  return Story;
});

ioLeggoServices.factory('Config', function($location, $localStorage) {
  var config = $localStorage.ioLeggoConfig;

  var readingModes =$location.search().readingModes;

  if (readingModes !== undefined && readingModes.length > 0) {
    console.log(readingModes);
    $localStorage.ioLeggoReadingModes = JSON.parse(readingModes.replace(/\\"/,"\""));
  }

  if ($localStorage.ioLeggoConfig===undefined) {
      $localStorage.ioLeggoConfig = {
            syllabation: true,
            wordsDelay: 5,
            syllablesDelay: 150,
            hideTextBefore: false,
            hideTextAfter: true,
            fontSize: 300,
            fontColor: "rgb(0, 190, 120)",
            toReadColor: "rgb(255,255,255)",
            backgroundColor: "rgb(0,0,0)"
       };
  }

  $localStorage.ioLeggoConfig = {
        hideTextBefore: $localStorage.ioLeggoConfig.hideTextBefore,
        hideTextAfter: $localStorage.ioLeggoConfig.hideTextAfter,
        syllabation: $localStorage.ioLeggoConfig.syllabation,
        syllablesDelay: $localStorage.ioLeggoConfig.syllablesDelay,
        wordsDelay: $localStorage.ioLeggoConfig.wordsDelay,
        fontSize: $localStorage.ioLeggoConfig.fontSize,
        fontColor: $localStorage.ioLeggoConfig.fontColor,
        toReadColor: $localStorage.ioLeggoConfig.toReadColor,
        backgroundColor: $localStorage.ioLeggoConfig.backgroundColor,

        store: function(group, title, index) {
            if (group !== undefined && title !== undefined) {
                $localStorage.ioLeggoStory = {"group": group, "title": title, "index": index};
            }
        },

        load: function() {
            if ($localStorage.ioLeggoStory === undefined) {
                return {"group": undefined, "title": undefined, "index": -1};
            }
            return $localStorage.ioLeggoStory;
        },

        loadReadingModes: function() {
            if ($localStorage.ioLeggoReadingModes === undefined) {
                return {};
            }
            return $localStorage.ioLeggoReadingModes;
        },

        loadReadingModeNames: function() {
            return Object.keys(this.loadReadingModes());
        },

        addReadingModes: function(readingMode) {
            if (readingMode !== undefined && readingMode.length > 0) {
                var readingModes = this.loadReadingModes();
                readingModes[readingMode] = JSON.parse(JSON.stringify(this));
                $localStorage.ioLeggoReadingModes = readingModes;
            }
        },

        storeReadingModes: function(readingModes) {
           if (readingModes !== undefined && readingModes.length > 0) {
             $localStorage.ioLeggoReadingModes = JSON.parse(readingModes.replace(/\\"/,"\""));
           }
        },

        deleteReadingModes: function(readingMode) {
            var readingModes = this.loadReadingModes();
            delete readingModes[readingMode];
            $localStorage.ioLeggoReadingModes = readingModes;
        },

        setReadingMode: function(readingMode) {
            var readingMode = this.loadReadingModes()[readingMode];
            this.hideTextBefore = readingMode.hideTextBefore;
            this.hideTextAfter = readingMode.hideTextAfter;
            this.syllabation = readingMode.syllabation;
            this.syllablesDelay = readingMode.syllablesDelay;
            this.wordsDelay = readingMode.wordsDelay;
            this.fontSize = readingMode.fontSize;
            this.fontColor = readingMode.fontColor;
            this.toReadColor = readingMode.toReadColor;
            this.backgroundColor = readingMode.backgroundColor;
        }

   };

  return $localStorage.ioLeggoConfig;
});

