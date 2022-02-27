package wrteam.multivendor.seller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wrteam.multivendor.seller.R;
import wrteam.multivendor.seller.adapter.TrackerAdapter;
import wrteam.multivendor.seller.helper.ApiConfig;
import wrteam.multivendor.seller.helper.AppController;
import wrteam.multivendor.seller.helper.Constant;
import wrteam.multivendor.seller.helper.Session;
import wrteam.multivendor.seller.helper.VolleyCallback;
import wrteam.multivendor.seller.model.OrderTracker;

@SuppressWarnings("ALL")
public class MainActivity extends DrawerActivity {
    public static ArrayList<OrderTracker> orderTrackerArrayList;
    public static OrderTracker orderLists;
    @Nullable
    public Session session;
    boolean doubleBackToExitPressedOnce = false;
    TextView tvTitleWeeklySales;
    TextView tvWeeklySales;
    TextView tvOrdersCount;
    TextView tvProductsCount;
    static TextView tvBalance;
    TextView tvSoldOutCount;
    TextView tvLowStockCount;
    CardView lytOrders, lytProducts, lytCustomers, lytSoldOut, lytLowStock;
    RecyclerView recyclerView;
    Toolbar toolbar;
    Activity activity;
    SwipeRefreshLayout swipeRefresh;
    NestedScrollView scrollView;
    int total = 0;
    boolean isLoadMore = false;
    int offset = 0;
    //    SearchView searchview;
    LinearLayout lyt_order_detail, lyt_stock_detail;//;,lytSearchview;
    String filterBy;
    int filterIndex;
    CardView lytSales;
    TrackerAdapter trackerAdapter;
    private String query;
    ProgressBar progressBar;
    Menu menu;

    @SuppressWarnings("deprecation")
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            getLayoutInflater().inflate(R.layout.activity_main, frameLayout);
            toolbar = findViewById(R.id.toolbar);

            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            activity = MainActivity.this;
            session = new Session(activity);

            orderTrackerArrayList = new ArrayList<>();

            filterIndex = 0;

            lytOrders = findViewById(R.id.lytOrders);
            lytProducts = findViewById(R.id.lytProducts);
            lytCustomers = findViewById(R.id.lytCustomers);
            lytSoldOut = findViewById(R.id.lytSoldOut);
            lytLowStock = findViewById(R.id.lytLowStock);

            tvProductsCount = findViewById(R.id.tvProductsCount);
            tvOrdersCount = findViewById(R.id.tvOrdersCount);
            tvBalance = findViewById(R.id.tvBalance);
            tvSoldOutCount = findViewById(R.id.tvSoldOutCount);
            tvLowStockCount = findViewById(R.id.tvLowStockCount);
            swipeRefresh = findViewById(R.id.swipeRefresh);
            scrollView = findViewById(R.id.scrollView);
//        searchview = findViewById(R.id.searchview);
//        lytSearchview = findViewById(R.id.lytSearchview);
            lyt_stock_detail = findViewById(R.id.lyt_stock_detail);
            lyt_order_detail = findViewById(R.id.lyt_order_detail);
            tvTitleWeeklySales = findViewById(R.id.tvTitleWeeklySales);
            tvWeeklySales = findViewById(R.id.tvWeeklySales);
            lytSales = findViewById(R.id.lytSales);
            progressBar = findViewById(R.id.progressBar);

            recyclerView = findViewById(R.id.recyclerView);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(linearLayoutManager);

            getFinancialStatistics();

            lytOrders.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, OrderListActivity.class));
                }
            });

            lytProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, ProductListActivity.class).putExtra("from", "all_stock"));
                }
            });

            lytCustomers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, WalletTransactionsListActivity.class));
                }
            });

            lytSoldOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, ProductListActivity.class).putExtra("from", "out_stock"));
                }
            });

            lytLowStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, ProductListActivity.class).putExtra("from", "low_stock"));
                }
            });

            swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefresh.setRefreshing(false);
                    if (AppController.isConnected(activity)) {
                        offset = 0;
                        getFinancialStatistics();
                    }
                }
            });


            drawerToggle = new ActionBarDrawerToggle
                    (
                            this,
                            drawer, toolbar,
                            R.string.drawer_open,
                            R.string.drawer_close
                    ) {
            };

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            String token = task.getResult();
                            updateFCMId();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFCMId() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
        params.put(Constant.UPDATE_SELLER_FCM_ID, Constant.GetVal);
        params.put(Constant.FCM_ID, "" + AppController.getInstance().getDeviceToken());

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(boolean result, String response) {
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    public void getData() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
        params.put(Constant.GET_SELLER_BY_ID, Constant.GetVal);

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(response);
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            JSONObject jsonObject = jsonObject1.getJSONArray(Constant.DATA).getJSONObject(0);
                            new Session(activity).createUserLoginSession(
                                    jsonObject.getString(Constant.ID),
                                    jsonObject.getString(Constant.FCM_ID),
                                    jsonObject.getString(Constant.NAME),
                                    jsonObject.getString(Constant.STORE_NAME),
                                    jsonObject.getString(Constant.EMAIL),
                                    jsonObject.getString(Constant.PASSWORD),
                                    jsonObject.getString(Constant.BALANCE),
                                    jsonObject.getString(Constant.CUSTOMER_PRIVACY),
                                    jsonObject.getString(Constant.LOGO),
                                    jsonObject.getString(Constant.VIEW_ORDER_OTP),
                                    jsonObject.getString(Constant.ASSIGN_DELIVERY_BOY),
                                    jsonObject.getString(Constant.STATUS));
                        }
                        invalidateOptionsMenu();
                        GetOrderData("");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        GetOrderData("");
                    }
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    public void getFinancialStatistics() {
        ApiConfig.ShowProgress(activity,progressBar);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
        params.put(Constant.GET_FINANCIAL_STATISTICS, Constant.GetVal);

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            tvOrdersCount.setText(jsonObject.getString(Constant.TOTAL_ORDERS));
                            tvProductsCount.setText(jsonObject.getString(Constant.TOTAL_PRODUCTS));
                            tvSoldOutCount.setText(jsonObject.getString(Constant.TOTAL_SOLD_OUT_PRODUCTS));
                            tvLowStockCount.setText(jsonObject.getString(Constant.TOTAL_LOW_STOCK_COUNT));
                            tvBalance.setText(jsonObject.getString(Constant.BALANCE));
                            session.setData(Constant.BALANCE, jsonObject.getString(Constant.BALANCE));
                            Constant.SETTING_CURRENCY_SYMBOL = jsonObject.getString(Constant.CURRENCY);
                            tvWeeklySales.setText(jsonObject.getString(Constant.TOTAL_SALE));
                            tvTitleWeeklySales.setText(getString(R.string.total_sale_title) + "(" + Constant.SETTING_CURRENCY_SYMBOL + ")");
                            getData();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getData();
                    }
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    void GetOrderData(String query) {
        orderTrackerArrayList = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ORDERS, Constant.GetVal);
        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            total = Integer.parseInt(objectbject.getString(Constant.TOTAL));
                            session.setData(Constant.TOTAL, String.valueOf(total));
                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                if (jsonObject1 != null) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    orderTrackerArrayList.add(ApiConfig.OrderTracker(jsonObject));
                                }
                            }
                            if (offset == 0) {
                                trackerAdapter = new TrackerAdapter(activity.getApplicationContext(), activity, orderTrackerArrayList);
                                recyclerView.setAdapter(trackerAdapter);
                                ApiConfig.HideProgress(activity,progressBar);
                                scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    private boolean isLoadMore;

                                    @Override
                                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                                        // if (diff == 0) {
                                        if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                            if (orderTrackerArrayList.size() < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == orderTrackerArrayList.size() - 1) {
                                                        //bottom of list!
                                                        orderTrackerArrayList.add(null);
                                                        trackerAdapter.notifyItemInserted(orderTrackerArrayList.size() - 1);

                                                        offset += Constant.LOAD_ITEM_LIMIT;
                                                        Map<String, String> params = new HashMap<>();
                                                        params.put(Constant.GET_ORDERS, Constant.GetVal);
                                                        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
                                                        params.put(Constant.OFFSET, "" + offset);
                                                        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

                                                        ApiConfig.RequestToVolley(new VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(boolean result, String response) {

                                                                if (result) {
                                                                    try {
                                                                        // System.out.println("====product  " + response);
                                                                        JSONObject objectbject1 = new JSONObject(response);

                                                                        orderTrackerArrayList.remove(orderTrackerArrayList.size() - 1);
                                                                        trackerAdapter.notifyItemRemoved(orderTrackerArrayList.size());
                                                                        if (!objectbject1.getBoolean(Constant.ERROR)) {
                                                                            session.setData(Constant.TOTAL, objectbject1.getString(Constant.TOTAL));
                                                                            JSONObject object = new JSONObject(response);
                                                                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                                                if (jsonObject1 != null) {
                                                                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                                                    orderTrackerArrayList.add(ApiConfig.OrderTracker(jsonObject));
                                                                                }
                                                                            }
                                                                            trackerAdapter.notifyDataSetChanged();
                                                                            trackerAdapter.setLoaded();
                                                                            isLoadMore = false;
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        }, activity, Constant.MAIN_URL, params, false);

                                                    }
                                                    isLoadMore = true;
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            ApiConfig.HideProgress(activity,progressBar);
                        }
                    } catch (JSONException e) {
                        ApiConfig.HideProgress(activity,progressBar);
                        e.printStackTrace();
                    }

                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(navigationView))
            drawer.closeDrawers();
        else
            doubleBack();
    }

    public void doubleBack() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.please_click_back_again_to_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void enableDisableDeliveryBoy(String status) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
        params.put(Constant.CHANGE_STATUS, Constant.GetVal);
        params.put(Constant.STATUS, status);

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                //  System.out.println("============" + response);
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String message = getString(R.string.delivery_boy_status) + ((status.equals("1") ? getString(R.string.enabled) : getString(R.string.disabled)) + getString(R.string.successfully));
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            if (status.equals("1")) {
                                menu.findItem(R.id.toolbar_action).setIcon(R.drawable.ic_action_off);
                            } else {
                                menu.findItem(R.id.toolbar_action).setIcon(R.drawable.ic_action_on);
                            }
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            session.setData(Constant.STATUS, status);
                            invalidateOptionsMenu();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            session.logoutUserConfirmation(activity);
        }else  if (item.getItemId() == R.id.toolbar_action) {
            enableDisableDeliveryBoy(session.getData(Constant.STATUS).equals("1") ? "0" : "1");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.toolbar_action).setVisible(true);
        if (session.getData(Constant.STATUS).equals("1")) {
            menu.findItem(R.id.toolbar_action).setIcon(R.drawable.ic_action_off);
        } else {
            menu.findItem(R.id.toolbar_action).setIcon(R.drawable.ic_action_on);
        }
        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
