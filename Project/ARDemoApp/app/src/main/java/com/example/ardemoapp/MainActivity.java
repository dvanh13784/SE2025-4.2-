package com.example.ardemoapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // ĐỔI IP SERVER CỦA BẠN Ở ĐÂY
    private static final String SERVER_URL = "http://192.168.1.11:3000";

    private ArFragment arFragment;
    private Button btnConnect;
    private String modelUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ux_fragment);

        btnConnect = findViewById(R.id.btnConnect);

        // Nhấn nút để lấy link model từ server
        btnConnect.setOnClickListener(v -> connectToServer());

        // Chạm mặt phẳng để đặt model
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (modelUrl.isEmpty()) {
                Toast.makeText(this,
                        "Chưa có model! Hãy nhấn nút để lấy model từ server.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            placeModel(anchorNode, modelUrl);
        });
    }

    private void connectToServer() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();

        Toast.makeText(this, "Đang kết nối đến server...", Toast.LENGTH_SHORT).show();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Lỗi kết nối server!",
                                Toast.LENGTH_LONG).show()
                );
                Log.e("SERVER_ERROR", "Kết nối thất bại", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "Server trả về lỗi: " + response.code(),
                                    Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                try {
                    String jsonResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonResponse);

                    modelUrl = jsonObject.getString("url");
                    Log.d("MODEL_URL", "Model URL: " + modelUrl);

                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "Đã lấy link model! Hãy chạm vào sàn để đặt model.",
                                    Toast.LENGTH_LONG).show()
                    );

                } catch (Exception e) {
                    Log.e("JSON_ERROR", "Lỗi parse JSON", e);
                }
            }
        });
    }

    private void placeModel(AnchorNode anchorNode, String url) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse(url)) // Tải trực tiếp từ URL
                .setRegistryId(url)
                .build()
                .thenAccept(renderable -> {
                    // Tạo Node để chứa model
                    TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                    node.setParent(anchorNode);
                    node.setRenderable(renderable);

                    // --- CHỈNH KÍCH THƯỚC (SCALE) TẠI ĐÂY ---
                    // Vì không dùng RenderableSource nên ta chỉnh scale trực tiếp lên Node
                    node.getScaleController().setMinScale(0.1f);
                    node.getScaleController().setMaxScale(2.0f);

                    // Đặt kích thước mặc định (ví dụ 0.5f là nhỏ bằng 1/2 kích thước thật)
                    node.setLocalScale(new com.google.ar.sceneform.math.Vector3(0.5f, 0.5f, 0.5f));

                    node.select();
                })
                .exceptionally(throwable -> {
                    // In lỗi ra Logcat để kiểm tra nếu không tải được
                    Log.e("AR_ERROR", "Không tải được model: " + throwable.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(this, "Lỗi tải model!", Toast.LENGTH_SHORT).show()
                    );
                    return null;
                });
    }
}
