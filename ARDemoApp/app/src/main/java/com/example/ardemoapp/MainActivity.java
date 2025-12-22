package com.example.ardemoapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_BASE_URL = "http://192.168.100.69:3000";
    private static final String MODELS_ENDPOINT = SERVER_BASE_URL + "/api/models";

    private ArFragment arFragment;
    private Button btnConnect;
    private Spinner spinnerModels;

    private boolean isModelReady = false;
    private String selectedModelUrl = null;
    private final List<ModelItem> modelItems = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ux_fragment);

        btnConnect = findViewById(R.id.btnConnect);
        spinnerModels = findViewById(R.id.spinnerModels);

        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModels.setAdapter(spinnerAdapter);

        btnConnect.setOnClickListener(v -> {
            loadModelList();
        });

        spinnerModels.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position >= 0 && position < modelItems.size()) {
                    selectedModelUrl = modelItems.get(position).url;
                    isModelReady = true;
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedModelUrl = null;
                isModelReady = false;
            }
        });

        loadModelList();

        // Chạm mặt phẳng để đặt model
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (!isModelReady) {
                Toast.makeText(this, "Chưa sẵn sàng...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedModelUrl == null) {
                Toast.makeText(this, "Hãy chọn một model trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            // Gọi hàm tải và đặt model
            placeModel(anchorNode, selectedModelUrl);
        });
    }

    private void loadModelList() {
        Request request = new Request.Builder()
                .url(MODELS_ENDPOINT)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Không tải được danh sách model", Toast.LENGTH_LONG).show());
                Log.e("MODEL_LIST", "Lỗi tải danh sách", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Server trả về lỗi", Toast.LENGTH_LONG).show());
                    return;
                }

                final String body = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(body);
                    JSONArray modelsArray = jsonObject.getJSONArray("models");

                    List<ModelItem> loadedItems = new ArrayList<>();
                    List<String> names = new ArrayList<>();

                    for (int i = 0; i < modelsArray.length(); i++) {
                        JSONObject item = modelsArray.getJSONObject(i);
                        String name = item.getString("name");
                        String url = item.getString("url");
                        loadedItems.add(new ModelItem(name, url));
                        names.add(name);
                    }

                    runOnUiThread(() -> {
                        modelItems.clear();
                        modelItems.addAll(loadedItems);

                        spinnerAdapter.clear();
                        spinnerAdapter.addAll(names);
                        spinnerAdapter.notifyDataSetChanged();

                        if (!modelItems.isEmpty()) {
                            spinnerModels.setSelection(0);
                            selectedModelUrl = modelItems.get(0).url;
                            isModelReady = true;
                        } else {
                            selectedModelUrl = null;
                            isModelReady = false;
                            Toast.makeText(MainActivity.this, "Chưa có model nào, hãy upload lên server", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    Log.e("MODEL_LIST", "Sai định dạng JSON", e);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Sai định dạng dữ liệu từ server", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    // Hàm này giữ nguyên logic, chỉ sửa lại thông báo lỗi rõ hơn
    private void placeModel(AnchorNode anchorNode, String url) {
        Toast.makeText(this, "Đang tải model về...", Toast.LENGTH_SHORT).show();

        ModelRenderable.builder()
                .setSource(this, Uri.parse(url))
                .setIsFilamentGltf(true) // 👇 QUAN TRỌNG: Thêm dòng này nếu dùng bản Sceneform mới (hỗ trợ GLB tốt hơn)
                .setRegistryId(url)
                .build()
                .thenAccept(renderable -> {
                    Toast.makeText(this, "Tải xong! Đang hiển thị...", Toast.LENGTH_SHORT).show();

                    TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                    node.setParent(anchorNode);
                    node.setRenderable(renderable);

                    // Chỉnh kích thước
                    node.getScaleController().setMinScale(0.1f);
                    node.getScaleController().setMaxScale(2.0f);
                    node.setLocalScale(new com.google.ar.sceneform.math.Vector3(0.5f, 0.5f, 0.5f));

                    node.select();
                })
                .exceptionally(throwable -> {
                    Log.e("AR_ERROR", "Không tải được model: " + throwable.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(this, "Lỗi: Không tải được file model.glb!", Toast.LENGTH_LONG).show()
                    );
                    return null;
                });
    }

    private static class ModelItem {
        final String name;
        final String url;

        ModelItem(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}