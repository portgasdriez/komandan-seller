package wrteam.multivendor.seller.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import wrteam.multivendor.seller.R;
import wrteam.multivendor.seller.adapter.ImageAdapter;
import wrteam.multivendor.seller.adapter.PinCodeAdapter;
import wrteam.multivendor.seller.adapter.ProductImagesAdapter;
import wrteam.multivendor.seller.adapter.ProductItemAdapter;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.helpers.Constants;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.models.Image;
import wrteam.multivendor.seller.helper.ApiConfig;
import wrteam.multivendor.seller.helper.Constant;
import wrteam.multivendor.seller.helper.OnIntentReceived;
import wrteam.multivendor.seller.helper.Session;
import wrteam.multivendor.seller.model.Categories;
import wrteam.multivendor.seller.model.OrderStatus;
import wrteam.multivendor.seller.model.PinCode;
import wrteam.multivendor.seller.model.PriceVariation;
import wrteam.multivendor.seller.model.Product;
import wrteam.multivendor.seller.model.SubCategories;
import wrteam.multivendor.seller.model.Tax;
import wrteam.multivendor.seller.model.Unit;

@SuppressLint("NotifyDataSetChanged")
public class ProductDetailActivity extends AppCompatActivity {

    private static final int RESULT_CODE_SINGLE = 1;
    private static final int RESULT_CODE_MULTI = 11;
    private static final int RESULT_CODE_VARIANT_IMAGES = 111;
    static Product product;
    Activity activity;
    Session session;
    Toolbar toolbar;
    EditText edtManufacturer, edtMadeIn, edtReturnDays, edtDescription;
    TextView tvMainImageName, tvOtherImagesName, edtProductName, btnMainImage, btnOtherImages, btnEditDoneDescription;
    Spinner spinnerProductType, spinnerDeliveryPlaces, spinnerTax, spinnerTillStatus, spinnerCategory, spinnerSubCategory;
    String from;
    SwitchCompat switchIsReturnable, switchIsCancellable;
    TextInputLayout lytReturnDays;
    RelativeLayout lytTillStatus;
    ProductItemAdapter productItemAdapter;
    PinCodeAdapter pinCodeAdapter;
    RecyclerView recyclerView, recyclerViewSelectedPinCodes, recyclerViewImageGallery, recycleviewimglist;
    RadioButton rdPacket, rdLoose;
    ProgressBar progressBar;
    CategoryAdapter categoryAdapter;
    SubCategoryAdapter subCategoryAdapter;
    StatusAdapter orderStatusAdapter;
    ArrayList<String> arrayListStockStatus, arrayListDeliveryPlaces;
    public static ArrayList<PriceVariation> priceVariations;
    ArrayList<Unit> arrayListUnit;
    ArrayList<Categories> arrayListCategories;
    ArrayList<Tax> arrayListTax;
    ArrayList<PinCode> arrayListPinCode, arrayListSelectedPinCode;
    ArrayList<String> pinCodesIds;
    ArrayList<OrderStatus> orderStatuses;
    Button btnAddUpdate;
    Button lytSelectedPinCodes;
    ImageView imgMainImage;
    ProductImagesAdapter productImagesAdapter;
    WebView webView;
    public static String productType = "packet";
    LinearLayout albumLyt;
    private List<Image> mAlbumFiles, mainImage;
    private ImageAdapter mAdapter;
    LinearLayout lytStock;
    EditText edtStock;
    String looseStockId;
    Spinner spinnerMeasurement;
    int position = 0;
    OnIntentReceived mIntentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_product_detail);

            activity = ProductDetailActivity.this;
            session = new Session(activity);

            toolbar = findViewById(R.id.toolbar);
            edtProductName = findViewById(R.id.edtProductName);
            edtManufacturer = findViewById(R.id.edtManufacturer);
            edtMadeIn = findViewById(R.id.edtMadeIn);
            edtReturnDays = findViewById(R.id.edtReturnDays);
            spinnerProductType = findViewById(R.id.spinnerProductType);
            spinnerDeliveryPlaces = findViewById(R.id.spinnerDeliveryPlaces);
            spinnerTillStatus = findViewById(R.id.spinnerTillStatus);
            spinnerTax = findViewById(R.id.spinnerTax);
            switchIsReturnable = findViewById(R.id.switchIsReturnable);
            switchIsCancellable = findViewById(R.id.switchIsCancellable);
            lytReturnDays = findViewById(R.id.lytReturnDays);
            lytTillStatus = findViewById(R.id.lytTillStatus);
            recyclerView = findViewById(R.id.recyclerView);
            recyclerViewSelectedPinCodes = findViewById(R.id.recyclerViewSelectedPinCodes);
            rdPacket = findViewById(R.id.rdPacket);
            rdLoose = findViewById(R.id.rdLoose);
            progressBar = findViewById(R.id.progressBar);
            spinnerCategory = findViewById(R.id.spinnerCategory);
            spinnerSubCategory = findViewById(R.id.spinnerSubCategory);
            btnAddUpdate = findViewById(R.id.btnAddUpdate);
            lytSelectedPinCodes = findViewById(R.id.lytSelectedPinCodes);
            btnMainImage = findViewById(R.id.btnMainImage);
            btnOtherImages = findViewById(R.id.btnOtherImages);
            tvMainImageName = findViewById(R.id.tvMainImageName);
            tvOtherImagesName = findViewById(R.id.tvOtherImagesName);
            recyclerViewImageGallery = findViewById(R.id.recyclerViewImageGallery);
            imgMainImage = findViewById(R.id.imgMainImage);
            webView = findViewById(R.id.webView);
            edtDescription = findViewById(R.id.edtDescription);
            btnEditDoneDescription = findViewById(R.id.btnEditDoneDescription);
            albumLyt = findViewById(R.id.albumLyt);
            lytStock = findViewById(R.id.lytStock);
            spinnerMeasurement = findViewById(R.id.spinnerMeasurement);
            edtStock = findViewById(R.id.edtStock);

            setSupportActionBar(toolbar);
            Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar())).setTitle(getString(R.string.product_detail));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            ApiConfig.ShowProgress(activity, progressBar);
            btnEditDoneDescription.setText(getString(R.string.edit));

            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(getString(R.string.select_product_type));
            arrayList.add(getString(R.string.veg));
            arrayList.add(getString(R.string.non_veg));
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProductType.setAdapter(arrayAdapter);

            arrayListDeliveryPlaces = new ArrayList<>();
            arrayListDeliveryPlaces.add(getString(R.string.city_included));
            arrayListDeliveryPlaces.add(getString(R.string.city_excluded));
            arrayListDeliveryPlaces.add(getString(R.string.includes_all));
            ArrayAdapter<String> arrayAdapterDeliveryPlaces = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayListDeliveryPlaces);
            arrayAdapterDeliveryPlaces.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDeliveryPlaces.setAdapter(arrayAdapterDeliveryPlaces);

            arrayListUnit = new ArrayList<>();
            mAlbumFiles = new ArrayList<>();
            priceVariations = new ArrayList<>();
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setNestedScrollingEnabled(false);
            recyclerViewSelectedPinCodes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerViewImageGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recycleviewimglist = findViewById(R.id.recycleviewimglist);
            recycleviewimglist.setLayoutManager(new GridLayoutManager(this, 3));

            mAdapter = new ImageAdapter(activity);

            recycleviewimglist.setAdapter(mAdapter);
            from = getIntent().getStringExtra("from");
            pinCodesIds = new ArrayList<>();
            assert from != null;
            if (from.equals("product")) {
                product = (Product) getIntent().getSerializableExtra("model");
                position = Integer.parseInt(String.valueOf(getIntent().getSerializableExtra("position")));
                priceVariations = product.getVariants();

                btnAddUpdate.setText(activity.getString(R.string.update_product));

                Picasso.get().
                        load(product.getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imgMainImage);

                productImagesAdapter = new ProductImagesAdapter(activity, product.getOther_images() == null ? new ArrayList<>() : product.getOther_images(), "api", product.getId());
                recyclerViewImageGallery.setAdapter(productImagesAdapter);

                switch (product.getDelivery_places()) {
                    case "0":
                        spinnerDeliveryPlaces.setSelection(0);
                        break;
                    case "1":
                        spinnerDeliveryPlaces.setSelection(1);
                        break;
                    case "2":
                        spinnerDeliveryPlaces.setSelection(2);
                        break;
                }

                productType = product.getType();

                edtProductName.setText(product.getName());
                edtManufacturer.setText(product.getManufacturer());
                edtMadeIn.setText(product.getMade_in());
                edtReturnDays.setText(product.getReturn_days());

                if (product.getReturn_status().equals("1")) {
                    switchIsReturnable.setChecked(true);
                    lytReturnDays.setVisibility(View.VISIBLE);
                    edtReturnDays.setText(product.getReturn_days());
                }

                if (product.getCancelable_status().equals("1")) {
                    switchIsCancellable.setChecked(true);
                    lytTillStatus.setVisibility(View.VISIBLE);
                }

                if (!product.getDelivery_places().equals("2")) {
                    pinCodesIds.addAll(Arrays.asList(product.getPincodes().split(",")));
                }

                webView.loadData(product.getDescription(), "text/html; charset=utf-8", "utf-8");
                edtDescription.setText(product.getDescription());

                if (product.getDelivery_places().equals("2")) {
                    recyclerViewSelectedPinCodes.setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent));
                } else {
                    recyclerViewSelectedPinCodes.setBackgroundColor(ContextCompat.getColor(activity, R.color.gray));
                }

            } else {
                product = new Product();
                priceVariations = new ArrayList<>();
                priceVariations.add(new PriceVariation());
                product.setVariants(priceVariations);
                switchIsCancellable.setTag("off");
                switchIsReturnable.setTag("off");
                btnAddUpdate.setText(activity.getString(R.string.add_product));
            }
            productType = rdPacket.isChecked() ? "packet" : "loose";


            if (productType.equals("packet")) {
                lytStock.setVisibility(View.GONE);
                rdPacket.setChecked(true);
                productType = "packet";
            } else {
                lytStock.setVisibility(View.VISIBLE);
                rdLoose.setChecked(true);
                productType = "loose";
            }

            rdLoose.setOnClickListener(v -> {
                rdLoose.setChecked(true);
                productType = "loose";
                lytStock.setVisibility(View.VISIBLE);
                productItemAdapter.notifyDataSetChanged();

            });

            rdPacket.setOnClickListener(v -> {
                rdPacket.setChecked(true);
                productType = "packet";
                lytStock.setVisibility(View.GONE);
                productItemAdapter.notifyDataSetChanged();
            });
            switchIsReturnable.setOnClickListener(v -> {
                if (from.equals("product")) {
                    if (product.getReturn_status().equals("1")) {
                        product.setReturn_status("0");
                        lytReturnDays.setVisibility(View.GONE);
                    } else if (product.getReturn_status().equals("0")) {
                        product.setReturn_status("1");
                        lytReturnDays.setVisibility(View.VISIBLE);
                        edtReturnDays.setText(product.getReturn_days());
                    }
                } else {
                    if (switchIsReturnable.getTag().equals("on")) {
                        lytReturnDays.setVisibility(View.GONE);
                        switchIsReturnable.setChecked(false);
                        switchIsReturnable.setTag("off");

                    } else {
                        lytReturnDays.setVisibility(View.VISIBLE);
                        switchIsReturnable.setChecked(true);
                        switchIsReturnable.setTag("on");
                    }
                }
            });

            switchIsCancellable.setOnClickListener(v -> {
                if (from.equals("product")) {
                    if (product.getCancelable_status().equals("1")) {
                        product.setCancelable_status("0");
                        lytTillStatus.setVisibility(View.GONE);
                    } else if (product.getCancelable_status().equals("0")) {
                        product.setCancelable_status("1");
                        lytTillStatus.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (switchIsCancellable.getTag().equals("on")) {
                        lytTillStatus.setVisibility(View.GONE);
                        switchIsCancellable.setChecked(false);
                        switchIsCancellable.setTag("off");
                    } else {
                        lytTillStatus.setVisibility(View.VISIBLE);
                        switchIsCancellable.setChecked(true);
                        switchIsCancellable.setTag("on");
                    }
                }
            });

            btnEditDoneDescription.setOnClickListener(v -> {
                if (btnEditDoneDescription.getTag().equals("edit")) {
                    btnEditDoneDescription.setTag("done");
                    btnEditDoneDescription.setText(getString(R.string.done));
                    webView.setVisibility(View.GONE);
                    edtDescription.setVisibility(View.VISIBLE);
                } else {
                    btnEditDoneDescription.setTag("edit");
                    btnEditDoneDescription.setText(getString(R.string.edit));
                    edtDescription.setVisibility(View.GONE);
                    webView.loadData(edtDescription.getText().toString(), "text/html; charset=utf-8", "utf-8");
                    webView.setVisibility(View.VISIBLE);
                }
            });

            btnMainImage.setOnClickListener(v -> SelectImage("single", activity));

            btnOtherImages.setOnClickListener(v -> {
                albumLyt.setVisibility(View.VISIBLE);
                SelectImage("multi", activity);
            });

            spinnerDeliveryPlaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 2) {
                        recyclerViewSelectedPinCodes.setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent));
                    } else {
                        recyclerViewSelectedPinCodes.setBackgroundColor(ContextCompat.getColor(activity, R.color.gray));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            btnAddUpdate.setOnClickListener(v -> verifiedAddProducts());

            GetCategory();
        } catch (Exception e) {
            ApiConfig.HideProgress(activity, progressBar);
            e.printStackTrace();
        }
    }

    public void verifiedAddProducts() {
        String pName = edtProductName.getText().toString();
        String description = edtDescription.getText().toString();
        if (pName.isEmpty()) {
            Toast.makeText(activity, getString(R.string.error_msg_product_name), Toast.LENGTH_SHORT).show();
        } else if (description.isEmpty()) {
            Toast.makeText(activity, getString(R.string.error_msg_product_description), Toast.LENGTH_SHORT).show();
        } else if (productType.equals("loose") && edtStock.getText().toString().isEmpty()) {
            Toast.makeText(activity, getString(R.string.error_msg_stock), Toast.LENGTH_SHORT).show();
        } else if (spinnerDeliveryPlaces.getSelectedItemPosition() != 2 && arrayListSelectedPinCode.size() == 0) {
            Toast.makeText(activity, getString(R.string.error_msg_pincodes), Toast.LENGTH_SHORT).show();
        } else {
            if (variantsValidation().equals("validate")) {
                if (from.equals("product")) {
                    addOrUpdateProduct();
                } else {
                    if (mainImage == null) {
                        Toast.makeText(activity, getString(R.string.error_msg_product_image), Toast.LENGTH_SHORT).show();
                    } else if (mAlbumFiles.size() == 0) {
                        Toast.makeText(activity, getString(R.string.error_msg_extra_product_image), Toast.LENGTH_SHORT).show();
                    } else {
                        addOrUpdateProduct();
                    }
                }
            } else {
                Toast.makeText(activity, variantsValidation(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String variantsValidation() {
        String validate = "validate";
        for (int i = 0; i < priceVariations.size(); i++) {
            PriceVariation priceVariation = priceVariations.get(i);
            if (priceVariation.getMeasurement() == null || priceVariation.getMeasurement().isEmpty()) {
                return "Please enter measurement in variant field!";
            } else if (priceVariation.getPrice() == null || priceVariation.getPrice().isEmpty()) {
                return "Please enter original price in variant field!";
            } else if (priceVariation.getDiscounted_price() == null || priceVariation.getDiscounted_price().isEmpty()) {
                return "Please enter discounted price in variant field!";
            } else if (priceVariation.getStock() == null || priceVariation.getStock().isEmpty()) {
                if (productType.equals("packet")) {
                    return "Please enter stock in variant field!";
                }
            }
        }
        return validate;
    }

    public static void SelectImage(String type, Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Intent intent = new Intent(activity, AlbumSelectActivity.class);

            switch (type) {
                case "single":
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1);
                    break;
                case "multi":
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 5);
                    break;
                case "variant":
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10);
                    break;
            }

            activity.startActivityForResult(intent, type.equals("single") ? RESULT_CODE_SINGLE : type.equals("variant") ? RESULT_CODE_VARIANT_IMAGES : RESULT_CODE_MULTI);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null)
            if (requestCode == RESULT_CODE_MULTI) {
                //The array list has the image paths of the selected images
                mAlbumFiles = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                mAdapter.notifyDataSetChanged(mAlbumFiles);
                recyclerViewImageGallery.setAdapter(mAdapter);
            } else if (requestCode == RESULT_CODE_SINGLE) {
                //The array list has the image paths of the selected images
                mainImage = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                imgMainImage.setImageURI(Uri.parse(mainImage.get(0).path));
            } else if (requestCode == RESULT_CODE_VARIANT_IMAGES && mIntentListener != null) {
                mIntentListener.onIntent(data, resultCode);
            }
    }

    public void GetCategory() {
        arrayListCategories = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
        params.put(Constant.GET_CATEGORIES, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        for (int i = 0; i < jsonObject.getJSONArray(Constant.DATA).length(); i++) {
                            Categories categories = new Gson().fromJson(jsonObject.getJSONArray(Constant.DATA).getJSONObject(i).toString(), Categories.class);
                            arrayListCategories.add(categories);
                        }

                        categoryAdapter = new CategoryAdapter(activity, arrayListCategories, spinnerCategory, spinnerSubCategory);
                        spinnerCategory.setAdapter(categoryAdapter);

                        GetUnits();
                    }
                } catch (JSONException e) {
                    ApiConfig.HideProgress(activity, progressBar);
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    public void GetUnits() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_UNITS, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        for (int i = 0; i < jsonObject.getJSONArray(Constant.DATA).length(); i++) {
                            Unit unit = new Unit(jsonObject.getJSONArray(Constant.DATA).getJSONObject(i).getString(Constant.ID), jsonObject.getJSONArray(Constant.DATA).getJSONObject(i).getString(Constant.NAME));
                            arrayListUnit.add(unit);
                        }

                        GetTaxes();
                        LooseStockUnitAdapter unitStockAdapter = new LooseStockUnitAdapter(activity, arrayListUnit);
                        spinnerMeasurement.setAdapter(unitStockAdapter);
                    }
                } catch (JSONException e) {
                    ApiConfig.HideProgress(activity, progressBar);
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    public void GetTaxes() {
        arrayListTax = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_TAXES, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Tax tax1 = new Tax("0", activity.getString(R.string.select_tax), "0");
                    arrayListTax.add(tax1);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        for (int i = 0; i < jsonObject.getJSONArray(Constant.DATA).length(); i++) {
                            Tax tax = new Gson().fromJson(jsonObject.getJSONArray(Constant.DATA).getJSONObject(i).toString(), Tax.class);
                            arrayListTax.add(tax);
                        }
                    }
                    TaxAdapter taxAdapter = new TaxAdapter(activity, arrayListTax, spinnerTax);
                    spinnerTax.setAdapter(taxAdapter);
                    GetPinCodes();
                } catch (JSONException e) {
                    ApiConfig.HideProgress(activity, progressBar);
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    public void GetPinCodes() {
        if (pinCodesIds != null && pinCodesIds.size() <= 0) {
            pinCodesIds = new ArrayList<>();
        }
        arrayListPinCode = new ArrayList<>();
        arrayListSelectedPinCode = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_PINCODES, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        for (int i = 0; i < jsonObject.getJSONArray(Constant.DATA).length(); i++) {
                            PinCode pinCode = new Gson().fromJson(jsonObject.getJSONArray(Constant.DATA).getJSONObject(i).toString(), PinCode.class);
                            arrayListPinCode.add(pinCode);
                            if (pinCodesIds != null && pinCodesIds.size() > 0) {
                                for (int j = 0; j < pinCodesIds.size(); j++) {
                                    if (arrayListPinCode.get(i).getId().equals(pinCodesIds.get(j))) {
                                        arrayListSelectedPinCode.add(pinCode);
                                        break;
                                    }
                                }
                            }
                        }

                        if (from.equals("product") && !product.getDelivery_places().equals("all")) {
                            pinCodeAdapter = new PinCodeAdapter(activity, arrayListSelectedPinCode);
                            recyclerViewSelectedPinCodes.setAdapter(pinCodeAdapter);
                        }

                        TaxAdapter taxAdapter = new TaxAdapter(activity, arrayListTax, spinnerTax);
                        spinnerTax.setAdapter(taxAdapter);

                        orderStatuses = new ArrayList<>();
                        orderStatuses.add(new OrderStatus(Constant.RECEIVED, activity.getString(R.string.received)));
                        orderStatuses.add(new OrderStatus(Constant.PROCESSED, activity.getString(R.string.processed)));
                        orderStatuses.add(new OrderStatus(Constant.SHIPPED, activity.getString(R.string.shipped)));
                        orderStatusAdapter = new StatusAdapter(activity, orderStatuses, spinnerTillStatus);
                        spinnerTillStatus.setAdapter(orderStatusAdapter);


                        arrayListStockStatus = new ArrayList<>();
                        arrayListStockStatus.add(getString(R.string.available));
                        arrayListStockStatus.add(getString(R.string.sold_out));

                        try {
                            taxAdapter.setItem(product.getTax_id());
                            if (!product.getTill_status().equals("")) {
                                orderStatusAdapter.setItem(product.getTill_status());
                            }

                            categoryAdapter.setItem(product.getCategory_id());
                        } catch (Exception ignore) {

                        }

                    }
                    productItemAdapter = new ProductItemAdapter(activity, priceVariations, arrayListUnit, arrayListStockStatus, from);
                    recyclerView.setAdapter(productItemAdapter);

                    mIntentListener = productItemAdapter;

                    lytSelectedPinCodes.setOnClickListener(v -> {
                        if (spinnerDeliveryPlaces.getSelectedItemPosition() != 2) {
                            String[] listItems = new String[arrayListPinCode.size()];

                            for (int i = 0; i < arrayListPinCode.size(); i++) {
                                listItems[i] = arrayListPinCode.get(i).getPincode();
                            }

                            boolean[] checkedItems = new boolean[arrayListPinCode.size()]; //this will checked the items when user open the dialog
                            try {
                                for (int i = 0; i < checkedItems.length; i++) {
                                    for (int j = 0; j < pinCodesIds.size(); j++) {
                                        if (arrayListPinCode.get(i).getId().equals(pinCodesIds.get(j))) {
                                            checkedItems[i] = true;
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception ignore) {

                            }

                            AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
                            mBuilder.setTitle(activity.getString(R.string.selected_pincodes));
                            mBuilder.setMultiChoiceItems(listItems, checkedItems, (dialogInterface, position, isChecked) -> {
                                if (isChecked) {
                                    pinCodesIds.add(arrayListPinCode.get(position).getId());
                                    arrayListSelectedPinCode.add(arrayListPinCode.get(position));
                                } else {
                                    pinCodesIds.remove(arrayListPinCode.get(position).getId());
                                    arrayListSelectedPinCode.remove(arrayListPinCode.get(position));
                                }
                            });

                            mBuilder.setCancelable(true);
                            mBuilder.setPositiveButton(R.string.ok, (dialogInterface, which) -> {
                                pinCodeAdapter.notifyDataSetChanged();
                                dialogInterface.dismiss();
                            });
                            pinCodeAdapter = new PinCodeAdapter(activity, arrayListSelectedPinCode);
                            recyclerViewSelectedPinCodes.setAdapter(pinCodeAdapter);
                            AlertDialog mDialog = mBuilder.create();
                            mDialog.show();
                        }
                    });

                    productImagesAdapter = new ProductImagesAdapter(activity, product.getOther_images() == null ? new ArrayList<>() : product.getOther_images(), from, product.getId());
                    recyclerViewImageGallery.setAdapter(productImagesAdapter);

                    ApiConfig.HideProgress(activity, progressBar);
                } catch (JSONException e) {
                    ApiConfig.HideProgress(activity, progressBar);
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public static class TaxAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<Tax> taxes;
        final LayoutInflater inflter;
        Spinner spinnerTax;


        public TaxAdapter(Context applicationContext, ArrayList<Tax> taxes, Spinner spinnerTax) {
            this.context = applicationContext;
            this.taxes = taxes;
            this.spinnerTax = spinnerTax;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return taxes.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setItem(String id) {
            for (int i = 0; i < getCount(); i++) {
                if (id.equals(taxes.get(i).getId())) {
                    spinnerTax.setSelection(i);
                }
            }
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView txtmeasurement = view.findViewById(R.id.txtmeasurement);
            TextView txtprice = view.findViewById(R.id.txtprice);

            Tax tax = taxes.get(position);
            txtmeasurement.setText(tax.getTitle());
            txtprice.setText(tax.getPercentage() + "%");

            spinnerTax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        product.setTax_id(tax.getId());
                        product.setTax_percentage(tax.getPercentage());
                        product.setTax_title(tax.getTitle());
                    } catch (Exception ignore) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return view;
        }
    }

    public static class StatusAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<OrderStatus> statuses;
        final LayoutInflater inflter;
        Spinner spinnerTillStatus;

        public StatusAdapter(Context applicationContext, ArrayList<OrderStatus> statuses, Spinner spinnerTillStatus) {
            this.context = applicationContext;
            this.statuses = statuses;
            this.spinnerTillStatus = spinnerTillStatus;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return statuses.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setItem(String id) {
            for (int i = 0; i < getCount(); i++) {
                if (id.equals(statuses.get(i).getStatusName())) {
                    spinnerTillStatus.setSelection(i);
                }
            }
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView txtmeasurement = view.findViewById(R.id.txtmeasurement);

            OrderStatus orderStatus = statuses.get(position);
            txtmeasurement.setText(orderStatus.getDisplayName());

            spinnerTillStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        product.setTill_status(orderStatus.getStatusName());
                    } catch (Exception ignore) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return view;
        }
    }

    public static class SubCategoryAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<SubCategories> subCategories;
        final LayoutInflater inflter;
        Spinner spinnerSubCategory;

        public SubCategoryAdapter(Context applicationContext, ArrayList<SubCategories> subCategories, Spinner spinnerSubCategory) {
            this.context = applicationContext;
            this.subCategories = subCategories;
            this.spinnerSubCategory = spinnerSubCategory;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return subCategories.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setItem(String id) {
            for (int i = 0; i < getCount(); i++) {
                if (id.equals(subCategories.get(i).getId())) {
                    spinnerSubCategory.setSelection(i);
                }
            }
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView txtmeasurement = view.findViewById(R.id.txtmeasurement);

            txtmeasurement.setText(subCategories.get(position).getName());

            return view;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void addOrUpdateProduct() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            StringBuilder id = new StringBuilder();
            StringBuilder measurement = new StringBuilder();
            StringBuilder measurement_unit_id = new StringBuilder();
            StringBuilder price = new StringBuilder();
            StringBuilder discounted_price = new StringBuilder();
            StringBuilder serve_for = new StringBuilder();
            StringBuilder stock = new StringBuilder();
            StringBuilder stock_unit_id = new StringBuilder();
            StringBuilder pincodes_ = new StringBuilder();

            for (int i = 0; i < priceVariations.size(); i++) {
                PriceVariation variant = priceVariations.get(i);
                if (mAlbumFiles.size() != 0) {
                    for (int j = 0; j < mAlbumFiles.size(); j++) {
                        File file = new File(mAlbumFiles.get(i).path);
                        builder.addFormDataPart(Constant.VARIANT_OTHER_IMAGES + "[" + i + "][]", file.getName(), RequestBody.create(getMimeType(file), file));
                    }
                }
                if (i == 0) {
                    if (from.equals("product")) {
                        id = variant.getId() == null ? new StringBuilder("0") : new StringBuilder(variant.getId());
                    }
                    if (productType.equals("packet")) {
                        stock = new StringBuilder(variant.getStock());
                        stock_unit_id = new StringBuilder(variant.getStock_unit_id());
                    }
                    measurement = new StringBuilder(variant.getMeasurement());
                    measurement_unit_id = new StringBuilder(variant.getMeasurement_unit_id());
                    price = new StringBuilder(variant.getPrice());
                    discounted_price = new StringBuilder(variant.getDiscounted_price());
                    serve_for = new StringBuilder(variant.getServe_for());
                } else {
                    if (from.equals("product")) {
                        id.append(",").append(variant.getId() == null ? "0" : variant.getId());
                    }
                    if (productType.equals("packet")) {
                        stock.append(",").append(variant.getStock());
                        stock_unit_id.append(",").append(variant.getStock_unit_id());
                    }
                    measurement.append(",").append(variant.getMeasurement());
                    measurement_unit_id.append(",").append(variant.getMeasurement_unit_id());
                    price.append(",").append(variant.getPrice());
                    discounted_price.append(",").append(variant.getDiscounted_price());
                    serve_for.append(",").append(variant.getServe_for().trim());
                }
            }

            builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal);

            if (spinnerDeliveryPlaces.getSelectedItemPosition() != 2) {
                for (int i = 0; i < arrayListSelectedPinCode.size(); i++) {
                    if (i == 0)
                        pincodes_ = new StringBuilder(arrayListSelectedPinCode.get(i).getId());
                    else {
                        pincodes_.append(",").append(arrayListSelectedPinCode.get(i).getId());
                    }
                }
                builder.addFormDataPart(Constant.PINCODES, pincodes_.toString());
            } else {
                builder.addFormDataPart(Constant.PINCODES, "");
            }
//                // Adding file data to http body
            builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal);
            if (from.equals("product")) {
                builder.addFormDataPart(Constant.UPDATE_PRODUCTS, Constant.GetVal);
                builder.addFormDataPart(Constant.ID, product.getId());
                builder.addFormDataPart(Constant.PRODUCT_VARIANT_ID, id.toString());
            } else {
                builder.addFormDataPart(Constant.ADD_PRODUCTS, Constant.GetVal);
            }
            builder.addFormDataPart(Constant.DELIVERY_PLACES, "" + spinnerDeliveryPlaces.getSelectedItemPosition());
            builder.addFormDataPart(Constant.SELLER_ID, session.getData(Constant.ID));
            builder.addFormDataPart(Constant.NAME, edtProductName.getText().toString().trim());
            builder.addFormDataPart(Constant.DESCRIPTION, edtDescription.getText().toString());
            String strCateId = arrayListCategories.get(spinnerCategory.getSelectedItemPosition()).getId();
            builder.addFormDataPart(Constant.CATEGORY_ID, strCateId);
            String strSubCate = arrayListCategories.get(spinnerCategory.getSelectedItemPosition()).getSubCategories().get(spinnerSubCategory.getSelectedItemPosition()).getId();
            builder.addFormDataPart(Constant.SUB_CATEGORY_ID, (strSubCate == null || strSubCate.isEmpty()) ? "0" : strSubCate);
            builder.addFormDataPart(Constant.SERVE_FOR, serve_for.toString());

            if (switchIsCancellable.isChecked()) {
                builder.addFormDataPart(Constant.CANCELABLE_STATUS, "1");
                builder.addFormDataPart(Constant.RETURN_DAYS, edtReturnDays.getText().toString().trim());
            } else {
                builder.addFormDataPart(Constant.CANCELABLE_STATUS, "0");
            }
            if (switchIsReturnable.isChecked()) {
                builder.addFormDataPart(Constant.RETURN_STATUS, "1");
                builder.addFormDataPart(Constant.TILL_STATUS, orderStatuses.get(spinnerTillStatus.getSelectedItemPosition()).getStatusName());
            } else {
                builder.addFormDataPart(Constant.RETURN_STATUS, "0");
            }
            if (arrayListTax != null) {
                builder.addFormDataPart(Constant.TAX_ID, arrayListTax.get(spinnerTax.getSelectedItemPosition()).getId());
            }
            builder.addFormDataPart(Constant.MANUFACTURER, edtManufacturer.getText().toString().trim());
            builder.addFormDataPart(Constant.MADE_IN, edtMadeIn.getText().toString().trim());
            builder.addFormDataPart(Constant.INDICATOR, "" + spinnerProductType.getSelectedItemPosition());

            builder.addFormDataPart(Constant.MEASUREMENT, measurement.toString());
            builder.addFormDataPart(Constant.MEASUREMENT_UNIT_ID, measurement_unit_id.toString());
            builder.addFormDataPart(Constant.PRICE, price.toString());
            builder.addFormDataPart(Constant.DISCOUNTED_PRICE, discounted_price.toString());

            if (rdLoose.isChecked()) {
                builder.addFormDataPart(Constant.TYPE, "loose");
                builder.addFormDataPart(Constant.LOOSE_STOCK, edtStock.getText().toString());
                builder.addFormDataPart(Constant.LOOSE_STOCK_UNIT_ID, looseStockId);
            } else {
                builder.addFormDataPart(Constant.TYPE, "packet");
                builder.addFormDataPart(Constant.STOCK, stock.toString());
                builder.addFormDataPart(Constant.STOCK_UNIT_ID, stock_unit_id.toString());
            }

            if (mainImage != null) {
                File file = new File(mainImage.get(0).path);
                builder.addFormDataPart(Constant.IMAGE, file.getName(), RequestBody.create(getMimeType(file), file));
            }

            for (int i = 0; i < mAlbumFiles.size(); i++) {
                File file = new File(mAlbumFiles.get(i).path);
                builder.addFormDataPart(Constant.OTHER_IMAGES, file.getName(), RequestBody.create(getMimeType(file), file));
            }

            RequestBody body = builder.build();

            Request request = new Request.Builder()
                    .url(Constant.MAIN_URL)
                    .method("POST", body)
                    .addHeader(Constant.AUTHORIZATION, "Bearer " + ApiConfig.createJWT("eKart", "eKart Authentication"))
                    .build();

            Response response = client.newCall(request).execute();

            JSONObject jsonObject = new JSONObject(response.body().string());
            if (!jsonObject.getBoolean(Constant.ERROR)) {
                if (!from.equals("product")) {
                    position = 0;
                    ProductListActivity.productArrayList.add(new Gson().fromJson(jsonObject.getJSONObject(Constant.DATA).toString(), Product.class));
                } else {
                    ProductListActivity.productArrayList.set(position, new Gson().fromJson(jsonObject.getJSONObject(Constant.DATA).toString(), Product.class));
                }
                ProductListActivity.mAdapter.notifyDataSetChanged();
                onBackPressed();
            }

            progressBar.setVisibility(View.GONE);
            Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // url = file path or whatever suitable URL you want.
    public static MediaType getMimeType(File file) {
        MediaType type = MediaType.parse("application/octet-stream");
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
        if (extension != null) {
            type = MediaType.parse(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
        }
        return type;
    }

    public class CategoryAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<Categories> categories;
        final LayoutInflater inflter;
        Spinner spinnerCategory, spinnerSubCategory;

        public CategoryAdapter(Context applicationContext, ArrayList<Categories> categories, Spinner spinnerCategory, Spinner spinnerSubCategory) {
            this.context = applicationContext;
            this.categories = categories;
            this.spinnerCategory = spinnerCategory;
            this.spinnerSubCategory = spinnerSubCategory;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setItem(String categoryId) {
            for (int i = 0; i < getCount(); i++) {
                if (categoryId.equals(categories.get(i).getId())) {
                    spinnerCategory.setSelection(i);
                }
            }
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView txtmeasurement = view.findViewById(R.id.txtmeasurement);

            Categories category = categories.get(position);
            txtmeasurement.setText(category.getName());

            spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    subCategoryAdapter = new SubCategoryAdapter(context, categories.get(i).getSubCategories(), spinnerSubCategory);
                    spinnerSubCategory.setAdapter(subCategoryAdapter);
                    subCategoryAdapter.setItem(product.getSubcategory_id() == null ? "0" : product.getSubcategory_id());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return view;
        }
    }

    public class LooseStockUnitAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<Unit> units;
        final LayoutInflater inflter;


        public LooseStockUnitAdapter(Context applicationContext, ArrayList<Unit> units) {
            this.context = applicationContext;
            this.units = units;


            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return units.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setItem(String id) {
            for (int i = 0; i < getCount(); i++) {
                if (id.equals(units.get(i).getId())) {
                    spinnerMeasurement.setSelection(i);
                }
            }
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.txtmeasurement);

            Unit unit = units.get(position);
            measurement.setText(unit.getName());

            spinnerMeasurement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        looseStockId = unit.getId();
                    } catch (Exception ignore) {

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            return view;
        }
    }

}