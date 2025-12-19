package com.example.arddemoapp.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arddemoapp.R;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private Uri modelUri;

    // EMULATOR: dùng 10.0.2.2. Nếu chạy máy thật, đổi thành IP máy tính.
    private static final String BASE_URL = "http://192.168.100.54:3000/";


    // --- Retrofit ---
    interface Api {
        @GET("api/models/{id}")
        Call<ModelResponse> getModel(@Path("id") String id);
    }
    static class ModelResponse { public String id; public String url; public String title; }

    private Api api() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(log).build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(Api.class);
    }
    // ----------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // 1) Lấy URL model từ server
        api().getModel("tree").enqueue(new Callback<ModelResponse>() {
            @Override public void onResponse(Call<ModelResponse> call, Response<ModelResponse> resp) {
                if (resp.isSuccessful() && resp.body()!=null) {
                    modelUri = Uri.parse(resp.body().url);
                    Toast.makeText(MainActivity.this, "Model sẵn sàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Không lấy được URL model", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<ModelResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // 2) Chạm mặt phẳng để đặt model
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (modelUri == null) {
                Toast.makeText(this, "Đang tải model...", Toast.LENGTH_SHORT).show();
                return;
            }
            placeModel(hitResult, modelUri);
        });
    }

    private void placeModel(HitResult hitResult, Uri uri) {
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // ⬇️ Không dùng RenderableSource. Load thẳng GLB từ URL:
        ModelRenderable.builder()
                .setSource(this, uri)               // GLB/GLTF URL
                // .setIsFilamentGltf(true)         // Nếu Android Studio gợi ý, có thể bật dòng này; nếu không có method thì bỏ
                .setRegistryId(uri.toString())      // cache theo URL
                .build()
                .thenAccept(renderable -> {
                    TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                    node.setParent(anchorNode);
                    node.setRenderable(renderable);
                    node.select();
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Load model lỗi: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    return null;
                });
    }
}
