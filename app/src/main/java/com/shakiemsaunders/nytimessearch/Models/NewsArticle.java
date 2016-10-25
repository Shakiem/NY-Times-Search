package com.shakiemsaunders.nytimessearch.Models;

import com.shakiemsaunders.nytimessearch.Utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class NewsArticle {

     String webUrl;
     String headline;
     String thumbNail;

    public NewsArticle(){}

    public NewsArticle(JSONObject jsonObject){
        try {
            this.webUrl = jsonObject.getString(AppConstants.WEB_URL_KEY);
            this.headline = jsonObject.getJSONObject(AppConstants.HEADLINE_KEY).getString(AppConstants.MAIN_KEY);

            JSONArray multimediaArray = jsonObject.getJSONArray(AppConstants.MULTIMEDIA_KEY);
            if(multimediaArray.length() > 0){
                JSONObject multimedia = multimediaArray.getJSONObject(0);
                this.thumbNail = AppConstants.BASE_URL + multimedia.getString(AppConstants.URL_KEY);
            }
            else{
                this.thumbNail = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<NewsArticle> generateList(JSONArray jsonArray){
        List<NewsArticle> results = new ArrayList<>();
        for(int index = 0; index < jsonArray.length(); index++){
            try{
                results.add(new NewsArticle(jsonArray.getJSONObject(index)));
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return results;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbNail() {
        return thumbNail;
    }
}
