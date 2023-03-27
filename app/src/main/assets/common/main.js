function isEmpty(val){
    return (val === undefined || val == null || val.length <= 0) ? true : false;
}

function alert(val) {
    Android.showMessage(val);
}

//option 1
function getScreenWidth() {
    return window.innerWidth
           || document.documentElement.clientWidth
           || document.body.clientWidth;
}

function getScreenHeight() {
    return window.innerHeight
           || document.documentElement.clientHeight
           || document.body.clientHeight;
}

function getClientWidth() {
    return document.documentElement.clientWidth
           || document.body.clientWidth;
}

function getClientHeight() {
    return document.documentElement.clientHeight
           || document.body.clientHeight;
}

function getElementWidth(elementId) {
    return document.getElementById(elementId).offsetWidth;
}

function getElementHeight(elementId) {
    return document.getElementById(elementId).offsetHeight;
}

function getClientHeight() {
    return document.documentElement.clientHeight
           || document.body.clientHeight;
}

function getDeviceWidth() {
    return Android.isPortrait() ? Android.getScreenWidth() : Android.getScreenHeight();
}

function getDeviceHeight() {
    return Android.isPortrait() ? Android.getScreenHeight() : Android.getScreenWidth();
}

function zoomElement(domElement, percentage) {
    domElement.style["zoom"] = (percentage > 1 ? percentage : (percentage * 100)) + "%";
}

function zoomFitScreen() {
    var ratio =  Math.round((getClientWidth() * 100) / getDeviceWidth());
    zoomElement(document.body, ratio);
    //alert("Ratio:" + ratio + " Client: " + getClientWidth() + ":" + getClientHeight() + "Device:" + getDeviceWidth() + ":" + getDeviceHeight() + ". Orient:" + Android.getOrientation());
}

function getZoomRatio(w1, w2) {
    return w1 < w2 ? Math.round((w1 * 100) / w2) : Math.round((w2 * 100) / w1);
}

function zoomElementFitScreen(elementId) {
    var ratio = getZoomRatio(getElementWidth(elementId), getDeviceWidth()); //Math.round((getElementWidth(elementId) * 100) / getDeviceWidth());
    //ratio = "80";
    zoomElement(document.body, ratio);
    //alert("Ratio:" + ratio + " Element: " + getElementWidth(elementId) + ":" + getElementHeight(elementId) + "Device:" + getDeviceWidth() + ":" + getDeviceHeight() + ". Orient:" + Android.getOrientation());
}

function zoomElementByWidth(width) {
    var ratio = getZoomRatio(width, getDeviceWidth()); //Math.round((getElementWidth(elementId) * 100) / getDeviceWidth());
    zoomElement(document.body, ratio);
}

function openScreen(screen) {
    //Android.showMessage(screen);
    if (isEmpty(screen))
        return;

    window.location.href = Android.getUrlForClient(screen); //set timeout=0
}

function getContentJson(id) {
    return Android.getContentJson(id);
}

function getData() {
    // prepare data
    let params = (new URL(document.location)).searchParams;
    let id = params.get("content_id");
    let json = null;

    if (isEmpty(id) || id == 'android')
        json = Android.getDeviceImagesJson();
    else
        json = Android.getContentJson(id);

    return getDataFromJson(json);
}

function getDataFromJson(json) {
    //alert(json);
    let data = [];
    if (json != null && json != undefined) {
        let obj = JSON.parse(json);
        if ((!isEmpty(obj.status) && obj.status.toLowerCase() == 'fail') || (!isEmpty(obj.code) && obj.code != 200)) {
            alert("Status: " + obj.status + ". Code: " + obj.code + ". Error message: " + obj.message);
            return [];
        }
        data = isEmpty(obj.data) ? json : obj.data;
        if (!isEmpty(data.data))
            data = data.data;
    }
    return data;
}

//not working well if url start with file://
function getUrlParam1(name, defaultValue, url ) {
    if (!url) url = location.href;
    const urlParams = new URLSearchParams(url);
    let names = [];
    if (name.isArray) {
        names = name;
    } else
        names = names.concat(name);

    for (var i = 0; i < names.length; i++) {
        let name1 = names[i];
        if (!isEmpty(urlParams.get(name1)))
            return urlParams.get(name1);
    }
    return defaultValue;
}

function getUrlParam(name, defaultValue, url ) {
    if (!url) url = location.href;
    let names = [];
    if (name.isArray) {
        names = name;
    } else
        names = names.concat(name);

    for (var i = 0; i < names.length; i++) {
        let name1 = names[i];
        name1 = name1.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
        var regexS = "[\\?&]" + name1 + "=([^&#]*)";
        var regex = new RegExp( regexS, "i" );
        var results = regex.exec( url );
        if (results == null)
            continue;
        return results[1];
    }
    return defaultValue;
}

function getAllUrlParams(url) {

  // get query string from url (optional) or window
  var queryString = url ? url.split('?')[1] : window.location.search.slice(1);

  // we'll store the parameters here
  var obj = {};

  // if query string exists
  if (queryString) {

    // stuff after # is not part of query string, so get rid of it
    queryString = queryString.split('#')[0];

    // split our query string into its component parts
    var arr = queryString.split('&');

    for (var i = 0; i < arr.length; i++) {
      // separate the keys and the values
      var a = arr[i].split('=');

      // set parameter name and value (use 'true' if empty)
      var paramName = a[0];
      var paramValue = typeof (a[1]) === 'undefined' ? true : a[1];

      // (optional) keep case consistent
      paramName = paramName.toLowerCase();
      if (typeof paramValue === 'string') paramValue = paramValue.toLowerCase();

      // if the paramName ends with square brackets, e.g. colors[] or colors[2]
      if (paramName.match(/\[(\d+)?\]$/)) {

        // create key if it doesn't exist
        var key = paramName.replace(/\[(\d+)?\]/, '');
        if (!obj[key]) obj[key] = [];

        // if it's an indexed array e.g. colors[2]
        if (paramName.match(/\[\d+\]$/)) {
          // get the index value and add the entry at the appropriate position
          var index = /\[(\d+)\]/.exec(paramName)[1];
          obj[key][index] = paramValue;
        } else {
          // otherwise add the value to the end of the array
          obj[key].push(paramValue);
        }
      } else {
        // we're dealing with a string
        if (!obj[paramName]) {
          // if it doesn't exist, create property
          obj[paramName] = paramValue;
        } else if (obj[paramName] && typeof obj[paramName] === 'string'){
          // if property does exist and it's a string, convert it to an array
          obj[paramName] = [obj[paramName]];
          obj[paramName].push(paramValue);
        } else {
          // otherwise add the property
          obj[paramName].push(paramValue);
        }
      }
    }
  }

  return obj;
}

function callAPI(url, callback) {
    return Android.callAPI(url, callback);
}

function setHTML(id, newvalue) {
  var s= document.getElementById(id);
  s.innerHTML = newvalue;
}

function setHtml(id, newvalue) {
  var s= document.getElementById(id);
  s.innerHTML = newvalue;
}

function setValue(id, newvalue) {
  var s= document.getElementById(id);
  s.innerHTML = newvalue;
}

var getJSON = function(url, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'json';
    xhr.onload = function() {
      var status = xhr.status;
      if (status === 200) {
        callback(null, xhr.response);
      } else {
        callback(status, xhr.response);
      }
    };
    xhr.send();
};

//getJSON(url2, function(err, data) {
//  if (err !== null) {
//    Android.alert('Something went wrong: ' + err);
//  } else {
//    Android.alert('Your query count: ' + data);
//  }
//});

//option 2
async function getHtml(url) {
  const request = await $.get(url)
  return request
}

//getHtml(url3)
//  .then((data) => { Android.alert('getHtml: ' + data.response.title); })
//  .then(() => { console.log('2')});


// option 3
async function getResponse(url) {
  const response = await fetch(url);
  const json = await response;
  return json;
}

async function getResponseJson(url) {
  const response = await fetch(url);
  const json = await response.json();
  return json;
}

//getData(url2)
//    .then(data => { Android.alert('getData: ' + data);}) ;

// option 4
async function callAndroidAsync(javaFuncName, params) {
    const rand = 'asyncJava_' + Math.floor(Math.random() * 1000000);
    window[rand] = {};

    // func called from android
    window[rand].callback = (isSuccess) => {
        const dataOrErr = Android.runAsyncResult(rand);
        if (isSuccess)
            window[rand].resolve(dataOrErr);
        else
            window[rand].reject(dataOrErr);
        delete window[rand]; // clean up
    }

    // call some android function that returns immediately - should run in a new thread
    // setTimeout(() => window[rand].callback(false, params.val * 2), 4000) // see testCallJavaAsync
    Android.runAsync(rand, javaFuncName, JSON.stringify(params));

    return new Promise((resolve, reject) => {
        window[rand].resolve = (data) => resolve(data);
        window[rand].reject = (err) => reject(err);
    })
}

//let res = await callAndroidAsync('callAPI', { url: url2, listener: null });
