package com.hestia.sixthsense.ui.routing.master;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hestia.sixthsense.R;

/**
 * Является Master'ром. Работает с сетью, кешем и управляет detail'ами
 * Чтобы узнать подробнее, почитайте о паттерне Master-Detail
 *
 * Простой {@link Fragment} подкласс.
 * Activities, которые содержат этот фрагмент, ДОЛЖНЫ реализовать
 * {@link RoutingMaster.OnFragmentInteractionListener} интерфейс
 * для обработки событий взаимодействия.
 * Используйте фабричный метод {@link RoutingMaster#newInstance}, чтобы создать экземпляр этого фрагмента.
 */
public class RoutingMaster extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String ARG_PARAM1 = "location";
    private static final String ARG_PARAM2 = "destination";

    // TODO: Rename and change types of parameters
    private String location;
    private String destination;

    private OnFragmentInteractionListener mListener;
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onButtonPressed(v.getId());
        }
    };

    public RoutingMaster() {
        // Required empty public constructor
    }

    /**
     * Используйте этот фабричный метод для создания нового экземпляра этого фрагмента
     * с использованием предоставленных параметров.
     *
     * @param location Parameter 1.
     * @param destination   Parameter 2.
     * @return Новый экземпляр фрагмента RoutingMaster.
     */
    // TODO: Rename and change types and number of parameters
    public static RoutingMaster newInstance(String location, String destination) {
        RoutingMaster fragment = new RoutingMaster();
        Bundle args = new Bundle();

        args.putString(ARG_PARAM1, location);
        args.putString(ARG_PARAM2, destination);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            location = getArguments().getString(ARG_PARAM1);
            destination = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_routing_master, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup viewGroup = view.findViewById(R.id.linear_layout_routing_master);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            TextView child = (TextView) viewGroup.getChildAt(i);
            child.setOnClickListener(mClickListener);
            switch (child.getId()) {
                case R.id.find_route_building_selector:
                    if (location.length() > 0) child.setText(location);
                    break;
                case R.id.find_route_campus_selector:
                    if(location.length() == 0) child.setEnabled(false);
                    if (destination.length() > 0) child.setText(destination);
                    break;
            }
        }
        Button applyButton = view.findViewById(R.id.find_route_find_route_button);
        applyButton.setOnClickListener(mClickListener);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(int viewId) {
        if (mListener != null) {
            mListener.onFragmentInteraction(viewId);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Этот интерфейс должен быть реализован activities, содержащими этот фрагмент,
     * чтобы взаимодействие в этом фрагменте могло передаваться activity и, возможно,
     * другим фрагментам, содержащимся в этом activity.
     *
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int viewId);
    }
}
