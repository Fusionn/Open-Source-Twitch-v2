package quinn.com.twitchos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdl on 1/25/2016.
 */
public class StreamsActivity extends AppCompatActivity {

    ListView lv;
    ProgressBar p;
    TextView t;
    List<MultiList> streamers = new ArrayList<>();
    String game,mu3Link,vidLink,gameWithSpaces;

    String DUMMYNAME = "sdk_cs";
    String TITLES[] = { "Following", "Games", "Channels", "Videos" };
    int ICONS[] = { R.drawable.ic_favorite_white_24dp,R.drawable.ic_games_white_24dp,R.drawable.ic_videocam_white_24dp, R.drawable.ic_video_library_white_24dp };
    int PROFILE = R.drawable.viewers;

    private Toolbar toolbar;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout Drawer;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_streams);
        //region Defines/Get Intent Extras
        Intent i = getIntent();
        gameWithSpaces = i.getStringExtra("game");
        game = gameWithSpaces.replaceAll("\\s+","%20");
        lv = (ListView)findViewById(R.id.listView);
        p = (ProgressBar)findViewById(R.id.loading);
        t = (TextView)findViewById(R.id.error);
        //endregion

        //region NavDrawer
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Streams");
        getSupportActionBar().setSubtitle(gameWithSpaces);




        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new NavDrawerAdapter(TITLES,ICONS,DUMMYNAME,PROFILE);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.open, R.string.close){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }



        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                switch (position) {
                    case 1:
                        Intent following = new Intent();
                        StreamsActivity.this.startActivity(following);
                        break;
                    case 2:
                        Intent games = new Intent(StreamsActivity.this, MainActivity.class);
                        StreamsActivity.this.startActivity(games);
                        break;
                    case 3:
                        Intent channels = new Intent(StreamsActivity.this,ChannelsActivity.class);
                        StreamsActivity.this.startActivity(channels);
                        break;
                    case 4:
                        Intent videos = new Intent();
                        StreamsActivity.this.startActivity(videos);
                        break;
                }
            }
        });
        //endregion

        if(checkNetworkState(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            new GetStreamers().execute("https://api.twitch.tv/kraken/streams?game=" + game + "&limit=100");
        } else {
            p.setVisibility(View.GONE);
            t.setVisibility(View.VISIBLE);
        }

        lv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final String unNORM = streamers.get(position).two;
                        final String un = unNORM.toLowerCase();
                        Thread t  = new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jobj = new JSONObject(readUrl("https://api.twitch.tv/api/channels/" + un + "/access_token/"));
                                            String token = URLEncoder.encode(jobj.getString("token"));
                                            String sig = jobj.getString("sig");
                                            String fUrl = "http://usher.twitch.tv/api/channel/hls/" + un + ".m3u8?player=twitchweb&token=" + token + "&sig=" + sig;
                                            Intent i = new Intent(StreamsActivity.this, StreamViewActivity.class);
                                            i.putExtra("streamLink", fUrl);
                                            i.putExtra("streamName", unNORM);
                                            i.putExtra("streamGame", gameWithSpaces);
                                            StreamsActivity.this.startActivity(i);
                                        } catch(Exception ex) {

                                        }
                                    }
                                }
                        );
                        t.start();
                    }
                }
        );
    }

    private boolean checkNetworkState(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    class GetStreamers extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... api_url) {
            try {
                String url = api_url[0];
                JSONObject jObj = new JSONObject(readUrl(url));
                JSONArray streamsLive = jObj.getJSONArray("streams");
                for(int i = 0, n = streamsLive.length(); i < n; i++) {
                    JSONObject stream = streamsLive.getJSONObject(i);
                    //Optional Maturity Check(I am going to ignore this for now 1/25/16
                    int viewers = stream.getInt("viewers");
                    JSONObject previews = stream.getJSONObject("preview");
                    String picture = previews.getString("large");
                    JSONObject channel = stream.getJSONObject("channel");
                    String name = channel.getString("display_name");
                    streamers.add(new MultiList(picture,name,Integer.toString(viewers)));
                }
                final StreamAdapter sa = new StreamAdapter(StreamsActivity.this, streamers);
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                lv.setAdapter(sa);
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                p.setVisibility(View.GONE);
                            }
                        }
                );
            } catch (final Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                p.setVisibility(View.GONE);
                                t.setVisibility(View.VISIBLE);
                            }
                        }
                );
            }
            return null;
        }
    }
}
