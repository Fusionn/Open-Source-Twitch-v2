package quinn.com.twitchos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by sdl on 1/25/2016.
 */
public class StreamAdapter extends ArrayAdapter<MultiList> {
    private final Context context;
    private final List<MultiList> values;

    public StreamAdapter(Context context, List<MultiList> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.streamsrow, parent, false);
        TextView username = (TextView)rowView.findViewById(R.id.username);
        TextView viewers = (TextView)rowView.findViewById(R.id.views);
        ImageView preview = (ImageView)rowView.findViewById(R.id.preview);
        username.setText(values.get(position).two);
        viewers.setText(values.get(position).three);
        Picasso.with(context).load(values.get(position).one).into(preview);

        return rowView;
    }
}
