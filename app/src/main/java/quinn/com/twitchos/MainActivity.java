package quinn.com.twitchos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView err;
    ProgressBar l;
    GridView gv;
    List<String> gameName = new ArrayList<>();
    String DUMMYNAME = "FusionCS";
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region Defines
        err = (TextView)findViewById(R.id.error);
        l = (ProgressBar)findViewById(R.id.loading);
        gv = (GridView)findViewById(R.id.gridView);
        //endregion
        //region NavDrawer
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Games");




        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new NavDrawerAdapter(TITLES, ICONS, DUMMYNAME, PROFILE);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
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
                switch(position) {
                    case 1:
                        Intent following = new Intent();
                        MainActivity.this.startActivity(following);
                        break;
                    case 2:
                        //Intent games = new Intent();
                        //MainActivity.this.startActivity(games);
                        break;
                    case 3:
                        Intent channels = new Intent(MainActivity.this,ChannelsActivity.class);
                        MainActivity.this.startActivity(channels);
                        break;
                    case 4:
                        Intent videos = new Intent();
                        MainActivity.this.startActivity(videos);
                        break;
                }
            }
        });
        //endregion
        if(checkNetworkState(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            new GetCategories().execute("https://api.twitch.tv/kraken/games/top?limit=100");
        } else {
            l.setVisibility(View.GONE);
            err.setVisibility(View.VISIBLE);
        }

        gv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent streamsActivity = new Intent(MainActivity.this, StreamsActivity.class);
                        streamsActivity.putExtra("game", gameName.get(position));
                        MainActivity.this.startActivity(streamsActivity);
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

    class GetCategories extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... api_url) {
            try {
                String url = api_url[0];
                List<String> games = new ArrayList<>();
                JSONObject mainReq = new JSONObject(readUrl(url));
                JSONArray topGames = mainReq.getJSONArray("top");
                for(int i = 0, n = topGames.length(); i < n; i++) {
                    JSONObject obj = topGames.getJSONObject(i);
                    JSONObject game = obj.getJSONObject("game");
                    String gName = game.getString("name");
                    JSONObject logos = game.getJSONObject("box");
                    String medium = logos.getString("large");
                    games.add(medium);
                    gameName.add(gName);
                }
                final GamesAdapter ga = new GamesAdapter(MainActivity.this,games);
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                gv.setAdapter(ga);
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                l.setVisibility(View.GONE);
                            }
                        }
                );
            } catch (final Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                l.setVisibility(View.GONE);
                                err.setVisibility(View.VISIBLE);
                            }
                        }
                );
            }
            return null;
        }
    }
}
