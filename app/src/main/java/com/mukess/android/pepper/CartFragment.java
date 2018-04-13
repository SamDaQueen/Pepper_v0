package com.mukess.android.pepper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.mukess.android.pepper.MainActivity.finalArrayList;

public class CartFragment extends Fragment {

    public static boolean checker = false;
    public static float total = 0;
    static ArrayList<MenuItem> ordered = new ArrayList<>(20);
    TextView cartTotal;
    Button clearCart, proceedButton, backToMenu;
    ArrayList<MenuItem> finalCartItems;
    myDBHandler dbHandler;
    MenuAdapter menuAdapter;
    ScrollView scrollView;
    LinearLayout bottomBar;
    ListView listView;
    TextView textView;
    ImageView empty;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cart, container, false);
        backToMenu = rootView.findViewById(R.id.backToMenuButton);
        backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        cartTotal = rootView.findViewById(R.id.total);
        updateTotal();
        listView = rootView.findViewById(R.id.cartitemView);

        Bundle args = getArguments();
        scrollView = rootView.findViewById(R.id.empty);
        bottomBar = rootView.findViewById(R.id.bottom);
        if (args == null) {
            scrollView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
            bottomBar.setVisibility(View.INVISIBLE);
        } else {
            scrollView.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
        }
        //LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter("Search"));
        finalCartItems = new ArrayList<>(20);
        if (checker) {
            if (args != null)
                if (args.getParcelableArrayList("final_order") != null)
                    finalCartItems = args.getParcelableArrayList("final_order");
            assert finalCartItems != null;
            for (MenuItem menuItem : finalCartItems)   //a foreach loop
                if (menuItem.getQuantity() != 0)
                    ordered.add(menuItem);
            checker = false;
            finalArrayList.clear();
        }
        menuAdapter = new MenuAdapter(getActivity(), R.layout.item_menu, ordered);
        listView.setAdapter(menuAdapter);

        clearCart = rootView.findViewById(R.id.clearCart);
        clearCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuAdapter.clear();
                scrollView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
                bottomBar.setVisibility(View.INVISIBLE);
                total = 0;
            }
        });
        proceedButton = rootView.findViewById(R.id.proceed);
        textView = rootView.findViewById(R.id.lonely);
        empty = rootView.findViewById(R.id.imageEmpty);
        dbHandler = new myDBHandler(getContext(), null, null, 1);
        proceedOrder();
        return rootView;
    }

    private void updateTotal() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                        if (getActivity() == null)
                            return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String cart = " Total: " + getResources().getString(R.string.rs) + total;
                                cartTotal.setText(cart);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    private void proceedOrder() {
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Proceed to counter for cash payment. Your order will be saved in history. Are you sure you want to continue?");
                alertDialogBuilder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        total = 0;
                        for (MenuItem menuItem : ordered)   //a foreach loop
                            if (menuItem.getQuantity() != 0)
                                dbHandler.addProduct(menuItem);
                        menuAdapter.clear();
                        dbHandler.databaseToString();
                    }
                });
                alertDialogBuilder.setNegativeButton("No, I changed my mind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialogBuilder.show();
            }
        });

    }
}