/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    cameraAvailable: false,
    geoAvailable: false,

    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        console.log('Received Event: ' + id);   // #DEBUG

        // var parentElement = document.getElementById(id);
        // var listeningElement = parentElement.querySelector('.listening');
        // var receivedElement = parentElement.querySelector('.received');
        //
        // listeningElement.setAttribute('style', 'display:none;');
        // receivedElement.setAttribute('style', 'display:block;');

        document.addEventListener("backbutton", function (e) {
            if (lightbox) {
                lightbox.close();
                e.preventDefault();
            }
            else if (($("#selectedWhiskyModalCenter").data('bs.modal') || {})._isShown) {
                $("#selectedWhiskyModalCenter").modal('hide')
                e.preventDefault();
            }
            else if (!confirm("Are you sure you want to close the app?")) {
                e.preventDefault();
            } else {
                navigator.app.exitApp();
            }
        }, false);

        navigator.geolocation.getCurrentPosition(function(position){
            this.geoAvailable = true;
            console.log('Geolocation success: ' + position);   // #DEBUG
        }, function(error){
            this.geoAvailable = false;
            console.log('Geolocation failure: ' + error);   // #DEBUG
        });

        console.log('Camera: ' + navigator.camera);   // #DEBUG
    }
};

app.initialize();