package quinn.com.twitchos;

/**
 * Created by sdl on 1/25/2016.
 */
public class ChannelList {
    public String one;
    public String two;
    public String three;
    public String four;

    public ChannelList(String o, String t, String th, String f) {
        this.one = o;
        this.two = t;
        this.three = th;
        this.four = f;
    }

    public String get(int pos) {
        switch(pos){
            case 1:
                return one;
            case 2:
                return two;
            case 3:
                return three;
            default:
                return "error";
        }
    }
}
