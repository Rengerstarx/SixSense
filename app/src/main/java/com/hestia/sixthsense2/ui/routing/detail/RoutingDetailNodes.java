package com.hestia.sixthsense2.ui.routing.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hestia.sixthsense2.R;
import com.hestia.sixthsense2.data.network.model.beacon.NodeResponse;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Фрагмент который показывает доступные точки куда можно прийти (Является Detail)
 */
public class RoutingDetailNodes extends Fragment {
    private static String ARG_PARAM1 = "node-list";
    private List<NodeResponse> nodes;
    private OnNodeFragmentInteractionListener mListener;
    public RoutingDetailNodes() {
    }

    public static RoutingDetailNodes newInstance(List<NodeResponse> nodes) {
        RoutingDetailNodes fragment = new RoutingDetailNodes();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, new Gson().toJson(nodes));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Type nodeListType = new TypeToken<List<NodeResponse>>() {}.getType();
            nodes = new Gson().fromJson(getArguments().getString(ARG_PARAM1), nodeListType);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new com.hestia.sixthsense2.ui.routing.detail.NodeRecyclerViewAdapter(nodes, mListener, getContext()));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNodeFragmentInteractionListener) {
            mListener = (OnNodeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLocationFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNodeFragmentInteractionListener {
        void onListFragmentInteraction(NodeResponse item);
    }
}
