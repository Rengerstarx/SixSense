package com.hestia.sixthsense.ui.routing.detail;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hestia.sixthsense.R;
import com.hestia.sixthsense.data.network.model.beacon.GraphResponse;
import com.hestia.sixthsense.ui.routing.detail.RoutingDetailLocations.OnLocationFragmentInteractionListener;

import java.util.List;

/**
 * Адапатер для списка доступных локаций. Локации приходят сюда либо из кеша либо из сети
 *
 * {@link RecyclerView.Adapter} который может отображать {@link DummyItem} и вызывать
 * указанный {@link OnLocationFragmentInteractionListener}.
 */
public class LocationRecyclerViewAdapter extends RecyclerView.Adapter<LocationRecyclerViewAdapter.ViewHolder> {

    private final List<GraphResponse> mValues;
    private final OnLocationFragmentInteractionListener mListener;
    private Context context;
    public LocationRecyclerViewAdapter(List<GraphResponse> items, RoutingDetailLocations.OnLocationFragmentInteractionListener listener, Context activityContext) {
        mValues = items;
        mListener = listener;
        context = activityContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        //holder.mIdView.setText(mValues.get(position).getId());
        holder.mContentView.setText(mValues.get(position).getName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public GraphResponse mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            mIdView.setTextSize(context.getResources().getDimension(R.dimen.routing_detail_text_size));
            mContentView.setTextSize(context.getResources().getDimension(R.dimen.routing_detail_text_size));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
