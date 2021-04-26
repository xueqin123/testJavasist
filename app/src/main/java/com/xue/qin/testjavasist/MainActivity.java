package com.xue.qin.testjavasist;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xue.qin.common.TimeStamp;
import com.xue.qin.testjavasist.t.TestBean;

public class MainActivity extends Activity implements View.OnClickListener {
    private Button button;
    private TextView textView;
    private TestBean testBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testBean = new TestBean();
        button = findViewById(R.id.button);
        textView = findViewById(R.id.text);
        button.setOnClickListener(this);
    }

    @TimeStamp
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                textView.setText(testBean.getTitle());
                break;
            default:
                break;
        }
    }
}