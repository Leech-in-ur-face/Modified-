package ru.nsu.ccfit.zuev.osu.online;

import android.os.Build;

import com.dgsrz.bancho.security.SecurityUtils;

import org.anddev.andengine.util.Debug;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class PostBuilder {
    private final FormBody.Builder formBodyBuilder = new FormBody.Builder();
    private final StringBuilder values = new StringBuilder();

    public void addParam(final String key, final String value) {
        if (values.length() > 0) {
            values.append("_");
        }
        formBodyBuilder.add(key, value);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            values.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        } else {
            try {
                values.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Debug.e(e);
            }
        }
    }

    public ArrayList<String> requestWithAttempts(final String scriptUrl, int attempts) throws RequestException {
        ArrayList<String> response = null;
        String signature = SecurityUtils.signRequest(values.toString());

        if (signature != null) {
            addParam("sign", signature);
        }
        for (int i = 0; i < attempts; i++) {
            try {
                response = request(scriptUrl);
            } catch (RequestException e) {
                if (e.getCause() instanceof UnknownHostException) {
                    Debug.e("Cannot resolve server name");
                    break;
                }
                Debug.e("Received error, continuing... ", e);
                response = null;
            }

            if (response == null || response.isEmpty() || response.get(0).length() == 0
                    || !(response.get(0).equals("FAIL") || response.get(0).equals("SUCCESS"))) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
                continue;
            }
            break;
        }

        if (response == null) response = new ArrayList<>();

        if (response.isEmpty()) {
            response.add("");
        }
        return response;
    }

    private ArrayList<String> request(final String scriptUrl) throws RequestException {
        ArrayList<String> response = new ArrayList<>();

        Request request = new Request.Builder()
            .url(scriptUrl)
            .post(formBodyBuilder.build())
            .build();

        try (Response resp = OnlineManager.client.newCall(request).execute()) {

            if (resp.isSuccessful() && resp.body() != null) {
                Debug.i("request url=" + scriptUrl);
                Debug.i("request --------Content---------");
                String line;
                BufferedReader reader = new BufferedReader(new StringReader(resp.body().string()));
                while ((line = reader.readLine()) != null) {
                    Debug.i(String.format("request [%d]: %s", response.size(), line));
                    response.add(line);
                }
                Debug.i("request url=" + scriptUrl);
                Debug.i("request -----End of content-----");
            }
        } catch(Exception e) {
            Debug.e(e.getMessage(), e);
        }

        if (response.isEmpty()) {
            response.add("");
        }
        return response;
    }

    public static class RequestException extends Exception {
        private static final long serialVersionUID = 671773899432746143L;

        public RequestException(final Throwable cause) {
            super(cause);
        }
    }
}
