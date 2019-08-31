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
    deviceReady: false,
    lightbox: null,

    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        console.log('Device ready');   // #DEBUG
        this.deviceReady = true;
        this.initDevice();
        //alert('Device ready');
    },

    // Update DOM on a Received Event
    initDevice: function(id) {
        // intercept Exit by Back button press
        document.addEventListener("backbutton", function (e) {
            if (app.lightbox) {
                app.lightbox.close();
                e.preventDefault();
            }
            else if (($("#selectedWhiskyModalCenter").data('bs.modal') || {})._isShown) {
                $("#selectedWhiskyModalCenter").modal('hide');
                e.preventDefault();
            }
            else if (!confirm("Are you sure you want to close the app?")) {
                e.preventDefault();
            } else {
                navigator.app.exitApp();
            }
        }, false);


        // enable 'lightbox' data-toggle links
        $(document).on('click', '[data-toggle="lightbox"]', function (event) {
            event.preventDefault();
            $(this).ekkoLightbox({
                wrapping: false, alwaysShowClose: true,
                onShown: function () {
                    console.log('Popup shown'); // #DEBUG
                    app.lightbox =this;
                    $('body').bind('touchmove', app.preventDefault);
                },
                onNavigate: function (direction, itemIndex) {
                    console.log('Popup navigating ' + direction + '. Current item: ' + itemIndex);  // #DEBUG
                },
                onHidden: function (direction, itemIndex) {
                    console.log('Popup hidden');    // #DEBUG
                    app.lightbox =null;
                    $('body').unbind('touchmove', app.preventDefault);
                    $('#selectedWhiskyModalCenter').css('overflow', 'auto');
                }
            });
        });


        // navigator.geolocation.getCurrentPosition(function(position){
        //     this.geoAvailable = true;
        //     console.log('Geolocation success: ' + position);   // #DEBUG
        // }, function(error){
        //     this.geoAvailable = false;
        //     console.log('Geolocation failure: ' + error);   // #DEBUG
        // });
        //
        // console.log('Camera: ' + navigator.camera);   // #DEBUG
    },

    preventDefault: function (e) {
        e.preventDefault();
    }
};

app.initialize();