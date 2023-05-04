package com.hestia.sixthsense.ui.routing.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hestia.sixthsense.R;
import com.hestia.sixthsense.data.network.model.beacon.NodeResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Адапатер для списка доступных точек куда можно прийти
 */
public class NodeRecyclerViewAdapter extends RecyclerView.Adapter<NodeRecyclerViewAdapter.ViewHolder> {

    private final List<NodeResponse> mValues = new ArrayList<>();
    private final RoutingDetailNodes.OnNodeFragmentInteractionListener mListener;
    private Context context;

    public NodeRecyclerViewAdapter(List<NodeResponse> items, RoutingDetailNodes.OnNodeFragmentInteractionListener listener, Context activityContext) {
        context = activityContext;
        // Filter all not destinctable, and leave only destinctable nodes
        for(NodeResponse node: items){
            if(node.isDestination())
                mValues.add(node);
        }
        mListener = listener;
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
        public NodeResponse mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mContentView = view.findViewById(R.id.content);
            mIdView.setTextSize(context.getResources().getDimension(R.dimen.routing_detail_destination_text_size));
            mContentView.setTextSize(context.getResources().getDimension(R.dimen.routing_detail_destination_text_size));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
