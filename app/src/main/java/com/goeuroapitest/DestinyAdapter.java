package com.goeuroapitest;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Lucas on 26/08/2015.
 */
public class DestinyAdapter extends BaseAdapter implements Filterable, Response.Listener<String>, Response.ErrorListener {
    private static RequestQueue queue;
    private final String locale;
    private Context mContext;
    private ArrayList<Destiny> mDestinies;
    private Filter filter;
    private Location userLocation;

    public DestinyAdapter(Context context, ArrayList<Destiny> destinies) {
        mContext = context;
        mDestinies = destinies;
        locale = context.getResources().getConfiguration().locale.getCountry();
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        return mDestinies.size();
    }

    @Override
    public String getItem(int position) {
        return mDestinies.get(position).getFullName();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.destiny_row, parent, false);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.destiny_row_icon);
            holder.title = (TextView) convertView.findViewById(R.id.destiny_row_title);
            holder.country = (TextView) convertView.findViewById(R.id.destiny_row_country);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Destiny destiny = mDestinies.get(position);
        if (destiny.getIata_airport_code() == null || destiny.getIata_airport_code().isEmpty()) {
            holder.icon.setImageResource(R.drawable.ic_pin_location);
        } else {
            holder.icon.setImageResource(R.drawable.ic_plane);
        }
        Drawable drawable = holder.icon.getDrawable();
        drawable.mutate();
        drawable.setColorFilter(getContext().getResources().getColor(R.color.primary_blue),
                PorterDuff.Mode.SRC_ATOP);
        holder.title.setText(destiny.getName());
        holder.country.setText(destiny.getCountry());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    createGetRequestFromUrl(constraint, DestinyAdapter.this, DestinyAdapter.this);
                    return null;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                }
            };
        }
        return filter;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    private void createGetRequestFromUrl(CharSequence term, Response.Listener<String> stringListener, Response.ErrorListener errorListener) {
        if (queue == null) {
            queue = Volley.newRequestQueue(getContext());
        } else {
            queue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }

        String url = String.format("http://api.goeuro.com/api/v2/position/suggest/%s/%s", locale, term);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                stringListener, errorListener);
        queue.add(stringRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        GsonBuilder gson = new GsonBuilder();
        mDestinies = new ArrayList<>();
        Collections.addAll(mDestinies, gson.create().fromJson(response, Destiny[].class));
        if (userLocation != null) {
            Collections.sort(mDestinies, new Comparator<Destiny>() {
                @Override
                public int compare(Destiny destiny1, Destiny destiny2) {
                    float distance1 = getDistance(destiny1);
                    float distance2 = getDistance(destiny2);

                    if (distance1 > distance2)
                        return 1;
                    else if (distance1 < distance2)
                        return -1;
                    return 0;
                }
            });
        }
        notifyDataSetChanged();
    }

    private float getDistance(Destiny destiny) {
        Location destLocation = new Location("tempLoc" + destiny.getId());
        destLocation.setLatitude(destiny.getGeo_position().getLatitude());
        destLocation.setLongitude(destiny.getGeo_position().getLongitude());
        return userLocation.distanceTo(destLocation);
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView country;
    }
}
