package com.example.ardemoapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
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
            placedNodes.add(anchorNode);

            placeModel(anchorNode, selectedModelUrl);
        });
    }

    private void setupControls() {
        scaleSeekBar.setMax(150); // 0 -> 1.5
        rotationSeekBar.setMax(360);

        scaleSeekBar.setProgress((int) (currentScale * 100));
        rotationSeekBar.setProgress((int) (currentRotationDegrees + 180));

        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentScale = Math.max(0.1f, progress / 100f);
                updateLabels();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        rotationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentRotationDegrees = progress - 180;
                updateLabels();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        updateLabels();
    }

    private void updateLabels() {
        scaleLabel.setText(String.format(Locale.getDefault(), "Scale: %.2fx", currentScale));
        rotationLabel.setText(String.format(Locale.getDefault(), "Xoay: %.0f°", currentRotationDegrees));
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

    private void preloadSelectedModel() {
        if (selectedModelUrl == null) {
            Toast.makeText(this, "Chọn model trước", Toast.LENGTH_SHORT).show();
            return;
        }

        if (renderableCache.containsKey(selectedModelUrl)) {
            Toast.makeText(this, "Model đã sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang tải trước model...", Toast.LENGTH_SHORT).show();
        buildRenderable(selectedModelUrl, null);
    }

    private void placeModel(AnchorNode anchorNode, String url) {
        Toast.makeText(this, "Đang tải model về...", Toast.LENGTH_SHORT).show();

        ModelRenderable cached = renderableCache.get(url);
        if (cached != null) {
            attachNode(anchorNode, cached);
            return;
        }

        buildRenderable(url, anchorNode);
    }

    private void buildRenderable(String url, AnchorNode anchorNodeIfAny) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse(url))
                .setIsFilamentGltf(true)
                .setRegistryId(url)
                .build()
                .thenAccept(renderable -> {
                    renderableCache.put(url, renderable);
                    runOnUiThread(() -> {
                        if (anchorNodeIfAny != null) {
                            attachNode(anchorNodeIfAny, renderable);
                        } else {
                            Toast.makeText(MainActivity.this, "Model đã sẵn sàng!", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Log.e("AR_ERROR", "Không tải được model: " + throwable.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Lỗi: Không tải được file model!", Toast.LENGTH_LONG).show()
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
