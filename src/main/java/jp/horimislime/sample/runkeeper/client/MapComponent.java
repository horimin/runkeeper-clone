package jp.horimislime.sample.runkeeper.client;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.timepedia.chronoscope.client.Dataset;
import org.timepedia.chronoscope.client.browser.ChartPanel;
import org.timepedia.chronoscope.client.browser.Chronoscope;
import org.timepedia.chronoscope.client.browser.json.JsonDatasetJSO;
import org.timepedia.chronoscope.client.event.PlotHoverEvent;
import org.timepedia.chronoscope.client.event.PlotHoverHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;


public class MapComponent extends Composite {

	private static final String MAPS_API_KEY=
		"apikey";
	private static final int CHART_WIDTH=900, CHART_HEIGHT=300;
	private static final String POLYLINE_COLOR="#0000FF";
	
	interface GMapsUiBinder extends UiBinder<Widget, MapComponent> {}
	private static GMapsUiBinder uiBinder = GWT.create(GMapsUiBinder.class);
	private static MapWidget map=null;
	private Marker marker=null;
	private List<Gps> points=null;
	
	@UiField HTMLPanel rootPanel;
	
	private PlotHoverHandler hoverEventHandler=new PlotHoverHandler(){
		@Override
		public void onHover(PlotHoverEvent event){
			
			int index=event.getHoverPoints()[0];
			updateMarker(points.get(index).getLat(),points.get(index).getLng());
		}
	};
	
	
	/**
	 * GWT entry point
	 */
	public MapComponent() {
		initWidget(uiBinder.createAndBindUi(this));

		Maps.loadMapsApi(MAPS_API_KEY, "2", false, new Runnable() {
			public void run() {
				buildUi();
			}
		});
	}
	
	/**
	 * Initializes UI related instances and sets parameters
	 */
	private void buildUi() {

		map = new MapWidget();
		map.setSize("900px", "400px");
		map.setScrollWheelZoomEnabled(true);
		map.setZoomLevel(17);
		map.addControl(new LargeMapControl());
		rootPanel.add(map, "uiMaps");
		
		
		/**Load GPS data from local .csv file*/
		try {  
            final String url = "/gps.csv";  
            final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
            
            requestBuilder.sendRequest("GET", new RequestCallback() {
                @Override  
                public void onError(Request request, Throwable exception) {  
                    Window.alert("Failed to load csv file.");  
                }  
  
                @Override  
                public void onResponseReceived(Request request, Response response) {  
                    
                	points=new ArrayList<Gps>();
                	BufferedReader reader=new BufferedReader(new StringReader(response.getText()));
                    String line="";
                    try {
						while((line=reader.readLine())!=null){
							String[] elems=line.split(",");
							Gps p=new Gps();
							p.setTime(Timestamp.valueOf(elems[0]).getTime());
							p.setLat(Double.parseDouble(elems[1]));
							p.setLng(Double.parseDouble(elems[2]));
							p.setSpeed(Float.parseFloat(elems[3]));
							points.add(p);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
                	
                	
                    ChartPanel chartPanel=createChart(createJson(points));
                    chartPanel.getChart().getPlot().addPlotHoverHandler(hoverEventHandler);
                    rootPanel.add(chartPanel,"chronoscope");
                    
                    drawPolyline(points);
            		updateMarker(points.get(0).getLat(),points.get(0).getLng());
                }  
            });  
        } catch (RequestException e) {  
            e.printStackTrace();
        } 
	}
	
	/**
	 * Updates position of location marker
	 * @param lat
	 * @param lng
	 */
	private void updateMarker(double lat,double lng){
		
		LatLng point=LatLng.newInstance(lat,lng);
		
		if(marker==null){
			marker=new Marker(point);
			
			map.setCenter(point);
			map.addOverlay(marker);
			
		}else{
			marker.setLatLng(point);
		}
		
		final InfoWindowContent windowContent=
			new InfoWindowContent("Location:<BR/>lat="+lat+" lng="+lng);
		marker.addMarkerClickHandler(new MarkerClickHandler(){
			@Override
			public void onClick(MarkerClickEvent event) {
				map.getInfoWindow().open(marker, windowContent);
			}
		});
	}

	/**
	 * Draws track of location history on the map
	 * @param points
	 */
	private void drawPolyline(List<Gps> points){

		ArrayList<LatLng> coords=new ArrayList<LatLng>();
		for(Gps g : points){
			coords.add(LatLng.newInstance(g.getLat(), g.getLng()));
		}
		
		Polyline line=new Polyline(coords.toArray(new LatLng[0]),POLYLINE_COLOR, 5);
		map.addOverlay(line);
	}
	
	/**
	 * Creates chart object representing 2D graph of specified JSON dataset
	 * @param jsonStr JSON text
	 * @return
	 */
	ChartPanel createChart(String jsonStr) {
		
		Dataset[] dataset = { Chronoscope.getInstance().createDataset(getJson(jsonStr)) };
		ChartPanel newPanel = Chronoscope.createTimeseriesChart(dataset, CHART_WIDTH, CHART_HEIGHT);

		return newPanel;
	}
	
	/**
	 * Creates JSON text from a list of GPS
	 * {
	 * 	"id":"id name",
	 * 	"domain":[x0,x1,x2....],
	 * 	"range" :[y0,y1,y2....],
	 * 	"label" :"Description of graph",
	 * 	"axis"  :"Description of y-axis"
	 * }
	 * 
	 * @param jsonStr JSON text used for rendering 2D graph
	 * @return
	 */
	private String createJson(List<Gps> points){
		
		String json="gpsdata={" +
		"\"id\":\"gps\"," +
		"\"domain\":[";
		for(Gps p:points){
			json+=p.getTime()+",";
		}
		json+="]," +
		"\"range\":[";
		for(Gps p:points){
			json+=p.getSpeed()+",";
		}
		json+="],\"label\":\"Speed\",\"axis\":\"Speed(m/s)\"}";

		return json;
	}
	
	/**
	 * Parse JSON text and returns dataset object
	 * @param varName JSON text
	 * @return
	 */
	private static native JsonDatasetJSO getJson(String json) /*-{
		return eval(json);
 	}-*/;
}

