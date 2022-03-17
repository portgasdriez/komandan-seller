package wrteam.multivendor.seller.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;

import wrteam.multivendor.seller.R;
import wrteam.multivendor.seller.activity.LoginActivity;

public class Session {

    public static final String PREFER_NAME = "eCart_Admin_App";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;


    public Session(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createUserLoginSession(String id, String fcmId, String user, String store_name,
                                       String email, String password,String balance, String customer_privacy,
                                       String logo, String view_order_otp, String assign_delivery_boy,
                                       String status,String mobile) {
        editor.putBoolean(Constant.IS_USER_LOGIN, true);
        editor.putString(Constant.FCM_ID, fcmId);
        editor.putString(Constant.ID, id);
        editor.putString(Constant.NAME, user);
        editor.putString(Constant.STORE_NAME, store_name);
        editor.putString(Constant.EMAIL, email);
        editor.putString(Constant.PASSWORD, password);
        editor.putString(Constant.BALANCE, balance);
        editor.putString(Constant.CUSTOMER_PRIVACY, customer_privacy);
        editor.putString(Constant.LOGO, logo);
        editor.putString(Constant.VIEW_ORDER_OTP, view_order_otp);
        editor.putString(Constant.ASSIGN_DELIVERY_BOY, assign_delivery_boy);
        editor.putString(Constant.STATUS, status);
        editor.putString(Constant.MOBILE,mobile);
        editor.commit();
    }


    public void createProfileSession(String seller_id,
                                     String name,
                                     String email,
                                     String country_code,
                                     String logo,
                                     String mobile,
                                     String balance,
                                     String fcmId,
                                     String is_premium,
                                     String street,
                                     String city,
                                     String state,
                                     String bank_name,
                                     String account_number,
                                     String bank_account_name,
                                     String latitude,
                                     String longitude,
                                     String national_identity,
                                     String store_name,
                                     String pincode,
                                     String description) {

        editor.putString(Constant.USER_ID, seller_id);
        editor.putString(Constant.NAME, name);
        editor.putString(Constant.EMAIL, email);
        editor.putString(Constant.COUNTRY_CODE, country_code);
        editor.putString(Constant.LOGO, logo);
        editor.putString(Constant.MOBILE, mobile);
        editor.putString(Constant.BALANCE, balance);
        editor.putString(Constant.FCM_ID, fcmId);
        editor.putString(Constant.IS_PREMIUM, is_premium);
        editor.putString(Constant.STREET, street);
        editor.putString(Constant.CITY, city);
        editor.putString(Constant.STATE, state);
        editor.putString(Constant.BANK_NAME, bank_name);
        editor.putString(Constant.ACCOUNT_NUMBER, account_number);
        editor.putString(Constant.BANK_ACCOUNT_NAME, bank_account_name);
        editor.putString(Constant.LATITUDE, latitude);
        editor.putString(Constant.LONGITUDE, longitude);
        editor.putString(Constant.NATIONAL_IDENTITY_CARD, national_identity);
        editor.putString(Constant.STORE_NAME, store_name);
        editor.putString(Constant.PINCODES, pincode);
        editor.putString(Constant.DESCRIPTION, description);

        editor.commit();
    }

    public String getData(String id) {
        return pref.getString(id, "");
    }

    public void setData(String id, String val) {
        editor.putString(id, val);
        editor.commit();
    }

    public boolean getReadMark(String id) {
        return pref.getBoolean(id, false);
    }

    public void setReadMark(String id, boolean val) {
        editor.putBoolean(id, val);
        editor.commit();
    }

    public String getCoordinates(String id) {
        return pref.getString(id, "0");
    }

    public boolean isUserLoggedIn() {
        return pref.getBoolean(Constant.IS_USER_LOGIN, false);
    }


    public void logoutUserConfirmation(final Activity activity) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(_context);
        // Setting Dialog Message
        alertDialog.setTitle(R.string.logout);
        alertDialog.setMessage(R.string.logout_msg);
        alertDialog.setCancelable(false);
        alertDialog.setIcon(R.drawable.ic_logout);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                editor.clear();
                editor.commit();

                Intent i = new Intent(activity, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(i);
                activity.finish();
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog1.dismiss();
            }
        });
        // Showing Alert Message
        alertDialog.show();

    }
}
