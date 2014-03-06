/**
 * Wrapper around JS speech engine.
 *
 * @author Boris Smus <boris@smus.com>
 */
(function(exports) {

var DEFAULT_CONFIDENCE = 0.1;
/**
 * A wrapper around the JavaScript speech API that provides the right
 * behavior (start listening, start transcribing, stop listening but
 * continue transcribing, then stop transcribing and callback).
 */
function SpeechEngine(options) {
  options = options || {};
  this.sr = new webkitSpeechRecognition();
  this.callbacks = {};

  this.confidence = options.confidence || DEFAULT_CONFIDENCE;
  this.autorestart = options.autorestart || false;
}

SpeechEngine.prototype.start = function() {
  this.sr.onerror = this._onError.bind(this);
  this.sr.onresult = this._onResult.bind(this);
  this.sr.onstart = this._onStart.bind(this);
  this.sr.onspeechstart = this._onSpeechStart.bind(this);
  this.sr.onspeechend = this._onSpeechEnd.bind(this);

  this.sr.maxAlternatives = 10;
  this.sr.interimResults = true;
  this.sr.continuous = true;
  this.sr.lang = 'en';
  this.sr.start();
  this._log('started listening');
};

SpeechEngine.prototype.stop = function() {
  this.sr.stop();
  this._log('stopped listening');
};

SpeechEngine.prototype.onStart = function(callback) {
  this.callbacks.start = callback;
};

SpeechEngine.prototype.onSpeechStart = function(callback) {
  this.callbacks.speechStart = callback;
};

SpeechEngine.prototype.onSpeechEnd = function(callback) {
  this.callbacks.speechEnd = callback;
};

SpeechEngine.prototype.onResult = function(callback) {
  this.callbacks.result = callback;
};

SpeechEngine.prototype.onError = function(callback) {
  this.callbacks.error = callback;
};

SpeechEngine.prototype._log = function(msg) {
  console.log('SpeechEngine:', msg);
};
/********** PRIVATE ************/

SpeechEngine.prototype._errorToString = function(errno) {
  switch(errno) {
    case 0: return "OTHER";
    case 1: return "NO_SPEECH";
    case 2: return "ABORTED";
    case 3: return "AUDIO_CAPTURE";
    case 4: return "NETWORK";
    case 5: return "NOT_ALLOWED";
    case 6: return "SERVICE_NOT_ALLOWED";
    case 7: return "BAD_GRAMMAR";
    case 8: return "LANGUAGE_NOT_SUPPORTED";
    default: return "? ("+errno+")";
  }
};

SpeechEngine.prototype._onError = function(res) {
  var error_code = 0;
  if(res.error) {
    error_code = res.error.code;
  } else if (res.code) {
    error_code = res.code;
  }
  var error = this._errorToString(error_code);
  console.log('Error: ' + error);
  this._fireCallback(this.callbacks.error, {error: error, errorCode: error_code});

  if (this.autorestart) {
    this.start();
  }
};

SpeechEngine.prototype._fireCallback = function(callback, args) {
  if (callback) {
    callback(args);
  }
};

SpeechEngine.prototype._onResult = function(e) {
  // Discard insufficiently confident results.
  var results = e.result;
  if (results.length > 0) {
    var result = results[0];
    if (result.confidence < this.confidence) {
      return;
    }
    var data = {
      transcript: result.transcript,
      isFinal: results.final,
      confidence: result.confidence
    };
    //console.log(data);
    this._fireCallback(this.callbacks.result, data);
  }
};

SpeechEngine.prototype._onStart = function(e) {
  this._fireCallback(this.callbacks.start);
};

SpeechEngine.prototype._onSpeechStart = function(e) {
  this._fireCallback(this.callbacks.speechStart);
};

SpeechEngine.prototype._onSpeechEnd = function(e) {
  this._fireCallback(this.callbacks.speechEnd);
};

exports.SpeechEngine = SpeechEngine;

})(window);
