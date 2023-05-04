package com.hestia.sixthsense.ui.routing.detail;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hestia.sixthsense.R;
import com.hestia.sixthsense.data.network.AppApiHelper;
import com.hestia.sixthsense.data.network.LocationCacheHelper;
import com.hestia.sixthsense.data.network.model.beacon.GraphResponse;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Фрагмент, который показывает доступные локации		(Является Detail)
 * <p/>
 * Activities, содержащие этот фрагмент, ДОЛЖНЫ реализовать {@link OnLocationFragmentInteractionListener}
 * интерфейс.
 */
public class RoutingDetailLocations extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private OnLocationFragmentInteractionListener mListener;
    private Context mContext;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RoutingDetailLocations() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RoutingDetailLocations newInstance(int columnCount) {
        RoutingDetailLocations fragment = new RoutingDetailLocations();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            if (LocationCacheHelper.isNetAvailable(context)) {
                AppApiHelper.getLocations()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<List<GraphResponse>>() {
                            @Override
                            public void onNext(@NonNull List<GraphResponse> graphResponse) {
                                new LocationCacheHelper(mContext).saveLastCache(graphResponse);
                                recyclerView.setAdapter(new LocationRecyclerViewAdapter(graphResponse, mListener,getContext()));
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                 LoadFromCache(recyclerView);

                            }
                            @Override
                            public void onComplete() {}
                        });
            } else {
                LoadFromCache(recyclerView);
            }

        }
        return view;
    }

    private void LoadFromCache(RecyclerView recyclerView) {
        List<GraphResponse> cachedLocations = new LocationCacheHelper(mContext).getLastCache();
        if (cachedLocations.size() == 0) {
            Toast.makeText(getActivity(), "В хранилище пусто. Включите интернет и попробуйте снова", Toast.LENGTH_LONG).show();
        }else{
            recyclerView.setAdapter(new LocationRecyclerViewAdapter(cachedLocations, mListener,getContext()));
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnLocationFragmentInteractionListener) {
            mListener = (OnLocationFragmentInteractionListener) context;
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
    public interface OnLocationFragmentInteractionListener {
        void onListFragmentInteraction(GraphResponse item);
    }
}
