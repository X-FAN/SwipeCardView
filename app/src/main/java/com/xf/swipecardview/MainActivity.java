package com.xf.swipecardview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xf.swipecardview.bean.Card;
import com.xf.swipecardview.lib.SwipeCardView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SwipeCardView swipeCardView = (SwipeCardView) findViewById(R.id.swipe_card_view);
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(0));
        cards.add(new Card(1));
        cards.add(new Card(2));
        cards.add(new Card(3));
        cards.add(new Card(4));
        cards.add(new Card(5));

        swipeCardView.setOnTopClickListener(new SwipeCardView.OnTopClickListener() {
            @Override
            public void onTopClickListener(View view) {
                Toast.makeText(MainActivity.this, "OnClick", Toast.LENGTH_SHORT).show();
            }
        });


        swipeCardView.setBindDataListener(new SwipeCardView.BindData<Card>() {
            @Override
            public void bindData(View view, Card data) {
                if (i % 2 == 0) {
                    view.setBackgroundResource(R.color.blue);
                } else {
                    view.setBackgroundResource(R.color.light_blue);
                }
                i++;
                TextView tv = (TextView) view.findViewById(R.id.index);
                tv.setText(data.getIndex() + "");
            }
        });

        swipeCardView.initSwipeCard(R.layout.item_add_car, cards);
    }
}
