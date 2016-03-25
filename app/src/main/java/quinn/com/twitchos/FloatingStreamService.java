package quinn.com.twitchos;

/**
 * Created by sdl on 3/25/2016.
 */
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class FloatingStreamService extends Service {

    private WindowManager windowManager;
    private VideoView streamView;
    String userLink;
    long lastPressTime;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
   }

    public int onStartCommand (Intent intent, int flags, int startId){

        String link = intent.getStringExtra("link");
        startVideo(link);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        streamView = new VideoView(this);

        //startVideo("http://usher.twitch.tv/api/channel/hls/tarik_tv.m3u8?player=twitchweb&&token={%22user_id%22:95826332,%22channel%22:%22tarik_tv%22,%22expires%22:1458895666,%22chansub%22:{%22view_until%22:1924905600,%22restricted_bitrates%22:[]},%22private%22:{%22allowed_to_view%22:true},%22privileged%22:false,%22source_restricted%22:false}&sig=a536aadf09c1b07b89e4618cae3e4a0e693bd9ec&allow_audio_only=true&allow_source=true&type=any&p=125456%27");

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager.addView(streamView, params);

        try {
            streamView.setOnTouchListener(new View.OnTouchListener() {
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:


                            //Close view on double click!
                            long pressTime = System.currentTimeMillis();
                            if (pressTime - lastPressTime <= 300) {
                                FloatingStreamService.this.stopSelf();
                            }
                            lastPressTime = pressTime;



                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(streamView, params);
                            return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void startVideo(final String video_link) {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(streamView);

        mediaController.setMediaPlayer(streamView);
        streamView.setMediaController(mediaController);
        streamView.setVideoURI(Uri.parse(video_link));
        streamView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer media) {
                media.start();
            }
        });

        streamView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer media, int what, int extra) {
                if (what == 100) {
                    streamView.stopPlayback();
                    startVideo(video_link);
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (streamView != null) windowManager.removeView(streamView);
    }

}
