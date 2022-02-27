package wrteam.multivendor.seller.helper;


import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import wrteam.multivendor.seller.activity.OrderDetailActivity;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    TextToSpeech textToSpeech;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                Log.e(TAG, String.valueOf(json));
                sendPushNotification(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void sendPushNotification(JSONObject json) {
        try {

            JSONObject data = json.getJSONObject(Constant.DATA);

            String type = data.getString("type");
            String title = data.getString("title");
            String message = data.getString("message");
            String imageUrl = data.getString("image");
            String id = data.getString("id");

            Intent intent = new Intent(getApplicationContext(), OrderDetailActivity.class);

            intent.putExtra(Constant.ID, id);
            intent.putExtra(Constant.FROM, type);

            textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
                if (status != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.speak(title, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });

            MyNotificationManager mNotificationManager = new MyNotificationManager(getApplicationContext());

            if (imageUrl == null || imageUrl.equals("null") || imageUrl.equals("")) {
                mNotificationManager.showSmallNotification(title, message, intent);
            } else {
                mNotificationManager.showBigNotification(title, message, imageUrl, intent);
            }


        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        AppController.getInstance().setDeviceToken(s);
    }

}
