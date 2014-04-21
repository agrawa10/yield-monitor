package com.example.yieldmonitor;

import java.util.ArrayList;
import java.lang.String;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.TextView;
import android.widget.PopupWindow;
import android.widget.Toast;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap.*;

import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

public class MainActivity extends FragmentActivity {
    
    static final double EARTH_RADIUS = 6371009;
	
    GoogleMap googleMap;
    ArrayList<LatLng> points; //Vertices of the polygon to be plotted
    ArrayList<Polyline> polylines; //Lines of the polygon (required for clearing)
    Marker iMarker, fMarker, inMarker; //Initial and final markers
    boolean longClickClear;
    PolylineOptions previewPolylineOptions;
    Polyline previewPolyline;
    LatLng overlayLocation = new LatLng(40.432923, -86.918481);
    TextView AreaTextView;
    //Bitmap vMap = BitmapFactory.decodeResource(getResources(), R.drawable.w_overlay);
    //Projection projection = googleMap.getProjection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        points = new ArrayList<LatLng>();
        polylines = new ArrayList<Polyline>();
        //GroundOverlayOptions overlay = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.yield_overlay)).position(overlayLocation, 860f, 650f).transparency(0.5f);
        GroundOverlayOptions overlay = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.w_overlay)).anchor(0, 0)
        		.position(overlayLocation, 860f, 650f);
        
        //Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = fm.getMap(); //Getting GoogleMap object from the fragment    
        googleMap.addGroundOverlay(overlay);
        googleMap.setMyLocationEnabled(true); //Enabling MyLocation Layer of Google Map
        
  
        googleMap.setOnMapClickListener(new OnMapClickListener() { //Setting OnClick event listener for the Google Map
            @Override
            public void onMapClick(LatLng point) {
            	if (points.size() == 0) {
            		// Instantiating the class MarkerOptions to plot marker on the map
            		MarkerOptions iMarkerOptions = new MarkerOptions(); MarkerOptions fMarkerOptions = new MarkerOptions();
            		
            		Toast.makeText(getApplicationContext(), "Press and hold data marker to drag.", Toast.LENGTH_SHORT).show();

            		// Setting latitude and longitude of the marker position
            		iMarkerOptions.position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));
        			fMarkerOptions.position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));

            		// Setting title of the infowindow of the marker
            		iMarkerOptions.title("Position"); fMarkerOptions.title("Position");

            		// Setting the content of the infowindow of the marker
            		iMarkerOptions.snippet("Latitude:"+point.latitude+","+"Longitude:"+point.longitude); 
            		fMarkerOptions.snippet("Latitude:"+point.latitude+","+"Longitude:"+point.longitude);

            		// Adding the tapped point to the ArrayList
            		points.add(point);

            		// Adding the marker to the map
            		iMarker = googleMap.addMarker(iMarkerOptions); fMarker = googleMap.addMarker(fMarkerOptions);
            		iMarker.setDraggable(false); fMarker.setDraggable(true);
            		longClickClear = false;
            	}
            	else if (points.size() > 0) {
            		if (inMarker != null) {
            			inMarker.remove();
            		}
            		MarkerOptions inMarkerOptions = new MarkerOptions();
            		inMarkerOptions.position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));
            		boolean inside = PolyUtil.containsLocation(point, points, true);
            		Toast.makeText(getApplicationContext(), String.format("%s", Boolean.toString(inside)), Toast.LENGTH_SHORT).show();
            		inMarker = googleMap.addMarker(inMarkerOptions);
            	}
            }
        });
        
        googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                if (longClickClear == true) {
                	//Clearing the markers and polylines in the google map
                	iMarker.remove(); fMarker.remove();
                	for(Polyline line: polylines) {
                		line.remove();
                	}
                	polylines.clear();
                	Toast.makeText(getApplicationContext(), "Polygon cleared. Tap to create another marker.", Toast.LENGTH_SHORT).show();

                	// Empty the array list
                	points.clear();
                }
            }
        });
        
        googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
            public boolean onMarkerClick(Marker marker) {
			    if (marker.equals(iMarker)) { //Initial marker clicked
			        points.add(marker.getPosition()); //Current position of movable marker added to list 
				    //Polyline added to map and to ArrayList polylines
				    polylines.add(googleMap.addPolyline(new PolylineOptions().color(Color.BLUE).width(3).addAll(points)));
				    Toast.makeText(getApplicationContext(), "Long press to clear polygon.", Toast.LENGTH_SHORT).show();
			        longClickClear = true;
			    }
			    return true;
		    }
        });
        
        googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
        	@Override
        	public void onMarkerDragEnd(Marker marker) {
        		previewPolyline.remove();
            	LatLng curr = marker.getPosition(); //Gets current marker position
            	points.add(curr);
            	//Polyline added to map and to ArrayList polylines
            	polylines.add(googleMap.addPolyline(new PolylineOptions().color(Color.RED).width(3).addAll(points)));
        	}

			@Override
			public void onMarkerDrag(Marker marker) {
				LatLng curr;
				if (previewPolyline == null) {
					curr = marker.getPosition();
					previewPolylineOptions = new PolylineOptions().color(Color.RED).width(3).add(points.get(points.size() - 1), curr);
					previewPolyline = googleMap.addPolyline(previewPolylineOptions);
				}
				else {
					previewPolyline.remove();
					curr = marker.getPosition();
					previewPolylineOptions = new PolylineOptions().color(Color.RED).width(3).add(points.get(points.size() - 1), curr);
					previewPolyline = googleMap.addPolyline(previewPolylineOptions);
				}
			}

			@Override
			public void onMarkerDragStart(Marker marker) {
			}
        });
        
        googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(getApplicationContext(), "Tap to create marker.", Toast.LENGTH_SHORT).show();;
                return false;
            }
        
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Inflate the menu; this adds items to the action bar if it is present.*/
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_undo:
    	        if (polylines.size() > 0) {
    		        polylines.remove(polylines.size() - 1).remove(); //Removes element from array and corresponding polyline from map
    	            points.remove(points.size() - 1);
    	            fMarker.setPosition(points.get(points.size() - 1));
    	        } else if (polylines.size() == 0) {
    		        if (points.size() > 0) {
    		            points.remove(points.size() - 1);
                        iMarker.remove(); fMarker.remove();
    	            }
    	        }
    	        break;
    	  
            case R.id.action_area:
    	        double area = SphericalUtil.computeArea(points);
    	        if (area != 0 && points.get(0).equals(points.get(points.size() - 1))) {
    	        	makePopupView(this, String.format("Area: %.2e sq meters", area));
    	        } else {
    		         Toast.makeText(this, "Complete polygon to compute area.", Toast.LENGTH_SHORT).show();
    	        }
    	        break;
    	        
            case R.id.action_average:
            	final Bitmap vMap = BitmapFactory.decodeResource(getResources(), R.drawable.w_overlay);
            	final int height = vMap.getHeight();
            	final int width = vMap.getWidth();
            	final Projection projection = googleMap.getProjection();
            	final Point point = projection.toScreenLocation(overlayLocation);
            	final Handler mHandler = new Handler();
            	
            	final PopupWindow interimPopup = makePopupView(this, "Calculating...");
            	
            	//Fire off a thread to calculate average
            	Thread t = new Thread() {
            		public void run() {
                    	int[] pixels = new int[height * width];
                    	vMap.getPixels(pixels, 0, width, 0, 0, width, height);
            			double sum = 0;
                    	for (int y = 0; y < height; y++) {
                    		point.y = y;
                			for (int x = 0; x < width; x++) {
                    			point.x = x;
                    			LatLng position = projection.fromScreenLocation(point);
                    			if (PolyUtil.containsLocation(position, points, true)) {
                      				int index = y * width + x;
                      			    int R = (pixels[index] >> 16) & 0xff;
                      			    int G = (pixels[index] >> 8) & 0xff;
                      			    int B = pixels[index] & 0xff;
                      			    sum += (R + G + B);
                      			    if (sum != 0) {
                      			    	MarkerOptions inMarkerOptions = new MarkerOptions();
                                		inMarkerOptions.position(position);
                                		inMarker = googleMap.addMarker(inMarkerOptions);
                      			    }
                      			    pixels[index] = 0xff000000 | (R << 16) | (G << 8)| B;
                      			}
                    		}
                    	}
                    	final double f_sum = sum;
                    	mHandler.post(new Runnable() {
                    		public void run() {
                        		//makePopupView(this, String.format("Average: %.2e sq meters", f_sum / (width * height)));
                    			LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    			View popupView = layoutInflater.inflate(R.layout.popup, null);
                    			final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    			popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
                    			
                    			AreaTextView = (TextView) popupView.findViewById(R.id.areaValue);
                    			AreaTextView.setText(String.format("Average: %.2e sq meters", f_sum / (width * height)));
                    			
                    			Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                    	    	btnDismiss.setOnClickListener(new Button.OnClickListener() {
                    	    		public void onClick(View v) {
                    	    			popupWindow.dismiss();
                    	    		}
                    	    	});
                    	    	
                    	    	interimPopup.dismiss();
                    		}
                    	});
            		}
            	};
            	t.start();
            	break;
            	
            case R.id.action_settings: // action with ID action_settings was selected
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                break;
        
            default:
                break;
        }
        return true;
    }
    
    private PopupWindow makePopupView(Activity context, String string) {
    	//Inflate popup.xml
    	LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
    	View popupView = layoutInflater.inflate(R.layout.popup, null);
    	final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
    	
    	AreaTextView = (TextView) popupView.findViewById(R.id.areaValue);
    	AreaTextView.setText(string);
    	
    	Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
    	btnDismiss.setOnClickListener(new Button.OnClickListener() {
    		public void onClick(View v) {
    			popupWindow.dismiss();
    		}
    	});
    	return popupWindow;
    }
}
