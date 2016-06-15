package ch.supsi.dti.e_missionconsumes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(this.getClass().getName(), "OBD Start");
        Intent intent = new Intent(this, OBDActivity.class);
        startActivity(intent);
    }
}
