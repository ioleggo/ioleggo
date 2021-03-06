'use strict';

var ioLeggoControllers = angular.module('ioLeggoControllers', ['ngRoute', 'ioLeggoServices']);

var SOUND_LEN = 100;
var SOUND_FREQUENCY = 880;
var SCROLL_END=100000;
var DELAY_FACTOR = 10;
var MESSAGE_DELAY = 5000;
var MESSAGES = {
    "storyNotLoaded" : {"text": "Devi prima scegliere una storia!", "type":"danger", "delay":MESSAGE_DELAY}
};

ioLeggoControllers.controller('ioLeggoCtrl', function($scope, $timeout, $sce, $location, $anchorScroll, Config, Story, Stories) {
  var audioContext;
  var readingTimer = undefined;

  $scope.playSound = function(freq, len) {
      try {
        var oscillator = audioContext.createOscillator();
        oscillator.frequency.value = freq;
        oscillator.connect(audioContext.destination);
        //Firefox: Not available in Firefox < 28.0 due to WEB Audio API porting
        oscillator.noteOn(0);
        $timeout(function() {
              try {
                oscillator.noteOff(0);
              } catch (e) {}}
              , len);
      } catch (e) {};
  },

  $scope.scroll = function(to) {
    angular.element('html body').scrollTop(to);
  },

  $scope.readingModesModalShowMessageText = function(message, type, time) {
        $scope.readingModesModalMessage = $sce.trustAsHtml(message);
        $scope.readingModesModalMessageType = type;
        $timeout(function(){$scope.readingModesModalMessage=undefined;},time);
  },

  $scope.showMessageText = function(message, type, time) {
        $scope.message = $sce.trustAsHtml(message);
        $scope.messageType = type;
        $timeout(function(){$scope.message=undefined;},time);
  },

  $scope.showMessage = function(key) {
        $scope.showMessage(MESSAGES[key].text, MESSAGES[key].type, MESSAGES[key].delay);
  },

  $scope.isStoryLoaded = function() {
    return $scope.items !== undefined && $scope.items.length > 0;
  },

  $scope.computeWordDelay = function(syllables) {
    // 2* because the old program is slow while reading words
    return ((2*Config.wordsDelay) + (Config.syllablesDelay * syllables)) * DELAY_FACTOR;
  },

   $scope.computeSyllablesDelay = function() {
     return Config.syllablesDelay * DELAY_FACTOR;
   },

  $scope.startOver = function() {
    if ($scope.isStoryLoaded()) {
        $scope.pause();
        $scope.index=-1;
        $scope.scroll(0);
        Config.store($scope.folder, $scope.title, $scope.index);
    } else {
        $scope.showMessage("storyNotLoaded");
    }
  },

  $scope.stepBackward = function() {
    if ($scope.isStoryLoaded()) {
        $scope.pause();
        do {
            $scope.index--;
            if ($scope.isBeginOfTheStory()) {
                $scope.index=$scope.items.length-1;
                $scope.scroll(SCROLL_END);
            }
        }
        while (Story.isNewLine($scope.items[$scope.index]));
        Config.store($scope.folder, $scope.title, $scope.index);
    } else {
        $scope.showMessage("storyNotLoaded");
    }
  },

   $scope.isEndOfTheStory = function() {
       return $scope.index >= $scope.items.length;
   },

   $scope.isBeginOfTheStory = function() {
       return $scope.index < 0;
   },

  $scope.readNext = function() {
    if ($scope.playing && !$scope.isEndOfTheStory()) {
       do {
            $scope.index++;
        } while (Story.isNewLine($scope.items[$scope.index]));
        var delay = 0;
        if (!Story.isNewLine($scope.items[$scope.index])) {
            if (Config.syllabation) {
                delay = $scope.computeSyllablesDelay();
            } else {
                delay = $scope.computeWordDelay($scope.nrOfSyllables[$scope.index]);
            }
            if (Config.syllabation) {
                $scope.playSound(SOUND_FREQUENCY, SOUND_LEN);
            }
        }
        Config.store($scope.folder, $scope.title, $scope.index);
        readingTimer = $timeout($scope.readNext, delay);
    } else {
        $scope.index=-1;
        $scope.playing = false;
    }
  },

  $scope.play = function() {
    if ($scope.isStoryLoaded()) {
        $scope.showStory();
        $scope.playing = true;
        $scope.readNext();
    } else {
        $scope.showMessage("storyNotLoaded");
    }
  },

  $scope.showConfig = function() {
    $scope.pause();
  },

  $scope.closeConfig = function() {
    $scope.showStory();
    $scope.refreshReadingArea();
  },

  $scope.showReadingModes = function() {
    $scope.readingModes = Config.loadReadingModeNames();
  },

  $scope.closeReadingModes = function() {
    $scope.showStory();
    $scope.refreshReadingArea();
  },

  $scope.setReadingMode = function(readingMode) {
    Config.setReadingMode(readingMode);
    $scope.showStory();
    $scope.refreshReadingArea();
    $scope.readingMode = readingMode;
    $scope.showMessageText("La modalità di lettura <b>" + readingMode + "</b> è stata attivata.", "success", 5000);
  },

  $scope.loadReadingModes = function() {
    $scope.pause();
    $scope.readingModes = Config.loadReadingModeNames();
  },

  $scope.addReadingMode = function(readingMode) {
    Config.addReadingModes(readingMode);
    $scope.readingModes = Config.loadReadingModeNames();
    $scope.readingModesModalShowMessageText("<b>" + readingMode + "</b> è stato aggiunto.", "success", 5000);
  },

  $scope.deleteReadingMode = function(readingMode) {
    Config.deleteReadingModes(readingMode);
    $scope.readingModes = Config.loadReadingModeNames();
    $scope.readingModesModalShowMessageText("<b>" + readingMode + "</b> è stato eliminato.", "success", 5000);
  },

  $scope.showHelp = function() {
    $scope.pause();
  },

  $scope.closeHelp = function() {
  },

  $scope.pause = function() {
    $scope.playing = false;
    if (readingTimer!==undefined) {
        $timeout.cancel(readingTimer);
        readingTimer = undefined;
    }
    Config.store($scope.folder, $scope.title, $scope.index);
  },

  $scope.stepForward = function() {
    if ($scope.isStoryLoaded()) {
        $scope.pause();
        do {
            $scope.index++;
            if ($scope.isEndOfTheStory()) {
                $scope.index=0;
                $scope.scroll(0);
            }
        }
        while (Story.isNewLine($scope.items[$scope.index]));
        Config.store($scope.folder, $scope.title, $scope.index);
    } else {
        $scope.showMessage("storyNotLoaded");
    }
  },

  $scope.finishStory = function() {
    if ($scope.isStoryLoaded()) {
        $scope.pause();
        $scope.index=-1;
        $scope.scroll(SCROLL_END);
        Config.store($scope.folder, $scope.title, $scope.index);
    } else {
        $scope.showMessage("storyNotLoaded");
    }
  },

  $scope.reset = function() {
        $scope.folder = undefined;
        $scope.title = undefined;
        $scope.index=-1;
        $scope.words = [];
        $scope.syllables = [];
        $scope.nrOfSyllables = [];
        $scope.items = [];
        $scope.pause();
        $scope.refresh = false;
  },

  $scope.refreshReadingArea = function() {
    $scope.refresh = !$scope.refresh;
  },

  $scope.showStory = function() {
    if (Config.syllabation) {
        $scope.items = $scope.syllables;
    } else {
        $scope.items = $scope.words;
    }
  },

  $scope.loadStory = function(folder, title, reload) {
    if (folder !== undefined && title !== undefined) {
        Story.load(folder, title, function(words, syllables) {
            $scope.reset();
            $scope.folder = folder;
            $scope.title = title;
            $scope.syllables = syllables;
            $scope.words = words[0];
            $scope.nrOfSyllables = words[1];
            $scope.showStory();
            if (!reload) {
                Config.store(folder, title, -1);
                $scope.showMessageText('La storia <b>' + title + '</b> è stata caricata, buona lettura.', 'success', MESSAGE_DELAY);
            } else {
                $scope.showMessageText('Ora puoi continuare la lettura di <b>' + title + '</b>.', 'success', MESSAGE_DELAY);
                $scope.index=Config.load().index;
            }
        });
    }
  },

  $scope.computeWordsToSyllabationIndex = function() {
    if ($scope.index >= 0) {
        var index = 0;
        for (var i = 0; i < $scope.index; i++) {
            index += (($scope.nrOfSyllables[i])==0?1:$scope.nrOfSyllables[i]);
        }
        //Assure it's not a space or new line
        while (index >= 0 && Story.isNewLine($scope.syllables[index])) {
            index--;
        }
        $scope.index = index;
    }
  },

  $scope.computeSyllabationToWordsIndex = function() {
    if ($scope.index >= 0) {
        var index = 0;
        var syllabes = 0;
        while (syllabes <= $scope.index) {
            syllabes += (($scope.nrOfSyllables[index])==0?1:$scope.nrOfSyllables[index]);
            index++;
        }
        index--;
        //Assure it's not a space or new line
        while (index >= 0 && Story.isNewLine($scope.words[index])) {
            index--;
        }
        $scope.index = index;
    }
  }

  try {
    audioContext = new (window.AudioContext || window.webkitAudioContext);
  } catch (e) {}

  $scope.Config = Config;
  $scope.reset();
  $scope.readingModes = Config.loadReadingModeNames();
  $scope.titles = Stories.query();
  $scope.loadStory(Config.load().folder, Config.load().title, true);
  $scope.$watch('Config.syllabation', function(newSyllabation, oldSyllabation) {
     if (newSyllabation != oldSyllabation) {
        if (newSyllabation) {
            $scope.computeWordsToSyllabationIndex();
        } else {
            $scope.computeSyllabationToWordsIndex();
        }
     }
  });

});