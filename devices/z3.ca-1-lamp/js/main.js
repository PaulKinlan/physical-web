var lamp = new LampController('http://lamp-server.appspot.com');
var cmdEl = document.querySelector('#command');

lamp.onChange(onLampChange);

function onLampChange(e) {
  var text = e ? 'On :)' : 'Off :(';
  var lampEl = document.querySelector('#lamp');
  var switchEl = document.querySelector('#switch');
  if (e) {
    lampEl.classList.add('on');
    switchEl.classList.add('on');
  } else {
    lampEl.classList.remove('on');
    switchEl.classList.remove('on');
  }
}
