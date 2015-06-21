package com.tih.irdb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pichu on 西元15/6/18.
 */
public class InfraredCodeRecordAdapter extends ArrayAdapter<InfraredCodeRecord> {

    String tag = "InfraredCodeRecordAdapter";

    private List<InfraredCodeRecord> mObjects;
    private int mResource;
    private int mDropDownResource;

    private int mFieldId = 0;
    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<InfraredCodeRecord> recordList;


    public InfraredCodeRecordAdapter(Context context, int resource) {
        super(context, resource);
        init(context, resource, 0, new ArrayList<InfraredCodeRecord>());
    }

    public InfraredCodeRecordAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        init(context, resource, textViewResourceId, new ArrayList<InfraredCodeRecord>());

    }

    public InfraredCodeRecordAdapter(Context context, int resource, InfraredCodeRecord[] objects) {
        super(context, resource, objects);
        init(context, resource, 0, Arrays.asList(objects));
    }

    public InfraredCodeRecordAdapter(Context context, int resource, int textViewResourceId, InfraredCodeRecord[] objects) {
        super(context, resource, textViewResourceId, objects);

        init(context, resource, textViewResourceId, Arrays.asList(objects));
    }

    public InfraredCodeRecordAdapter(Context context, int resource, List<InfraredCodeRecord> objects) {
        super(context, resource, objects);
        init(context, resource, 0, objects);
    }

    public InfraredCodeRecordAdapter(Context context, int resource, int textViewResourceId, List<InfraredCodeRecord> objects) {
        super(context, resource, textViewResourceId, objects);
        init(context, resource, textViewResourceId, objects);
    }


    private void init(Context context, int resource, int textViewResourceId, List<InfraredCodeRecord> objects) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = mDropDownResource = resource;
        mObjects = objects;
        mFieldId = textViewResourceId;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(int position, View convertView, final ViewGroup parent,
                                        int resource) {
        View view;
        TextView text;

        ImageView mapBackgroud;
        final ImageView mapDot;

        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mFieldId);
            }



        } catch (ClassCastException e) {
            Log.e(tag, "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        final InfraredCodeRecord item = getItem(position);
        mapBackgroud = (ImageView) view.findViewById(R.id.mapBackgroud);
        mapBackgroud.setImageBitmap(MainScreenActivity.currentControllerPhoto);

        mapDot = (ImageView) view.findViewById(R.id.mapDot);

        Bitmap dotLayer;
        if(MainScreenActivity.aspectRatio > 1){
            // Landscape
            dotLayer = Bitmap.createBitmap(32, (int) (32 / MainScreenActivity.aspectRatio), Bitmap.Config.ARGB_8888);
        }else{
            // Portrait
            Log.d(tag,"aspectRatio: " + MainScreenActivity.aspectRatio );
            Log.d(tag,"width: " + (int) (32*MainScreenActivity.aspectRatio));
            dotLayer = Bitmap.createBitmap((int) (32*MainScreenActivity.aspectRatio),32, Bitmap.Config.ARGB_8888);
        }



        Canvas canvas = new Canvas(dotLayer);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2);

        canvas.drawCircle(item.getX() * dotLayer.getWidth(), item.getY() * dotLayer.getHeight(), 2, paint);

        mapDot.setImageBitmap(dotLayer);
        mapDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "Has Been Click~");
//                (new ButtonReviewDialogFragment()).show(mContext );
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                View dialogView = inflater.inflate(R.layout.button_review_dialog,null,false);

                builder.setMessage("測試")
                        .setView(dialogView)
                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                            }
                        });

                builder.show();


                ImageView ButtonReviewBackground =  (ImageView)dialogView.findViewById(R.id.button_review_background);
                ImageView ButtonReviewMapDot =  (ImageView)dialogView.findViewById(R.id.button_review_map_dot);
                ButtonReviewBackground.setImageBitmap(MainScreenActivity.currentControllerPhoto);

                Log.d(tag, "ButtonReviewBackground: " + ButtonReviewBackground);
                Log.d(tag, "ButtonReviewBackground.getWidth: " + ButtonReviewBackground.getWidth());
                Log.d(tag, "ButtonReviewBackground.getHeight: " + ButtonReviewBackground.getHeight());

                double ButtonReviewAspect = 1.0 * ButtonReviewBackground.getWidth() /
                        ButtonReviewBackground.getHeight();


                Log.d(tag,"BRA: " + ButtonReviewAspect);
                Bitmap dotLayer = Bitmap.createBitmap(MainScreenActivity.dotLayer.getWidth(),
                        MainScreenActivity.dotLayer.getHeight(), Bitmap.Config.ARGB_8888);



                Canvas canvas = new Canvas(dotLayer);
                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setStrokeWidth(2);

                canvas.drawCircle(item.getX() * dotLayer.getWidth(), item.getY() * dotLayer.getHeight(), 8, paint);

                ButtonReviewMapDot.setImageBitmap(dotLayer);






            }
        });



        if (item instanceof CharSequence) {
            text.setText((CharSequence)item);
        } else {
            text.setText(item.getPostionString());
        }

        return view;
    }
}
