package com.shakiemsaunders.nytimessearch.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class ErrorParser {

    public static String parseError(JSONObject jsonObject){
        String errorMsg;
        try {
            errorMsg = jsonObject.getString(AppConstants.MESSAGE_KEY);

        } catch (JSONException e) {
            e.printStackTrace();
            return "I'm sorry, but I couldnt parse your message this time :(";
        }
        return errorMsg;
    }
}
