/* eslint-disable promise/no-nesting */
/* eslint-disable promise/always-return */
const functions = require('firebase-functions');
const admin = require('firebase-admin');
// const firebase = require('firebase');
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
            title:'Let\'s Meet',
            body:'Members in group are near by, want to meet them?',
            badge:'1',
            sound:'default'
        }
    };
    console.log("payload created!");

    //send notification using FCM
    return admin.database().ref('Notifications').once('value').then((snapshot) => {
        snapshot.forEach((childSnapshot) => {
            var ss = childSnapshot.val();
            var sent = "";
            sent = ss.Sent;
            console.log("the sent status is: " + sent);
            if(sent !== null && sent.localeCompare("false") === 0){
                var token = ss.Token;
                console.log('Token is: ' + token);
                
                console.log("Notification sent!");
                //update sent field to true
                return childSnapshot.ref.update({Sent:'true'}).then(()=>{
                    admin.messaging().sendToDevice(token, payload);
                });
            }
            
        });
    });

});