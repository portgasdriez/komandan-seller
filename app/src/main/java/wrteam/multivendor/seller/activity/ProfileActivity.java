package wrteam.multivendor.seller.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wrteam.multivendor.seller.R;
import wrteam.multivendor.seller.adapter.PinCodeAdapter;
import wrteam.multivendor.seller.adapter.ProductImagesAdapter;
import wrteam.multivendor.seller.adapter.ProductItemAdapter;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.helpers.Constants;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.models.Image;
import wrteam.multivendor.seller.helper.ApiConfig;
import wrteam.multivendor.seller.helper.Constant;
import wrteam.multivendor.seller.helper.Session;
import wrteam.multivendor.seller.helper.Utils;
import wrteam.multivendor.seller.model.OrderStatus;
import wrteam.multivendor.seller.model.PinCode;

public class ProfileActivity extends AppCompatActivity {
    public ImageView imgProfile,imgKtp;
    public FloatingActionButton fabProfile,fabKtp;
    public ProgressBar progressBar;
    TextView tvChangePassword,titleSelectedPincodes;
    Session session;
    Button btnSubmit;
    Activity activity;
    EditText edtName, edtEmail, edtMobile, edtOldPassword, edtNewPassword, edtConfirmNewPassword;
    EditText edtStoreName,edStreet,edCity,edState,edBankName,edAccountNumber,edAccountHolderName,edStoreDescription,edLatitude,edLongitude;
    RecyclerView recyclerViewSelectedPinCodes;
    public static final int REQUEST_LOGO = 1000;
    public static final int REQUEST_KTP = 2000;
    ArrayList<String> pinCodesIds;
    ArrayList<PinCode> arrayListPinCode, arrayListSelectedPinCode;
    PinCodeAdapter pinCodeAdapter;
    Button lytSelectedPinCodes;
    String idPincode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile);
            activity = this;
            edtName = (EditText) findViewById(R.id.edtName);
            edtEmail = (EditText) findViewById(R.id.edtEmail);
            edtMobile = (EditText) findViewById(R.id.edtMobile);
            edtStoreName = (EditText) findViewById(R.id.edtStoreName);
            titleSelectedPincodes = (TextView) findViewById(R.id.titleSelectedPincodes);
            edStreet = (EditText) findViewById(R.id.edStreet);
            edCity = (EditText) findViewById(R.id.edCity);
            edState = (EditText) findViewById(R.id.edState);
            edBankName = (EditText) findViewById(R.id.edBankName);
            edAccountNumber = (EditText) findViewById(R.id.edAccountNumber);
            edAccountHolderName = (EditText) findViewById(R.id.edAccountHolderName);
            edStoreDescription = (EditText) findViewById(R.id.edStoreDescription);
            edLatitude = (EditText) findViewById(R.id.edLatitude);
            edLongitude = (EditText) findViewById(R.id.edLongitude);
            recyclerViewSelectedPinCodes = findViewById(R.id.recyclerViewSelectedPinCodes);
            fabKtp = (FloatingActionButton) findViewById(R.id.fabKtp);
            lytSelectedPinCodes = (Button)findViewById(R.id.lytSelectedPinCodes);
            pinCodesIds = new ArrayList<>();


            btnSubmit = (Button) findViewById(R.id.btnSubmit);
            tvChangePassword = (TextView) findViewById(R.id.tvChangePassword);
            fabProfile = (FloatingActionButton) findViewById(R.id.fabProfile);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);

            edtOldPassword = (EditText)findViewById(R.id.edtOldPassword);
            edtNewPassword = (EditText)findViewById(R.id.edtNewPassword);
            edtConfirmNewPassword = (EditText)findViewById(R.id.edtConfirmNewPassword);
            imgProfile = findViewById(R.id.imgProfile);
            imgKtp = findViewById(R.id.imgKtp);
            session = new Session(activity);
            GetPinCodes();
            getUserData(activity, session);
            recyclerViewSelectedPinCodes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            edtMobile.setText(session.getData(Constant.MOBILE));

        fabProfile.setOnClickListener(view -> {
            Intent intent = new Intent(activity, AlbumSelectActivity.class);
            //set limit on number of images that can be selected, default is 10
            intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1);
            startActivityForResult(intent, REQUEST_LOGO);
        });
        fabKtp.setOnClickListener(view -> {
            Intent intent = new Intent(activity, AlbumSelectActivity.class);
            //set limit on number of images that can be selected, default is 10
            intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1);
            startActivityForResult(intent, REQUEST_KTP);
        });



        tvChangePassword.setOnClickListener(v -> OpenBottomDialog(activity));
        btnSubmit.setOnClickListener(view -> {
            final String name = edtName.getText().toString();
            final String email = edtEmail.getText().toString();
            final String mobile = edtMobile.getText().toString();

//            if (ApiConfig.CheckValidation(name, false, false)) {
//                edtName.requestFocus();
//                edtName.setError("Masukkan Nama");
//            } else if (ApiConfig.CheckValidation(email, false, false)) {
//                edtEmail.requestFocus();
//                edtEmail.setError("Masukkan Email");
//            } else if (ApiConfig.CheckValidation(email, true, false)) {
//                edtEmail.requestFocus();
//                edtEmail.setError("Masukkan Email Valid");
//            } else {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.TYPE, Constant.EDIT_PROFILE);
                params.put(Constant.AccessKey, Constant.AccessKeyVal);
                params.put(Constant.USER_ID, "58");
                params.put(Constant.NAME, edtName.getText().toString());
                params.put(Constant.MOBILE, edtMobile.getText().toString());
                params.put(Constant.EMAIL, edtEmail.getText().toString());
                params.put(Constant.STORE_NAME, edtStoreName.getText().toString());
                params.put(Constant.PINCODES, idPincode);
                params.put(Constant.STREET, edStreet.getText().toString());
                params.put(Constant.CITY, edCity.getText().toString());
                params.put(Constant.STATE, edState.getText().toString());
                params.put(Constant.BANK_NAME, edBankName.getText().toString());
                params.put(Constant.ACCOUNT_NUMBER, edAccountNumber.getText().toString());
                params.put(Constant.BANK_ACCOUNT_NAME, edAccountHolderName.getText().toString());
                params.put(Constant.DESCRIPTION, edStoreDescription.getText().toString());
                params.put(Constant.LATITUDE, edLatitude.getText().toString());
                params.put(Constant.LONGITUDE, edLongitude.getText().toString());
                //System.out.println("====update res " + params.toString());
                ApiConfig.RequestToVolley((result, response) -> {
                    //System.out.println ("=================* " + response);
                    if (result) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
//                                session.setData(Constant.NAME, name);
//                                session.setData(Constant.EMAIL, email);
//                                session.setData(Constant.MOBILE, mobile);
                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            }
                            Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                }, activity, Constant.MAIN_URL, params, true);
//            }


        });
    }

    public void OpenBottomDialog(final Activity activity) {
        try {
            View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_change_password, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            EditText edtOldPassword = sheetView.findViewById(R.id.edtOldPassword);
            EditText edtNewPassword = sheetView.findViewById(R.id.edtNewPassword);
            EditText edtConfirmNewPassword = sheetView.findViewById(R.id.edtConfirmNewPassword);
            ImageView imgChangePasswordClose = sheetView.findViewById(R.id.imgChangePasswordClose);
            Button btnChangePassword = sheetView.findViewById(R.id.btnChangePassword);

            edtOldPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
            edtNewPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
            edtConfirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);

            Utils.setHideShowPassword(edtOldPassword);
            Utils.setHideShowPassword(edtNewPassword);
            Utils.setHideShowPassword(edtConfirmNewPassword);
            mBottomSheetDialog.setCancelable(true);


            imgChangePasswordClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

            btnChangePassword.setOnClickListener(view -> {
                String oldpsw = edtOldPassword.getText().toString();
                String password = edtNewPassword.getText().toString();
                String cpassword = edtConfirmNewPassword.getText().toString();

                if (!password.equals(cpassword)) {
                    edtConfirmNewPassword.requestFocus();
                    edtConfirmNewPassword.setError("Password Tidak Sama");
                } else if (ApiConfig.CheckValidation(oldpsw, false, false)) {
                    edtOldPassword.requestFocus();
                    edtOldPassword.setError("Masukkan Password Lama");
                } else if (ApiConfig.CheckValidation(password, false, false)) {
                    edtNewPassword.requestFocus();
                    edtNewPassword.setError("Masukkan Password Baru");
                } else if (!oldpsw.equals(new Session(activity).getData(Constant.PASSWORD))) {
                    edtOldPassword.requestFocus();
                    edtOldPassword.setError("Password tidak sama");
                } else {
                    ChangePassword(password);
                }
            });

            mBottomSheetDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void ChangePassword(String password) {

        final Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.CHANGE_PASSWORD);
        params.put(Constant.PASSWORD, password);
        params.put(Constant.USER_ID, session.getData(Constant.ID));

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setTitle("Ubah Password");
        alertDialog.setMessage("Anda yakin ingin mengubah password? setelah terganti akan akan di minta login kembali");
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(getString(R.string.yes), (dialog, which) -> ApiConfig.RequestToVolley((result, response) -> {
            //  System.out.println("=================*change_password " + response);
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        session.logoutUserConfirmation(activity);
                    }
                    Toast.makeText(activity, object.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, activity, Constant.REGISTER_URL, params, true));
        alertDialog.setNegativeButton(getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_LOGO) {
                ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                updateLogo(activity, images.get(0).path);
            }
            if (requestCode == REQUEST_KTP) {
                ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                updateKTP(activity, images.get(0).path);
            }
        }
    }

    public void updateLogo(Activity activity, String filePath) {
        Map<String, String> params = new HashMap<>();
        Map<String, String> fileParams = new HashMap<>();
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        fileParams.put(Constant.LOGO_PATH, filePath);
        params.put(Constant.TYPE, Constant.UPLOAD_LOGO);

        ApiConfig.RequestToVolley2((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {

                        Picasso.get()
                                .load(jsonObject.getString(Constant.LOGO))
                                .fit()
                                .centerInside()
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(imgProfile);
                    }
                    Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MAIN_URL, params, fileParams);
    }

    public void updateKTP(Activity activity, String filePath) {
        Map<String, String> params = new HashMap<>();
        Map<String, String> fileParams = new HashMap<>();
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        fileParams.put(Constant.KTP_PATH, filePath);
        params.put(Constant.TYPE, Constant.UPLOAD_KTP);

        ApiConfig.RequestToVolley2((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {

                        Picasso.get()
                                .load(jsonObject.getString(Constant.NATIONAL_IDENTITY_CARD))
                                .fit()
                                .centerInside()
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(imgProfile);
                    }
                    Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MAIN_URL, params, fileParams);
    }

    @SuppressLint("SetTextI18n")
    public void getUserData(final Activity activity, Session session) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.GET_USER_DATA, Constant.GetVal);
            params.put(Constant.USER_ID, "58");
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject_ = new JSONObject(response);
                        if (!jsonObject_.getBoolean(Constant.ERROR)) {
                            JSONObject jsonObject = jsonObject_.getJSONArray(Constant.DATA).getJSONObject(0);
                            edtName.setText(jsonObject.getString(Constant.NAME));
                            edtEmail.setText(jsonObject.getString(Constant.EMAIL));
                            edtMobile.setText(jsonObject.getString(Constant.MOBILE));
                            edtStoreName.setText(jsonObject.getString(Constant.STORE_NAME));
                            titleSelectedPincodes.setText(jsonObject.getString(Constant.PINCODES));
                            edStreet.setText(jsonObject.getString(Constant.STREET));
                            edCity.setText(jsonObject.getString(Constant.CITY));
                            edState.setText(jsonObject.getString(Constant.STATE));
                            edBankName.setText(jsonObject.getString(Constant.BANK_NAME));
                            edAccountNumber.setText(jsonObject.getString(Constant.ACCOUNT_NUMBER));
                            edAccountHolderName.setText(jsonObject.getString(Constant.BANK_ACCOUNT_NAME));
                            edStoreDescription.setText(jsonObject.getString(Constant.DESCRIPTION));
                            edLatitude.setText(jsonObject.getString(Constant.LATITUDE));
                            edLongitude.setText(jsonObject.getString(Constant.LONGITUDE));
                            idPincode = jsonObject.getString(Constant.PINCODE_ID);
                            Picasso.get()
                                .load(jsonObject.getString(Constant.LOGO))
                                .fit()
                                .centerInside()
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .into(imgProfile);

                            Picasso.get()
                                    .load(jsonObject.getString(Constant.NATIONAL_IDENTITY_CARD))
                                    .fit()
                                    .centerInside()
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(imgKtp);

                        }
                    } catch (JSONException e) {
                        Toast.makeText(this,"Ada kesalahan Koneksi, mohon coba beberapa saat lagi",Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.MAIN_URL, params, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        }


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
                        lytSelectedPinCodes.setOnClickListener(v -> {
                            AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
                            mBuilder.setTitle(activity.getString(R.string.selected_pincodes));
                            mBuilder.setMultiChoiceItems(listItems, checkedItems, (dialogInterface, position, isChecked) -> {
                                if (isChecked) {
                                    pinCodesIds.add(arrayListPinCode.get(position).getId());
                                    arrayListSelectedPinCode.add(arrayListPinCode.get(position));
                                    idPincode=arrayListPinCode.get(position).getId();
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
                                });


                    }
                    ApiConfig.HideProgress(activity, progressBar);
                } catch (JSONException e) {
                    ApiConfig.HideProgress(activity, progressBar);
                    e.printStackTrace();
                }
            }
        }, activity, Constant.MAIN_URL, params, false);
    }
}
