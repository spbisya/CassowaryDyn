package com.okunev.cassowarydyn;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import no.agens.cassowarylayout.CassowaryLayout;
import no.agens.cassowarylayout.ViewIdResolver;

/**
 * Created by gwa on 2/19/16.
 */
public class BuyTicket extends AppCompatActivity {
    private int k;
    private CassowaryLayout cassowaryLayout;
    ArrayList<String> parents = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    HashMap<Integer, HashMap<Integer, HashMap<String, String>>> allElements =
            new HashMap<>();
    ArrayList<String> CONSTRAINTS = new ArrayList<>();
    ArrayList<TextView> views = new ArrayList<>();
    HashMap<String, TextView> textViewHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cassowaryLayout = new CassowaryLayout(this, viewIdResolver);
        cassowaryLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        k = getIntent().getIntExtra("game", 1);
        int id = 1000;
        try {
            firstScreens("buyTicket");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        String k = "";
        Random rand = new Random();
        for (String name : names) {
            TextView tv = new TextView(this);
            tv.setText(name);
            tv.setId(id++);
            tv.setBackgroundColor(Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            tv.setTextColor(Color.RED);
            views.add(tv);
            cassowaryLayout.addView(tv);
            textViewHashMap.put(name, tv);
            k += name + "\n";
        }
        String[] newc = new String[CONSTRAINTS.size()];
        for (int i = 0; i < CONSTRAINTS.size(); i++) {
            newc[i] = CONSTRAINTS.get(i);

        }
        cassowaryLayout.setupSolverAsync(newc);
        setContentView(cassowaryLayout);
        Toast.makeText(this, k, Toast.LENGTH_LONG).show();
    }

    private ViewIdResolver viewIdResolver = new ViewIdResolver() {

        @Override
        public int getViewIdByName(String viewName) {
            return textViewHashMap.get(viewName).getId();
        }

        @Override
        public String getViewNameById(int id) {
            return views.get(id - 1000).getText().toString();
        }
    };

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("lotteries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void firstScreens(String screen) {
        String jsonData = loadJSONFromAsset();
        try {
            JSONArray games = new JSONArray(jsonData);
            JSONObject firstgame = games.getJSONObject(k);//В теории, для других игр изменяем 0 на нужное число.
            //А потом молимся, чтобы ticketDataTemp не был null
            JSONObject ticketDataTemp = firstgame.getJSONObject("ticketDataTemp");
            JSONObject buyTicket = ticketDataTemp.getJSONObject(screen);//ticketDataTemp.getJSONObject("buyTicket");
            JSONArray elements = buyTicket.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject layout = elements.getJSONObject(i);
                JSONArray constraints_json = layout.getJSONArray("constraints");
                parents.add(layout.get("parent_name").toString());
                names.add(layout.get("name").toString());
                HashMap<Integer, HashMap<String, String>> constrains0 = new HashMap<>();
                for (int k = 0; k < constraints_json.length(); k++) {
                    JSONObject constrK = constraints_json.getJSONObject(k);
                    HashMap<String, String> constrains = new HashMap<>();
                    constrains.put("attribute1", constrK.get("attribute1").toString());
                    constrains.put("attribute2", constrK.get("attribute2").toString());
                    constrains.put("constant", constrK.get("constant").toString());
                    constrains.put("from_object", constrK.get("from_object").toString());
                    constrains.put("multiplier", constrK.get("multiplier").toString());
                    constrains.put("offset", constrK.get("offset").toString());
                    constrains.put("relation", constrK.get("relation").toString());
                    constrains.put("to_object", constrK.get("to_object").toString());
                    constrains.put("type", constrK.get("type").toString());
                    constrains0.put(k, constrains);
                }
                allElements.put(i, constrains0);
            }
        } catch (Exception l) {
            Toast.makeText(this, l.getMessage(), Toast.LENGTH_LONG).show();
        }
        for (int i = 0; i < allElements.size(); i++) {
            ConstraintsResolver constraintsResolver = new ConstraintsResolver(allElements.get(i), parents.get(i), names.get(i));
            ArrayList<String> constr = constraintsResolver.resolve();
            for (String s : constr)
                CONSTRAINTS.add(s);
        }
    }
}
