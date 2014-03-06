/*
 * An interface to the simple cloud lamp:
 *
 * `GET /`: Returns 1 if the lamp is on. Otherwise returns 0.
 * `POST /on`: Turns the lamp on.
 * `POST /off`: Turns the lamp off.
 *
 * @author Boris Smus <boris@smus.com>
 */

var REFRESH_RATE = 500;
function LampController(host) {
  this.host = host;

  // Internal state.
  this.isLit = undefined;

  this.callbacks = {};

  // Start polling the lamp for changes to status.
  this._checkStatus();
  setInterval(this._checkStatus.bind(this), REFRESH_RATE);
}

LampController.prototype.on = function() {
  this._makeRequest('POST', '/on');
};

LampController.prototype.off = function() {
  this._makeRequest('POST', '/off');
};

LampController.prototype.toggle = function() {
  if (this.isLit) {
    this.off();
  } else {
    this.on();
  }
};

LampController.prototype.onChange = function(callback) {
  this.callbacks.changed = callback;
};

LampController.prototype._checkStatus = function() {
  this._makeRequest('GET', '/', function(response) {
    var isLit = !!parseInt(response);
    if (isLit !== this.isLit) {
      this.isLit = isLit;
      if (this.callbacks.changed) {
        this.callbacks.changed(isLit);
      }
    }
  }.bind(this));
};

LampController.prototype._makeRequest = function(method, url, callback) {
  var xhr = new XMLHttpRequest();
  xhr.addEventListener('load', function(e) {
    if (callback) {
      callback(xhr.response);
    }
  });
  xhr.open(method, this.host + url);
  xhr.send();
};
