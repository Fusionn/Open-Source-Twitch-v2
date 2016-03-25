package quinn.com.twitchos;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * Created by sdk on 10/8/2015.
 */
public class StreamViewActivity extends AppCompatActivity {

    private TextView aTitle;
    VideoView vw;

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
    Button b;
    String steam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streamview);

        Intent i = getIntent();
        steam = i.getStringExtra("streamLink");
        String name = i.getStringExtra("streamName");
        String game = i.getStringExtra("streamGame");
        System.out.println(steam);
        vw = (VideoView) findViewById(R.id.videoView);
        b = (Button) findViewById(R.id.openFloater);
        startVideo(steam);

        b.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent floater = new Intent(StreamViewActivity.this, FloatingStreamService.class);
                        floater.putExtra("link", steam);
                        StreamViewActivity.this.startService(floater);
                        vw.stopPlayback();
                    }
                }
        );

        //region NavDrawer
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else {
            toolbar = (Toolbar) findViewById(R.id.tool_bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(name);
            getSupportActionBar().setSubtitle("Playing " + game);


            mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

            mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

            mAdapter = new NavDrawerAdapter(TITLES, ICONS, DUMMYNAME, PROFILE);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
            // And passing the titles,icons,header view name, header view email,
            // and header view profile picture

            mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

            mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

            mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


            Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
            mDrawerToggle = new ActionBarDrawerToggle(this, Drawer, toolbar, R.string.open, R.string.close) {

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
                            StreamViewActivity.this.startActivity(following);
                            break;
                        case 2:
                            Intent games = new Intent(StreamViewActivity.this, MainActivity.class);
                            StreamViewActivity.this.startActivity(games);
                            break;
                        case 3:
                            Intent channels = new Intent(StreamViewActivity.this, ChannelsActivity.class);
                            StreamViewActivity.this.startActivity(channels);
                            break;
                        case 4:
                            Intent videos = new Intent();
                            StreamViewActivity.this.startActivity(videos);
                            break;
                    }
                }
            });
        }
        //endregion
    }

    private void startVideo(final String video_link) {
        vw.setMediaController(new MediaController(this));
        vw.setVideoURI(Uri.parse(video_link));
        vw.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer media) {
                media.start();
            }
        });

        vw.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer media, int what, int extra) {
                if (what == 100) {
                    vw.stopPlayback();
                    startVideo(video_link);
                }
                return true;
            }
        });
    }

    private boolean checkNetworkState(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
