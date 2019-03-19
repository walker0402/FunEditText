package walker.com.funedittext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FunEditText a = findViewById(R.id.a);
        a.setTextChangeListener(new FunEditText.OnTextChangeListener() {
            @Override
            public void onTextChanged(CharSequence text) {
                Log.e("walker", "change" + text);
            }

            @Override
            public void onInputFinish() {
                Log.e("walker", "onInputFinish");
            }
        });
    }
}
