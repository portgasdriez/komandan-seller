package wrteam.multivendor.seller.activity;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import wrteam.multivendor.seller.R;
import wrteam.multivendor.seller.adapter.ItemsAdapter;
import wrteam.multivendor.seller.helper.ApiConfig;
import wrteam.multivendor.seller.helper.Constant;
import wrteam.multivendor.seller.helper.Session;
import wrteam.multivendor.seller.helper.VolleyCallback;
import wrteam.multivendor.seller.model.DeliveryBoy;
import wrteam.multivendor.seller.model.OrderTracker;

public class OrderDetailActivity extends AppCompatActivity {
    OrderTracker order;
    TextView txtorderotp, tvItemTotal, tvDeliveryCharge, tvTotal, tvPromoCode, tvPCAmount, tvWallet, tvFinalTotal, tvDPercent, tvDAmount, tvOrderNote;
    TextView txtotherdetails, txtorderid, txtorderdate;
    RecyclerView recyclerView;
    RelativeLayout relativeLyt;
    LinearLayout lytPromo, lytWallet, lytPriceDetail, lytOTP;
    double totalAfterTax = 0.0;
    String id;
    ScrollView scrollView;
    Activity activity;
    Session session;
    Toolbar toolbar;
    ArrayList<DeliveryBoy> deliveryBoys;
    ArrayList<String> deliveryBoysName;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_order_detail);
            toolbar = findViewById(R.id.toolbar);
            activity = this;
            session = new Session(activity);

            lytPriceDetail = findViewById(R.id.lytPriceDetail);
            lytPromo = findViewById(R.id.lytPromo);
            lytWallet = findViewById(R.id.lytWallet);
            tvItemTotal = findViewById(R.id.tvItemTotal);
            tvDeliveryCharge = findViewById(R.id.tvDeliveryCharge);
            tvDAmount = findViewById(R.id.tvDAmount);
            tvDPercent = findViewById(R.id.tvDPercent);
            tvTotal = findViewById(R.id.tvTotal);
            tvPromoCode = findViewById(R.id.tvPromoCode);
            tvPCAmount = findViewById(R.id.tvPCAmount);
            tvWallet = findViewById(R.id.tvWallet);
            tvFinalTotal = findViewById(R.id.tvFinalTotal);
            txtorderid = findViewById(R.id.txtorderid);
            txtorderdate = findViewById(R.id.txtorderdate);
            relativeLyt = findViewById(R.id.relativeLyt);
            txtotherdetails = findViewById(R.id.txtotherdetails);
            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setNestedScrollingEnabled(false);
            txtorderotp = findViewById(R.id.txtorderotp);
            lytOTP = findViewById(R.id.lytOTP);
            scrollView = findViewById(R.id.scrollView);
            progressBar = findViewById(R.id.progressBar);
            tvOrderNote = findViewById(R.id.tvOrderNote);

            ApiConfig.ShowProgress(activity,progressBar);

            id = getIntent().getStringExtra("id");
            getDeliveryBoys();
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.order_detail));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            ApiConfig.HideProgress(activity,progressBar);
            e.printStackTrace();
        }
    }

    private void getDeliveryBoys() {
        scrollView.setVisibility(View.GONE);
        deliveryBoys = new ArrayList<>();
        deliveryBoysName = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_DELIVERY_BOYS, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject objectbject = new JSONObject(response);
                    if (!objectbject.getBoolean(Constant.ERROR)) {
                        if (getContext() != null) {
                            JSONArray jsonArray = objectbject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                deliveryBoys.add(new DeliveryBoy(jsonObject.getString(Constant.ID), jsonObject.getString(Constant.NAME)));
                                deliveryBoysName.add(jsonObject.getString(Constant.NAME));
                            }
                        }
                    }
                    if (id.equals("")) {
                        order = (OrderTracker) getIntent().getSerializableExtra("model");
                        id = order.getId();
                        SetData(order);
                    } else {
                        getOrderDetails(id);
                    }
                } catch (JSONException e) {
                    ApiConfig.HideProgress(activity,progressBar);
                    if (id.equals("")) {
                        order = (OrderTracker) getIntent().getSerializableExtra("model");
                        id = order.getId();
                        SetData(order);
                    } else {
                        getOrderDetails(id);
                    }
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    public void getOrderDetails(String id) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ORDERS, Constant.GetVal);
        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
        params.put(Constant.ORDER_ID, id);

        //  System.out.println("=====params " + params.toString());
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(response);
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            JSONObject jsonObject = jsonObject1.getJSONArray(Constant.DATA).getJSONObject(0);
                            SetData(ApiConfig.OrderTracker(jsonObject));
                            scrollView.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        ApiConfig.HideProgress(activity,progressBar);
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    @SuppressLint("SetTextI18n")
    public void SetData(OrderTracker order) {
        try {
            String[] date = order.getDate_added().split("\\s+");
            txtorderid.setText(order.getId());
            if (order.getOtp().equals("0") || session.getData(Constant.VIEW_ORDER_OTP).equals("0")) {
                lytOTP.setVisibility(View.GONE);
            } else {
                lytOTP.setVisibility(View.VISIBLE);
                txtorderotp.setText(order.getOtp());
            }
            tvOrderNote.setText(order.getOrder_note().equals("")?"-":order.getOrder_note());
            txtorderdate.setText(date[0]);
            if (session.getData(Constant.CUSTOMER_PRIVACY).equals("1")) {
                txtotherdetails.setText(getString(R.string.name_1) + order.getUser_name() + getString(R.string.mobile_no_1) + order.getMobile() + getString(R.string.address_1) + order.getAddress());
            } else {
                txtotherdetails.setText("-");
            }
            totalAfterTax = (Double.parseDouble(order.getTotal()) + Double.parseDouble(order.getDelivery_charge()));
            tvItemTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + ApiConfig.StringFormat(order.getTotal()));
            tvDeliveryCharge.setText("+ " + Constant.SETTING_CURRENCY_SYMBOL + ApiConfig.StringFormat(order.getDelivery_charge()));
            tvDAmount.setText("- " + Constant.SETTING_CURRENCY_SYMBOL + ApiConfig.StringFormat(order.getDiscounted_price()));
            tvTotal.setText("+ " + Constant.SETTING_CURRENCY_SYMBOL + ApiConfig.StringFormat(order.getTotal()));
            if (!order.getPromo_code().equals("")) {
                lytPromo.setVisibility(View.VISIBLE);
                tvPromoCode.setText(getString(R.string.promo_applied) + "(" + order.getPromo_code() + ")");
                tvPCAmount.setText("- " + Constant.SETTING_CURRENCY_SYMBOL + ApiConfig.StringFormat(order.getPromo_discount()));
            } else {
                lytPromo.setVisibility(View.GONE);
            }

            tvWallet.setText("- " + Constant.SETTING_CURRENCY_SYMBOL + ApiConfig.StringFormat(order.getWallet_balance()));
            tvFinalTotal.setText(Constant.SETTING_CURRENCY_SYMBOL + ApiConfig.StringFormat(order.getFinal_total()));

            recyclerView.setAdapter(new ItemsAdapter(activity, order.getItems(), deliveryBoys, deliveryBoysName));
            scrollView.setVisibility(View.VISIBLE);
            ApiConfig.HideProgress(activity,progressBar);
        } catch (Exception e) {
            e.printStackTrace();
            ApiConfig.HideProgress(activity,progressBar);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}