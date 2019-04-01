/* eslint-disable promise/always-return */
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
exports.myNotification = functions.database.ref('Notifications/{id}/Token').onCreate(newName => {
    //prepare notification payload
    const payload = {
        notification:{
            title:'Message from cloud',
            body:'This is a test body',
            badge:'1',
            sound:'default'
        }
    };
    console.log("payload created!");

    //send notification using FCM
    return admin.database().ref('Notifications').once('value').then((snapshot) => {
        snapshot.forEach((childSnapshot) => {
            var ss = childSnapshot.val();
            var token = ss.Token;
            console.log('Token is: ' + token);
            admin.messaging().sendToDevice(token, payload);
            console.log("Notification sent!");
        });
    });

});