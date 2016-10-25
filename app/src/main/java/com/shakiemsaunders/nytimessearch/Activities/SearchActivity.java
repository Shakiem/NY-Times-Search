package com.shakiemsaunders.nytimessearch.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.shakiemsaunders.nytimessearch.Adapters.NewsArticleArrayAdapter;
import com.shakiemsaunders.nytimessearch.Listeners.EndlessScrollListener;
import com.shakiemsaunders.nytimessearch.Models.NewsArticle;
import com.shakiemsaunders.nytimessearch.R;
import com.shakiemsaunders.nytimessearch.Utils.AppConstants;
import com.shakiemsaunders.nytimessearch.Utils.ErrorParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class SearchActivity extends AppCompatActivity {

    private GridView resultsGridView;
    private List<NewsArticle> articles;
    private NewsArticleArrayAdapter adapter;
    private AsyncHttpClient client = null;
    private final String url = AppConstants.BASE_SEARCH_URL;
    private RequestParams params;
    SharedPreferences sharedPreferences;
    private boolean isFilterSettingsSet;
    private String beginDate;
    private String endDate;
    private int sortOrder;
    private boolean isArtsChecked;
    private boolean isFnsChecked;
    private boolean isSportsChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar)findViewById(R.id.articleSearchToolbar);
        setSupportActionBar(toolbar);

        instantiateWidgetsAndVars();
        getFilterSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // perform query here
                searchForArticles(query);

                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()){
            case R.id.action_filter:
                showFilterDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void instantiateWidgetsAndVars() {
        sharedPreferences = getSharedPreferences(AppConstants.SEARCH_PREFS, MODE_PRIVATE);
        resultsGridView = (GridView) findViewById(R.id.resultsGridView);
        articles = new ArrayList<>();
        adapter = new NewsArticleArrayAdapter(this, articles);
        resultsGridView.setAdapter(adapter);

        resultsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DetailedArticleActivity.class);
                NewsArticle article = articles.get(position);

                intent.putExtra(AppConstants.ARTICLE_KEY, Parcels.wrap(article));
                startActivity(intent);
            }
        });

        resultsGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                continueSearch(page);
                return true;
            }
        });
    }

    private void getFilterSettings() {
        isFilterSettingsSet = sharedPreferences.getBoolean(AppConstants.FILTER_SET, false);
        beginDate =  sharedPreferences.getString(AppConstants.BEGIN_DATE, "");
        endDate = sharedPreferences.getString(AppConstants.END_DATE, "");
        sortOrder = sharedPreferences.getInt(AppConstants.SORT, -1);
        isArtsChecked = sharedPreferences.getBoolean(AppConstants.ARTS_CHECKED, false);
        isFnsChecked = sharedPreferences.getBoolean(AppConstants.FNS_CHECKED, false);
        isSportsChecked = sharedPreferences.getBoolean(AppConstants.SPORTS_CHECKED, false);
    }

    private void searchForArticles(String searchQuery) {
        adapter.clear();
        client = new AsyncHttpClient();

        params = new RequestParams();

        if(isFilterSettingsSet){
            if(isSportsChecked || isArtsChecked || isFnsChecked){
                String filterQuery = AppConstants.NEWS_DESK + ":(";
                filterQuery += isSportsChecked ? "\""+ AppConstants.SPORTS + "\" " : "";
                filterQuery += isFnsChecked ? "\""+ AppConstants.FNS + "\" " : "";
                filterQuery += isArtsChecked ? "\""+ AppConstants.ARTS + "\" " : "";
                filterQuery += ")";
                params.put(AppConstants.REQ_PARAM_FILTER_QUERY, filterQuery);
            }

            String sort = (sortOrder == 1) ? AppConstants.NEWEST : AppConstants.OLDEST;
            params.put(AppConstants.REQ_PARAM_SORT, sort);

            if(!beginDate.isEmpty())
                params.put(AppConstants.REQ_BEGIN_DATE, beginDate);

            if(!endDate.isEmpty())
                params.put(AppConstants.REQ_END_DATE, endDate);

        }

        params.put(AppConstants.REQ_PARAM_QUERY, searchQuery);
        params.put(AppConstants.REQ_PARAM_API_KEY, AppConstants.API_KEY);
        params.put(AppConstants.REQ_PARAM_PAGE, 0);

        performSearch();
    }

    private void performSearch() {
        client.get(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                Log.e("ERROR", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), ErrorParser.parseError(errorResponse), Toast.LENGTH_LONG).show();
                Log.e("ERROR", errorResponse.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray jsonResults = null;

                try{
                    jsonResults = response.getJSONObject(AppConstants.RESPONSE_KEY).getJSONArray(AppConstants.DOCS_KEY);
                    articles.addAll(NewsArticle.generateList(jsonResults));
                    adapter.notifyDataSetChanged();
                    Log.d("DEBUG", articles.toString());
                    Log.d("DEBUG", articles.toString());
                }catch(JSONException e ){
                    e.printStackTrace();
                }
            }
        });
    }

    private void continueSearch(int page){
        params.put(AppConstants.REQ_PARAM_PAGE, page);
        performSearch();
    }

    private void showFilterDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter_settings);

        Button saveButton = (Button) dialog.findViewById(R.id.saveButton);
        final EditText beginDateEditText = (EditText) dialog.findViewById(R.id.beginDateEditTxt);
        final EditText endDateEditText = (EditText) dialog.findViewById(R.id.endDateEditTxt);
        ImageView beginDateHelperBttn = (ImageView) dialog.findViewById(R.id.beginDateHelperBttn);
        ImageView endDateHelperBttn = (ImageView) dialog.findViewById(R.id.endDateHelperBttn);
        final LinearLayout beginDatePickerLayout = (LinearLayout) dialog.findViewById(R.id.beginDatePickerLayout);
        final LinearLayout endDatePickerLayout = (LinearLayout) dialog.findViewById(R.id.endDatePickerLayout);
        final DatePicker beginDatePicker = (DatePicker) dialog.findViewById(R.id.beginDatePicker);
        final DatePicker endDatePicker = (DatePicker) dialog.findViewById(R.id.endDatePicker);
        Button beginDateOkayBttn = (Button) dialog.findViewById(R.id.beginDateOkayBttn);
        Button endDateOkayBttn = (Button) dialog.findViewById(R.id.endDateOkayBttn);
        final Spinner sortSpinner = (Spinner) dialog.findViewById(R.id.sortSpinner);
        final CheckBox artsCheckBox = (CheckBox) dialog.findViewById(R.id.artsCheckbox);
        final CheckBox fnsCheckBox = (CheckBox) dialog.findViewById(R.id.fnsCheckBox);
        final CheckBox sportsCheckBox = (CheckBox) dialog.findViewById(R.id.sportsCheckBox);

        if(isFilterSettingsSet){
            beginDateEditText.setText(beginDate);
            endDateEditText.setText(endDate);
            sortSpinner.setSelection(sortOrder);
            artsCheckBox.setChecked(isArtsChecked);
            fnsCheckBox.setChecked(isFnsChecked);
            sportsCheckBox.setChecked(isSportsChecked);
        }

        beginDateHelperBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginDatePickerLayout.setVisibility(View.VISIBLE);
            }
        });

        endDateHelperBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDatePickerLayout.setVisibility(View.VISIBLE);
            }
        });

        beginDateOkayBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyMMdd");
                int year = beginDatePicker.getYear();
                int month = beginDatePicker.getMonth();
                int day = beginDatePicker.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                String dateString = formatter.format(calendar.getTimeInMillis());
                beginDateEditText.setText(dateString);
                beginDatePickerLayout.setVisibility(View.GONE);
            }
        });

        endDateOkayBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                int year = endDatePicker.getYear();
                int month = endDatePicker.getMonth();
                int day = endDatePicker.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                String dateString = formatter.format(calendar.getTimeInMillis());
                endDateEditText.setText(dateString);
                endDatePickerLayout.setVisibility(View.GONE);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                beginDate = beginDateEditText.getText().toString();
                editor.putString(AppConstants.BEGIN_DATE, beginDate);

                endDate = endDateEditText.getText().toString();
                editor.putString(AppConstants.END_DATE, endDate);

                sortOrder = sortSpinner.getSelectedItemPosition();
                editor.putInt(AppConstants.SORT, sortOrder);

                isArtsChecked = artsCheckBox.isChecked();
                editor.putBoolean(AppConstants.ARTS_CHECKED, isArtsChecked);

                isFnsChecked = fnsCheckBox.isChecked();
                editor.putBoolean(AppConstants.FNS_CHECKED, isFnsChecked);

                isSportsChecked = sportsCheckBox.isChecked();
                editor.putBoolean(AppConstants.SPORTS_CHECKED, isSportsChecked);

                isFilterSettingsSet = true;
                editor.putBoolean(AppConstants.FILTER_SET, isFilterSettingsSet);
                editor.commit();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
