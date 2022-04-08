package com.example.kafkademo;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ARServiceHelper {

    public interface ARServiceCallback {
        public void onResponse(String response);

        public void onError(VolleyError error);
    }

    Context context;
    String url;
    static RequestQueue queue;
    JSONArray params;
    JSONObject params1;
    private ProgressDialog mProgressDialog;


    public ARServiceHelper(Context context, String url, JSONObject params,JSONArray param1) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        this.context = context;
        this.params1 = params;
        this.params = param1;

        this.url = url;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
    }

    public void Get_Standard_Request1(final ARServiceCallback arServiceCallback) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Method.GET, url, params1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        arServiceCallback.onResponse(response.toString());
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                arServiceCallback.onError(error);

                // TODO: Handle error

            }
        });

        queue.add(jsonObjectRequest);
    }

    public void Get_Simple_Request(final ARServiceCallback arServiceCallback) {
//        try {
//            mProgressDialog.show();
//        } catch (Exception e) {
//        }

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        arServiceCallback.onResponse(response.toString());
                       // mProgressDialog.dismiss();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                arServiceCallback.onError(error);
               // mProgressDialog.dismiss();

            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

//    public void Get_Standard_Request(final ARServiceCallback arServiceCallback) {
//        try {
//            mProgressDialog.show();
//        } catch (Exception e) {
//        }
//        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Method.GET, url, params,
//                new Response.Listener<JSONArray>() {
//
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        mProgressDialog.dismiss();
//                        arServiceCallback.onResponse(response.toString());
//                    }
//
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                mProgressDialog.dismiss();
//                arServiceCallback.onError(error);
//
//                // TODO: Handle error
//
//            }
//        });
//
//        queue.add(jsonObjectRequest);
//    }

    public void Post_Standard_Request(final ARServiceCallback arServiceCallback) {
       // mProgressDialog.show();
        JsonObjectRequest req = new JsonObjectRequest(Method.POST,url, params1,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       // mProgressDialog.dismiss();
                        Log.wtf("Respons", response+"");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
             //   mProgressDialog.dismiss();

                Log.wtf("Error: ", error);
            }

        }

        );

        queue.add(req);

    }

//    public void Post_Standard_Request1(final ARServiceCallback arServiceCallback) {
//        mProgressDialog.show();
//        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Method.POST, url, params,
//                new Response.Listener<JSONArray>() {
//
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        mProgressDialog.dismiss();
//                        Log.wtf("Respons", response+"");
//
//
//                        //arServiceCallback.onResponse(response.toString());
//                    }
//
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                mProgressDialog.dismiss();
//               // arServiceCallback.onError(error);
//                Log.wtf("Error: ", error);
//
//                // TODO: Handle error
//
//            }
//        });
//
//        queue.add(jsonObjectRequest);
//
//    }

    }



