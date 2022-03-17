package wrteam.multivendor.seller.helper;

import static wrteam.multivendor.seller.helper.AppController.getInstance;
import static wrteam.multivendor.seller.helper.AppController.isConnected;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.security.Key;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import wrteam.multivendor.seller.model.OrderTracker;

public class ApiConfig {

    public static boolean CheckValidation(String item, boolean isemailvalidation, boolean ismobvalidation) {
        if (item.length() == 0)
            return true;
        else if (isemailvalidation && (!android.util.Patterns.EMAIL_ADDRESS.matcher(item).matches()))
            return true;
        else
            return ismobvalidation && (item.length() < 10 || item.length() > 12);
    }

    @SuppressLint("DefaultLocale")
    public static String StringFormat(String number) {
        return String.format("%.2f", Double.parseDouble(number));
    }

    public static String maskEmailAddress(final String email) {
        final String mask = "*************************";
        final int at = email.indexOf("@");
        if (at > 2) {
            final int maskLen = Math.min(Math.max(at / 2, 2), 25);
            final int start = (at - maskLen) / 2;
            return email.substring(0, start) + mask.substring(0, maskLen) + email.substring(start + maskLen);
        }
        return email;
    }

    public static void ShowProgress(Activity activity, ProgressBar progressBar){
        progressBar.setVisibility(View.VISIBLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void HideProgress(Activity activity, ProgressBar progressBar){
        progressBar.setVisibility(View.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static String maskMobileNumber(String mobile) {
        final String mask = "*************************";
        mobile = mobile == null ? mask : mobile;
        final int lengthOfMobileNumber = mobile.length();
        if (lengthOfMobileNumber > 2) {
            final int maskLen = Math.min(Math.max(lengthOfMobileNumber / 2, 2), 10);
            final int start = (lengthOfMobileNumber - maskLen) / 2;
            return mobile.substring(0, start) + mask.substring(0, maskLen) + mobile.substring(start + maskLen);
        }
        return mobile;
    }

    public static OrderTracker OrderTracker(JSONObject jsonObject) {
        OrderTracker orderTracker = null;
        try {
            orderTracker = new Gson().fromJson(jsonObject.toString(), OrderTracker.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderTracker;
    }

    public static String VolleyErrorMessage(VolleyError error) {
        String message = "";
        try {
            if (error instanceof NetworkError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (error instanceof ServerError) {
                message = "The server could not be found. Please try again after some time!!";
            } else if (error instanceof AuthFailureError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again after some time!!";
            } else if (error instanceof TimeoutError) {
                message = "Connection TimeOut! Please check your internet connection.";
            } else
                message = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public static void RequestToVolley(final VolleyCallback callback, final Activity activity, final String url,
                                       final Map<String, String> params, final boolean isprogress) {
        try {
            final ProgressDisplay progressDisplay = new ProgressDisplay(activity);
            if (ProgressDisplay.mProgressBar != null) {
                ProgressDisplay.mProgressBar.setVisibility(View.GONE);
            }
            if (isConnected(activity)) {
                if (isprogress)
                    progressDisplay.showProgress();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
//                    System.out.println("================= " + url + " == " + response);
                    callback.onSuccess(true, response);
                    if (isprogress) {
                        progressDisplay.hideProgress();
                    }
                },
                        error -> {
                            if (isprogress) {
                                progressDisplay.hideProgress();
                            }
                            Toast.makeText(activity, error.toString(), Toast.LENGTH_LONG).show();

                            callback.onSuccess(false, "");
                            String message = VolleyErrorMessage(error);
                            if (!message.equals(""))
                                if (isprogress) {
                                    progressDisplay.hideProgress();
                                }
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        }) {


                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params1 = new HashMap<>();
                        params1.put("Authorization", "Bearer " + createJWT("eKart", "eKart Authentication"));
                        return params1;
                    }


                    @Override
                    protected Map<String, String> getParams() {
                        params.put(Constant.AccessKey, Constant.AccessKeyVal);
                        return params;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                getInstance().getRequestQueue().getCache().clear();
                getInstance().addToRequestQueue(stringRequest);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void RequestToVolley2(final VolleyCallback callback, final Activity activity, final String url, final Map<String, String> params, final Map<String, String> fileParams) {
        if (isConnected(activity)) {
            VolleyMultiPartRequest multipartRequest = new VolleyMultiPartRequest(url,
                    response -> callback.onSuccess(true, response),
                    error -> callback.onSuccess(false, "")) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params1 = new HashMap<>();
                    params1.put(Constant.AUTHORIZATION, "Bearer " + createJWT("eKart", "eKart Authentication"));
                    return params1;
                }

                @Override
                public Map<String, String> getDefaultParams() {
                    params.put(Constant.AccessKey, Constant.AccessKeyVal);
                    return params;
                }

                @Override
                public Map<String, String> getFileParams() {
                    return fileParams;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            getInstance().getRequestQueue().getCache().clear();
            getInstance().addToRequestQueue(multipartRequest);
        }
    }


    @Nullable
    public static String toTitleCase(@Nullable String str) {
        if (str == null) {
            return null;
        }
        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    @Nullable
    public static String createJWT(String issuer, String subject) {
        try {
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            byte[] apiKeySecretBytes = Constant.JWT_KEY.getBytes();
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
            JwtBuilder builder = Jwts.builder()
                    .setIssuedAt(now)
                    .setSubject(subject)
                    .setIssuer(issuer)
                    .signWith(signatureAlgorithm, signingKey);

            return builder.compact();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
