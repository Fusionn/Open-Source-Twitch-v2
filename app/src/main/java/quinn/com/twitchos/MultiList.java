package quinn.com.twitchos;

/**
 * Created by sdl on 1/25/2016.
 */
public class MultiList {
    public String one;
    public String two;
    public String three;

    public MultiList(String o, String t, String th) {
        this.one = o;
        this.two = t;
        this.three = th;
    }

    /*
    * <p><b>Pre:</b></p>  <Ul>True</Ul>
    * <p><b>Post:</b></p> <Ul>The x is pushed onto the top of the stack,
    *                       and the rest of the stack remains unchanged.</Ul>
    *
    * @param x              Indicates the current node
     */
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
