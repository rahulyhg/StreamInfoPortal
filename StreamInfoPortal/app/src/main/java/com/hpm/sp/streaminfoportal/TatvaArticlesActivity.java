package com.hpm.sp.streaminfoportal;

import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TatvaArticlesActivity extends AppCompatActivity {

    ArrayList<TatvaDataObject> articleList = new ArrayList<>();
    public static JSONObject jsonObject = new JSONObject();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    protected long downloadReference = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tatva_articles);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ArticleFetchHandler fetchArticles = new ArticleFetchHandler("http://hpmahesh.com/articlesList.php");
        fetchArticles.execute();
        System.out.println(jsonObject);
        mRecyclerView = (RecyclerView) findViewById(R.id.tatva_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        articleList.add(new TatvaDataObject("Please refresh the list", " ", " "));
        mAdapter = new TatvaRecyclerViewAdapter(articleList);
        mRecyclerView.setAdapter(mAdapter);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.refreshList);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                articleList.clear();
                mAdapter = new TatvaRecyclerViewAdapter(articleList);
                mRecyclerView.setAdapter(mAdapter);
                boolean connected = false;
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    connected = true;
                }
                else
                    connected = false;
                if(connected){
                    mAdapter = new TatvaRecyclerViewAdapter(refreshList());
                    mRecyclerView.setAdapter(mAdapter);
                }
                else
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
            }
        });
    }

    protected ArrayList<TatvaDataObject> refreshList(){
        try {
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                for(int i=0; i<jsonArray.length(); i++) {
                    TatvaDataObject dataObject = new TatvaDataObject((String) jsonArray.getJSONObject(i).get("name"), (String) jsonArray.getJSONObject(i).get("author"), (String) jsonArray.getJSONObject(i).get("location"));
                    articleList.add(dataObject);
                }
                return articleList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
    }

    protected void onResume() {
        super.onResume();
        ((TatvaRecyclerViewAdapter) mAdapter).setOnItemClickListener(new TatvaRecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Uri articleUri = Uri.parse("http://hpmahesh.com/articles/" + ((TextView) v.findViewById(R.id.artLoc)).getText().toString());
                System.out.println(articleUri.toString());
                if(!((TextView) v.findViewById(R.id.artLoc)).getText().toString().equalsIgnoreCase(" "))
                    downloadSong(articleUri, ((TextView) v.findViewById(R.id.artLoc)).getText().toString());
            }
        });
    }


    protected long downloadSong(Uri uri, String articleName){

        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setTitle(articleName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalFilesDir(TatvaArticlesActivity.this, Environment.DIRECTORY_DOWNLOADS,articleName);
        downloadReference = downloadManager.enqueue(request);

        Toast.makeText(getApplicationContext(), "Download started", Toast.LENGTH_SHORT).show();

        return downloadReference;
    }


}
