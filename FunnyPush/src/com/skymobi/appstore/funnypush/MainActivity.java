
package com.skymobi.appstore.funnypush;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements
        OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_show).setOnClickListener(this);
        findViewById(R.id.btn_hide).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // if (v.getId() == R.id.btn_show) {
        // String strDepth = ((EditText)
        // findViewById(R.id.edit_depth)).getText().toString();
        // int d = 0;
        // try {
        // d = Integer.parseInt(strDepth);
        // } catch (Exception e) {
        // }
        // ConfigUtil.setDepthZ(d);
        // ConfigUtil.setTestTurn(((CheckBox)
        // findViewById(R.id.ckBox)).isChecked());
        // }
        Toast.makeText(this, "操作已生效，请返回桌面观看效果", Toast.LENGTH_SHORT)
                .show();
        startService(new Intent(this, FloatWindowService.class)
                .putExtra(FloatWindowService.OPERATION,
                        (v.getId() == R.id.btn_show)
                                ? FloatWindowService.OPERATION_SHOW
                                : FloatWindowService.OPERATION_HIDE));
    }
}
