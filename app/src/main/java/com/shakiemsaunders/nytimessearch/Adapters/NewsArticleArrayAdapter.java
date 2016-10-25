package com.shakiemsaunders.nytimessearch.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shakiemsaunders.nytimessearch.Models.NewsArticle;
import com.shakiemsaunders.nytimessearch.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by shakiem on 10/23/16.
 */
public class NewsArticleArrayAdapter extends ArrayAdapter<NewsArticle> {
    final float scale = getContext().getResources().getDisplayMetrics().density;
    public NewsArticleArrayAdapter(Context context, List<NewsArticle> articles){
        super(context, android.R.layout.simple_list_item_1, articles);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NewsArticle article = getItem(position);
        NewsArticleViewHolder articleHolder;

        if(convertView==null){
            articleHolder = new NewsArticleViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_news_article, parent, false);
            articleHolder.headlineTextView = (TextView)convertView.findViewById(R.id.headlineTextView);
            articleHolder.thumbNailImageView = (ImageView)convertView.findViewById(R.id.thumbNailImageView);
            convertView.setTag(articleHolder);
        }
        else{
            articleHolder = (NewsArticleViewHolder)convertView.getTag();
        }

        articleHolder.headlineTextView.setText(article.getHeadline());
        String thumbNail = article.getThumbNail();
        if(!thumbNail.isEmpty()){
            int dimension = (int)(75 * scale + 0.5f);
            Picasso.with(getContext()).load(thumbNail)
                    .placeholder(R.drawable.news_article)
                    .resize(dimension, dimension).centerCrop()
                    .error(R.drawable.image_not_found)
                    .into(articleHolder.thumbNailImageView);
        }
        else{
            articleHolder.thumbNailImageView.setImageResource(R.drawable.news_article);
        }


        return convertView;
    }

    private class NewsArticleViewHolder{
        ImageView thumbNailImageView;
        TextView headlineTextView;
    }
}
