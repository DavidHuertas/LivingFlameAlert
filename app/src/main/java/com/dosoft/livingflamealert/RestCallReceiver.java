package com.dosoft.livingflamealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RestCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LFAlertApp myApplication = (LFAlertApp) context.getApplicationContext();

        // Perform the REST call in the background
        new RestCallTask(myApplication).execute();
    }

    private static class RestCallTask extends AsyncTask<Void, Void, String> {
        private final LFAlertApp myApplication;

        RestCallTask(LFAlertApp myApplication) {
            this.myApplication = myApplication;
        }

        @Override
        protected String doInBackground(Void... params) {
            // Replace this URL with your actual API endpoint
            String apiUrl = "https://worldofwarcraft.blizzard.com/graphql";

            // Replace this JSON body with your actual request body
            String jsonBody = "{\"operationName\":\"GetRealmStatusData\",\"variables\":{\"input\":{\"compoundRegionGameVersionSlug\":\"classic1x-eu\"}},\"extensions\":{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"b37e546366a58e211e922b8c96cd1ff74249f564a49029cc9737fef3300ff175\"}}}";

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : null;

                Log.d("RestCallReceiver", "HTTP Request successful. Response: " + responseBody);

                return responseBody;
            } catch (IOException e) {
                Log.e("RestCallReceiver", "Error in REST call", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(result);
                    JSONArray realmsArray = jsonResponse.getJSONObject("data").getJSONArray("Realms");

                    // Find Living Flame
                    JSONObject foundRealm = filterLivingFlame(realmsArray);

                    // Use UiUpdateListener reference to update UI
                    UiUpdateListener uiUpdateListener = myApplication.getUiUpdateListener();
                    if (uiUpdateListener != null) {
                        // Check realmLockStatus and send notification if null
                        JSONObject realmLockStatus = foundRealm.optJSONObject("realmLockStatus");
                        if (realmLockStatus == null) {
                            uiUpdateListener.updateUi("Realm is now unlocked!", android.R.color.holo_green_light);
                        }

                        // Update the UI with realm information
                        updateUI(foundRealm, uiUpdateListener);
                    }
                } catch (JSONException e) {
                    Log.e("RestCallReceiver", "Error parsing JSON", e);
                }
            }
        }

        private void updateUI(JSONObject realm, UiUpdateListener uiUpdateListener) throws JSONException {
            // Update the UI elements with realm information
            String realmInfo = "Name: " + realm.getString("name") +
                    "\nSlug: " + realm.getString("slug") +
                    "\nLocale: " + realm.getString("locale") +
                    "\nTimezone: " + realm.getString("timezone") +
                    "\nOnline: " + realm.getBoolean("online") +
                    "\nCategory: " + realm.getString("category") +
                    "\nType: " + realm.getJSONObject("type").getString("name") +
                    "\nPopulation: " + realm.getJSONObject("population").getString("name");

            // Update realm info TextView
            uiUpdateListener.updateUi(realmInfo, getColorForLockStatus(realm));
        }

        private int getColorForLockStatus(JSONObject realm) throws JSONException {
            boolean isLocked = realm.optJSONObject("realmLockStatus") != null;
            return isLocked ? android.R.color.holo_red_light : android.R.color.holo_green_light;
        }

        private JSONObject filterLivingFlame(JSONArray realmsArray) throws JSONException {
            for (int i = 0; i < realmsArray.length(); i++) {
                JSONObject realm = realmsArray.getJSONObject(i);
                String slug = realm.getString("slug");

                if ("living-flame".equals(slug)) {
                    return realm;
                }
            }
            return null;
        }
    }
}
